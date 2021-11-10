/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.formmanagement

import org.odk.collect.analytics.Analytics.Companion.log
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber
import java.io.File
import java.util.ArrayList

class ServerFormsDetailsFetcher(
    private val formsRepository: FormsRepository,
    private val formSource: FormSource,
    private val diskFormsSynchronizer: DiskFormsSynchronizer
) {
    fun updateUrl(url: String?) {
        (formSource as OpenRosaFormSource).updateUrl(url)
    }

    fun updateCredentials(webCredentialsUtils: WebCredentialsUtils?) {
        (formSource as OpenRosaFormSource).updateWebCredentialsUtils(webCredentialsUtils)
    }

    @Throws(FormSourceException::class)
    fun fetchFormDetails(): List<ServerFormDetails> {
        diskFormsSynchronizer.synchronize()
        val formListItems = formSource.fetchFormList()
        val serverFormDetailsList: MutableList<ServerFormDetails> = ArrayList()
        for (listItem in formListItems) {
            var manifestFile: ManifestFile? = null
            if (listItem.manifestURL != null) {
                manifestFile = getManifestFile(formSource, listItem.manifestURL)
            }
            val forms = formsRepository.getAllNotDeletedByFormId(listItem.formID)
            val thisFormAlreadyDownloaded = !forms.isEmpty()
            var isNewerFormVersionAvailable = false
            if (!isHashValid(listItem.hashWithPrefix)) {
                log(AnalyticsEvents.NULL_OR_EMPTY_FORM_HASH)
            } else if (thisFormAlreadyDownloaded) {
                val form = getFormByHash(listItem.hashWithPrefix)
                if (form == null || form.isDeleted) {
                    isNewerFormVersionAvailable = true
                } else if (manifestFile != null) {
                    val newMediaFiles = manifestFile.mediaFiles
                    if (!newMediaFiles.isEmpty()) {
                        isNewerFormVersionAvailable =
                            areNewerMediaFilesAvailable(form, newMediaFiles)
                    }
                }
            }
            val serverFormDetails = ServerFormDetails(
                listItem.name,
                listItem.downloadURL,
                listItem.formID,
                listItem.version,
                listItem.hashWithPrefix,
                !thisFormAlreadyDownloaded,
                isNewerFormVersionAvailable,
                manifestFile
            )
            serverFormDetailsList.add(serverFormDetails)
        }
        return serverFormDetailsList
    }

    private fun getManifestFile(formSource: FormSource, manifestUrl: String?): ManifestFile? {
        return if (manifestUrl == null) {
            null
        } else try {
            formSource.fetchManifest(manifestUrl)
        } catch (formSourceException: FormSourceException) {
            Timber.w(formSourceException)
            null
        }
    }

    private fun areNewerMediaFilesAvailable(
        existingForm: Form,
        newMediaFiles: List<MediaFile?>
    ): Boolean {
        val localMediaFiles = FormUtils.getMediaFiles(existingForm)
        for (newMediaFile in newMediaFiles) {
            if (!isMediaFileAlreadyDownloaded(localMediaFiles, newMediaFile)) {
                return true
            }
        }
        return false
    }

    private fun getFormByHash(hashWithPrefix: String): Form? {
        val hash = hashWithPrefix.substring("md5:".length)
        return formsRepository.getOneByMd5Hash(hash)
    }

    private fun isHashValid(hash: String?): Boolean {
        return hash != null && hash.startsWith("md5:")
    }

    companion object {
        private fun isMediaFileAlreadyDownloaded(
            localMediaFiles: List<File>,
            newMediaFile: MediaFile?
        ): Boolean {
            // TODO Zip files are ignored we should find a way to take them into account too
            if (newMediaFile!!.filename!!.endsWith(".zip")) {
                return true
            }
            var mediaFileHash = newMediaFile.hash
            mediaFileHash = mediaFileHash!!.substring(4, mediaFileHash.length)
            for (localMediaFile in localMediaFiles) {
                if (mediaFileHash == getMd5Hash(localMediaFile)) {
                    return true
                }
            }
            return false
        }
    }
}
