package org.odk.collect.android.support

import org.odk.collect.android.formmanagement.metadata.FormMetadataParser.readMetadata
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.openrosa.http.CaseInsensitiveEmptyHeaders
import org.odk.collect.openrosa.http.CaseInsensitiveHeaders
import org.odk.collect.openrosa.http.HttpCredentialsInterface
import org.odk.collect.openrosa.http.HttpGetResult
import org.odk.collect.openrosa.http.HttpHeadResult
import org.odk.collect.openrosa.http.HttpPostResult
import org.odk.collect.openrosa.http.OpenRosaConstants
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.shared.strings.Md5.getMd5Hash
import org.odk.collect.shared.strings.RandomString
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.UUID
import java.util.stream.Collectors

class StubOpenRosaServer : OpenRosaHttpInterface {
    private val forms: MutableList<XFormItem> = mutableListOf()
    private var username: String? = null
    private var password: String? = null
    private var alwaysReturnError = false
    private var fetchingFormsError = false
    private var noHashInFormList = false
    private var noHashPrefixInMediaFiles = false
    private var randomHash = false

    /**
     * A list of submitted forms, maintained in the original order of submission, with the oldest forms appearing first.
     */
    private val submittedForms: MutableList<File> = mutableListOf()
    private val deletedEntities: MutableList<String> = mutableListOf()
    private var includeIntegrityUrl = false

    val hostName = "server.example.com"
    val url: String
        get() = "https://$hostName"

    val submissions: List<File>
        get() = submittedForms

    var accesses = 0
        private set

    override fun executeGetRequest(
        uri: URI,
        contentType: String?,
        credentials: HttpCredentialsInterface
    ): HttpGetResult {
        accesses += 1

        if (alwaysReturnError) {
            return HttpGetResult(null, HashMap(), "", 500)
        }

        if (uri.host != hostName) {
            return HttpGetResult(
                null,
                HashMap(),
                "Trying to connect to incorrect server: " + uri.host,
                410
            )
        } else if (credentialsIncorrect(credentials)) {
            return HttpGetResult(null, HashMap(), "", 401)
        } else if (uri.path == OpenRosaConstants.FORM_LIST) {
            return HttpGetResult(formListResponse, standardHeaders, "", 200)
        } else if (uri.path == "/form") {
            if (fetchingFormsError) {
                return HttpGetResult(null, HashMap(), "", 500)
            }

            return HttpGetResult(getFormResponse(uri), standardHeaders, "", 200)
        } else if (uri.path == "/manifest") {
            val manifestResponse = getManifestResponse(uri)

            return if (manifestResponse != null) {
                HttpGetResult(manifestResponse, standardHeaders, "", 200)
            } else {
                HttpGetResult(null, HashMap(), "", 404)
            }
        } else if (uri.path.startsWith("/mediaFile")) {
            return HttpGetResult(getMediaFile(uri), HashMap(), "", 200)
        } else if (uri.path.startsWith("/integrityUrl")) {
            return HttpGetResult(getIntegrityResponse(uri), standardHeaders, "", 200)
        } else {
            return HttpGetResult(null, HashMap(), "", 404)
        }
    }

    override fun executeHeadRequest(
        uri: URI,
        credentials: HttpCredentialsInterface
    ): HttpHeadResult {
        accesses += 1

        if (alwaysReturnError) {
            return HttpHeadResult(500, CaseInsensitiveEmptyHeaders())
        }

        if (uri.host != hostName) {
            return HttpHeadResult(410, CaseInsensitiveEmptyHeaders())
        } else if (credentialsIncorrect(credentials)) {
            return HttpHeadResult(401, CaseInsensitiveEmptyHeaders())
        } else if (uri.path == OpenRosaConstants.SUBMISSION) {
            val headers = standardHeaders
            headers["x-openrosa-accept-content-length"] = "10485760"

            return HttpHeadResult(204, MapHeaders(headers))
        } else {
            return HttpHeadResult(404, CaseInsensitiveEmptyHeaders())
        }
    }

    override fun uploadSubmissionAndFiles(
        submissionFile: File,
        fileList: List<File>,
        uri: URI,
        credentials: HttpCredentialsInterface,
        contentLength: Long
    ): HttpPostResult {
        accesses += 1

        if (alwaysReturnError) {
            return HttpPostResult("", 500, "")
        }

        if (uri.host != hostName) {
            return HttpPostResult("Trying to connect to incorrect server: " + uri.host, 410, "")
        } else if (credentialsIncorrect(credentials)) {
            return HttpPostResult("", 401, "")
        } else if (uri.path == OpenRosaConstants.SUBMISSION) {
            submittedForms.add(submissionFile)
            return HttpPostResult("", 201, "")
        } else {
            return HttpPostResult("", 404, "")
        }
    }

    fun setCredentials(username: String?, password: String?) {
        this.username = username
        this.password = password
    }

    fun addForm(formLabel: String?, id: String?, version: String?, formXML: String) {
        forms.add(XFormItem(formLabel, formXML, id, version))
    }

    fun addForm(
        formLabel: String?,
        id: String?,
        version: String?,
        formXML: String,
        mediaFiles: List<String>
    ) {
        forms.add(
            XFormItem(
                formLabel,
                formXML,
                id,
                version,
                mediaFiles.stream().map { name: String -> MediaFileItem(name, name) }.collect(
                    Collectors.toList()
                )
            )
        )
    }

    fun addForm(formXML: String, mediaFiles: List<MediaFileItem> = emptyList()) {
        try {
            FileUtils.getResourceAsStream("forms/$formXML").use { formDefStream ->
                addFormFromInputStream(
                    formXML, mediaFiles,
                    formDefStream!!
                )
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun removeForm(formLabel: String) {
        forms.removeIf { xFormItem: XFormItem -> xFormItem.formLabel == formLabel }
    }

    fun alwaysReturnError() {
        alwaysReturnError = true
    }

    fun neverReturnError() {
        alwaysReturnError = false
    }

    fun errorOnFetchingForms() {
        fetchingFormsError = true
    }

    fun removeHashInFormList() {
        noHashInFormList = true
    }

    fun removeMediaFileHashPrefix() {
        noHashPrefixInMediaFiles = true
    }

    fun returnRandomMediaFileHash() {
        randomHash = true
    }

    fun includeIntegrityUrl() {
        includeIntegrityUrl = true
    }

    private fun credentialsIncorrect(credentials: HttpCredentialsInterface?): Boolean {
        return if (username == null && password == null) {
            false
        } else {
            if (credentials == null) {
                true
            } else {
                credentials.username != username || credentials.password != password
            }
        }
    }

    private val standardHeaders: HashMap<String, String>
        get() {
            val headers =
                HashMap<String, String>()
            headers["x-openrosa-version"] = "1.0"
            return headers
        }

    private val formListResponse: InputStream
        get() {
            val stringBuilder = StringBuilder()
            stringBuilder
                .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
                .append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n")

            for (i in forms.indices) {
                val form = forms[i]

                val xform = stringBuilder
                    .append("<xform>\n")
                    .append("<formID>" + form.iD + "</formID>\n")
                    .append("<name>" + form.formLabel + "</name>\n")
                    .append("<version>" + form.version + "</version>\n")

                if (!noHashInFormList) {
                    val hash = getMd5Hash(getFormXML(i.toString()))
                    xform.append("<hash>md5:$hash</hash>\n")
                }

                xform.append("<downloadUrl>$url/form?formId=$i</downloadUrl>\n")

                if (form.mediaFiles.isNotEmpty()) {
                    xform.append("<manifestUrl>$url/manifest?formId=$i</manifestUrl>\n")
                }

                stringBuilder.append("</xform>\n")
            }

            stringBuilder.append("</xforms>")
            return ByteArrayInputStream(stringBuilder.toString().toByteArray())
        }

    private fun getFormResponse(uri: URI): InputStream {
        val formID = uri.query.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        return getFormXML(formID)
    }

    private fun getManifestResponse(uri: URI): InputStream? {
        val formID = uri.query.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val xformItem = forms[formID.toInt()]

        if (xformItem.mediaFiles.isEmpty()) {
            return null
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder
                .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
                .append("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">\n")

            for (mediaFile in xformItem.mediaFiles) {
                val mediaFileHash = mediaFile.hash
                    ?: if (randomHash) {
                        RandomString.randomString(8)
                    } else {
                        getMd5Hash(FileUtils.getResourceAsStream("media/" + mediaFile.file)!!)
                    }

                if (mediaFile is EntityListItem) {
                    if (mediaFile.isApprovalList) {
                        stringBuilder
                            .append("<mediaFile type=\"approvalEntityList\">")
                    } else {
                        stringBuilder
                            .append("<mediaFile type=\"entityList\">")
                    }
                } else {
                    stringBuilder
                        .append("<mediaFile>")
                }

                stringBuilder
                    .append("<filename>" + mediaFile.name + "</filename>\n")

                if (noHashPrefixInMediaFiles) {
                    stringBuilder.append("<hash>$mediaFileHash </hash>\n")
                } else if (mediaFile is EntityListItem) {
                    stringBuilder.append("<hash>md5:" + mediaFile.version + " </hash>\n")
                } else {
                    stringBuilder.append("<hash>md5:$mediaFileHash </hash>\n")
                }

                stringBuilder
                    .append("<downloadUrl>" + url + "/mediaFile/" + formID + "/" + mediaFile.id + "</downloadUrl>\n")

                if (mediaFile is EntityListItem && includeIntegrityUrl) {
                    stringBuilder.append("<integrityUrl>" + url + "/integrityUrl</integrityUrl>\n")
                }

                stringBuilder.append("</mediaFile>\n")
            }

            stringBuilder.append("</manifest>")
            return ByteArrayInputStream(stringBuilder.toString().toByteArray())
        }
    }

    private fun getIntegrityResponse(uri: URI): InputStream {
        val ids = uri.query.split("=".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val stringBuilder = StringBuilder()
        stringBuilder
            .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
            .append("<data>\n")
            .append("<entities>\n")

        for (id in ids) {
            stringBuilder
                .append("<entity id=\"$id\">\n")
                .append("<deleted>${deletedEntities.contains(id)}</deleted>\n")
                .append("</entity>\n")
        }

        stringBuilder
            .append("</entities>\n")
            .append("</data>\n")

        return ByteArrayInputStream(stringBuilder.toString().toByteArray())
    }

    private fun getFormXML(formID: String): InputStream {
        val xmlPath = forms[formID.toInt()].formXML
        return FileUtils.getResourceAsStream("forms/$xmlPath")!!
    }

    private fun getMediaFile(uri: URI): InputStream {
        val formID = uri.path.split("/mediaFile/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val id = uri.path.split("/mediaFile/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val xformItem = forms[formID.toInt()]
        val actualFileName =
            xformItem.mediaFiles.stream().filter { mediaFile: MediaFileItem -> mediaFile.id == id }
                .findFirst().get().file
        return FileUtils.getResourceAsStream("media/$actualFileName")!!
    }

    private fun addFormFromInputStream(
        formXML: String,
        mediaFiles: List<MediaFileItem>,
        formDefStream: InputStream
    ) {
        val formMetadata = readMetadata(formDefStream)
        forms.add(
            XFormItem(
                formMetadata.title,
                formXML,
                formMetadata.id,
                formMetadata.version,
                mediaFiles
            )
        )
    }

    fun deleteEntity(list: String, id: String) {
        deletedEntities.add(id)

        for (form in forms) {
            val entityList = form.mediaFiles.stream()
                .filter { mediaFileItem: MediaFileItem -> mediaFileItem is EntityListItem && mediaFileItem.name == list }
                .findFirst()

            if (entityList.isPresent) {
                (entityList.get() as EntityListItem).incrementVersion()
            }
        }
    }

    private class XFormItem(
        val formLabel: String?,
        val formXML: String,
        val iD: String?,
        val version: String?,
        val mediaFiles: List<MediaFileItem> = emptyList()
    )

    open class MediaFileItem(
        val name: String,
        val file: String = name,
        val hash: String? = null
    ) {
        val id: String = UUID.randomUUID().toString()
    }

    class EntityListItem : MediaFileItem {
        var version: Int = 0
            private set
        var isApprovalList: Boolean = false
            private set

        constructor(
            name: String,
            file: String,
            version: Int,
            approvalList: Boolean = false
        ) : super(name, file) {
            this.version = version
            this.isApprovalList = approvalList
        }

        constructor(name: String) : super(name, name, name)

        constructor(name: String, approvalList: Boolean) : this(name, name, 0, approvalList)

        fun incrementVersion() {
            version++
        }
    }

    private class MapHeaders(private val headers: Map<String, String>) :
        CaseInsensitiveHeaders {
        override fun getHeaders(): Set<String> {
            return headers.keys
        }

        override fun containsHeader(header: String): Boolean {
            return headers.containsKey(header.lowercase())
        }

        override fun getAnyValue(header: String): String? {
            return headers[header.lowercase()]
        }

        override fun getValues(header: String): List<String?> {
            return listOf(headers[header.lowercase()])
        }
    }
}
