package org.odk.collect.android.formmanagement

import android.database.SQLException
import org.javarosa.xform.parse.XFormParser
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.utils.Validator
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.strings.Md5
import org.odk.collect.strings.localization.getLocalizedString
import timber.log.Timber
import java.io.File
import java.util.Collections
import java.util.LinkedList

object LocalFormUseCases {

    private var counter = 0

    @JvmStatic
    fun deleteForm(
        formsRepository: FormsRepository,
        instancesRepository: InstancesRepository,
        id: Long
    ) {
        val form = formsRepository[id]
        val instancesForVersion = instancesRepository.getAllNotDeletedByFormIdAndVersion(
            form!!.formId,
            form.version
        )

        // If there's more than one form with the same formid/version, trust the user that they want to truly delete this one
        // because otherwise it may not ever be removed (instance deletion only deletes one corresponding form).
        val formsWithSameFormIdVersion = formsRepository.getAllByFormIdAndVersion(
            form.formId,
            form.version
        )

        if (instancesForVersion.isEmpty() || formsWithSameFormIdVersion.size > 1) {
            formsRepository.delete(id)
        } else {
            formsRepository.softDelete(form.dbId)
        }
    }

    fun synchronizeWithDisk(formsRepository: FormsRepository, formsDir: String?): String {
        var statusMessage = ""
        val instance = ++counter
        Timber.i("[%d] doInBackground begins!", instance)
        val idsToDelete: MutableList<Long> = ArrayList()
        return try {
            // Process everything then report what didn't work.
            val errors = StringBuilder()
            val formDir = File(formsDir)
            if (formDir.exists() && formDir.isDirectory) {
                // Get all the files in the /odk/foms directory
                val formDefs = formDir.listFiles()

                // Step 1: assemble the candidate form files
                val formsToAdd = filterFormsToAdd(formDefs, instance)

                // Step 2: quickly run through and figure out what files we need to
                // parse and update; this is quick, as we only calculate the md5
                // and see if it has changed.
                val uriToUpdate: MutableList<IdFile?> = ArrayList()
                val forms = formsRepository.all
                for (form in forms) {
                    // For each element in the provider, see if the file already exists
                    val sqlFilename = form.formFilePath
                    val md5 = form.mD5Hash
                    val sqlFile = File(sqlFilename)
                    if (sqlFile.exists()) {
                        // remove it from the list of forms (we only want forms
                        // we haven't added at the end)
                        formsToAdd.remove(sqlFile)
                        val md5Computed = Md5.getMd5Hash(sqlFile)
                        if (md5Computed == null || md5 == null || md5Computed != md5) {
                            // Probably someone overwrite the file on the sdcard
                            // So re-parse it and update it's information
                            val id = form.dbId
                            uriToUpdate.add(IdFile(id, sqlFile))
                        }
                    } else {
                        // File not found in sdcard but file path found in database
                        // probably because the file has been deleted or filename was changed in sdcard
                        // Add the ID to list so that they could be deleted all together
                        val id = form.dbId
                        idsToDelete.add(id)
                    }
                }

                // Delete the forms not found in sdcard from the database
                for (id in idsToDelete) {
                    formsRepository.delete(id)
                }

                // Step3: go through uriToUpdate to parse and update each in turn.
                // Note: buildContentValues calls getMetadataFromFormDefinition which parses the
                // form XML. This takes time for large forms and/or slow devices.
                Collections.shuffle(uriToUpdate) // Big win if multiple DiskSyncTasks running
                for (entry in uriToUpdate) {
                    val formDefFile = entry!!.file
                    // Probably someone overwrite the file on the sdcard
                    // So re-parse it and update it's information
                    var form: Form
                    form = try {
                        parseForm(formDefFile)
                    } catch (e: IllegalArgumentException) {
                        errors.append(e.message).append("\r\n")
                        val badFile = File(
                            formDefFile.parentFile,
                            formDefFile.name + ".bad"
                        )
                        badFile.delete()
                        formDefFile.renameTo(badFile)
                        continue
                    }
                    formsRepository.save(
                        Form.Builder(form)
                            .dbId(entry.id)
                            .build()
                    )
                }
                uriToUpdate.clear()

                // Step 4: go through the newly-discovered files in xFormsToAdd and add them.
                // This is slow because buildContentValues(...) is slow.
                //
                Collections.shuffle(formsToAdd) // Big win if multiple DiskSyncTasks running
                while (!formsToAdd.isEmpty()) {
                    val formDefFile = formsToAdd.removeAt(0)

                    // Since parsing is so slow, if there are multiple tasks,
                    // they may have already updated the database.
                    // Skip this file if that is the case.
                    if (formsRepository.getOneByPath(formDefFile!!.absolutePath) != null) {
                        Timber.i(
                            "[%d] skipping -- definition already recorded: %s",
                            instance,
                            formDefFile.absolutePath
                        )
                        continue
                    }

                    // Parse it for the first time...
                    var form: Form
                    form = try {
                        parseForm(formDefFile)
                    } catch (e: IllegalArgumentException) {
                        errors.append(e.message).append("\r\n")
                        val badFile = File(
                            formDefFile.parentFile,
                            formDefFile.name + ".bad"
                        )
                        badFile.delete()
                        formDefFile.renameTo(badFile)
                        continue
                    }

                    // insert into content provider
                    try {
                        // insert failures are OK and expected if multiple
                        // DiskSync scanners are active.
                        formsRepository.save(form)
                    } catch (e: SQLException) {
                        Timber.i("[%d] %s", instance, e.toString())
                    }
                }
            }
            if (errors.length != 0) {
                statusMessage = errors.toString()
            } else {
                Timber.d(
                    Collect.getInstance()
                        .getLocalizedString(org.odk.collect.strings.R.string.finished_disk_scan)
                )
            }
            statusMessage
        } finally {
            Timber.i("[%d] doInBackground ends!", instance)
        }
    }

    @JvmStatic
    fun filterFormsToAdd(formDefs: Array<File>?, backgroundInstanceId: Int): MutableList<File?> {
        val formsToAdd: MutableList<File?> = LinkedList()
        if (formDefs != null) {
            for (candidate in formDefs) {
                if (shouldAddFormFile(candidate.name)) {
                    formsToAdd.add(candidate)
                } else {
                    Timber.i("[%d] Ignoring: %s", backgroundInstanceId, candidate.absolutePath)
                }
            }
        }
        return formsToAdd
    }

    @JvmStatic
    fun shouldAddFormFile(fileName: String): Boolean {
        // discard files beginning with "."
        // discard files not ending with ".xml" or ".xhtml"
        val ignoredFile = fileName.startsWith(".")
        val xmlFile = fileName.endsWith(".xml")
        val xhtmlFile = fileName.endsWith(".xhtml")
        return !ignoredFile && (xmlFile || xhtmlFile)
    }

    @Throws(IllegalArgumentException::class)
    private fun parseForm(formDefFile: File?): Form {
        // Probably someone overwrite the file on the sdcard
        // So re-parse it and update it's information
        val builder = Form.Builder()
        val fields: HashMap<String, String>
        fields = try {
            FileUtils.getMetadataFromFormDefinition(formDefFile)
        } catch (e: RuntimeException) {
            throw IllegalArgumentException(formDefFile!!.name + " :: " + e.toString())
        } catch (e: XFormParser.ParseException) {
            throw IllegalArgumentException(formDefFile!!.name + " :: " + e.toString())
        }

        // update date
        val now = System.currentTimeMillis()
        builder.date(now)
        val title = fields[FileUtils.TITLE]
        if (title != null) {
            builder.displayName(title)
        } else {
            throw IllegalArgumentException(
                Collect.getInstance()
                    .getLocalizedString(
                        org.odk.collect.strings.R.string.xform_parse_error,
                        formDefFile!!.name,
                        "title"
                    )
            )
        }
        val formid = fields[FileUtils.FORMID]
        if (formid != null) {
            builder.formId(formid)
        } else {
            throw IllegalArgumentException(
                Collect.getInstance()
                    .getLocalizedString(
                        org.odk.collect.strings.R.string.xform_parse_error,
                        formDefFile!!.name,
                        "id"
                    )
            )
        }
        val version = fields[FileUtils.VERSION]
        if (version != null) {
            builder.version(version)
        }
        val submission = fields[FileUtils.SUBMISSIONURI]
        if (submission != null) {
            if (Validator.isUrlValid(submission)) {
                builder.submissionUri(submission)
            } else {
                throw IllegalArgumentException(
                    Collect.getInstance().getLocalizedString(
                        org.odk.collect.strings.R.string.xform_parse_error,
                        formDefFile!!.name,
                        "submission url"
                    )
                )
            }
        }
        val base64RsaPublicKey = fields[FileUtils.BASE64_RSA_PUBLIC_KEY]
        if (base64RsaPublicKey != null) {
            builder.base64RSAPublicKey(base64RsaPublicKey)
        }
        builder.autoDelete(fields[FileUtils.AUTO_DELETE])
        builder.autoSend(fields[FileUtils.AUTO_SEND])
        builder.geometryXpath(fields[FileUtils.GEOMETRY_XPATH])

        // Note, the path doesn't change here, but it needs to be included so the
        // update will automatically update the .md5 and the cache path.
        builder.formFilePath(formDefFile!!.absolutePath)
        builder.formMediaPath(
            FileUtils.constructMediaPath(
                formDefFile.absolutePath
            )
        )
        return builder.build()
    }

    private class IdFile(val id: Long, val file: File)
}
