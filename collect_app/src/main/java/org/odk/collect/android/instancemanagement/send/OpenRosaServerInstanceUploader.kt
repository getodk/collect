package org.odk.collect.android.instancemanagement.send

import android.net.Uri
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.openrosa.http.OpenRosaConstants
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import timber.log.Timber
import java.io.File
import java.net.URI
import androidx.core.net.toUri
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.Collect
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.ResponseMessageParser
import org.odk.collect.entities.javarosa.parse.toUriWithParam
import org.odk.collect.openrosa.http.CaseInsensitiveHeaders
import org.odk.collect.openrosa.http.HttpHeadResult
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.strings.localization.getLocalizedString
import java.net.URLDecoder
import javax.net.ssl.HttpsURLConnection

class OpenRosaServerInstanceUploader(
    private val projectDependencyFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val httpInterface: OpenRosaHttpInterface
) : InstanceUploader {
    private val uriRemap = mutableMapOf<Uri, Uri>()

    /**
     * Uploads all files associated with an instance to the specified URL. Writes fail/success
     * status to database.
     * <p>
     * Returns a custom success message if one is provided by the server.
     */
    override fun uploadOneSubmission(
        projectId: String,
        instance: Instance,
        deviceId: String?,
        overrideURL: String?,
        referrer: String
    ): String? {
        val projectDependencyModule = projectDependencyFactory.create(projectId)
        val unprotectedSettings = projectDependencyModule.generalSettings
        val instancesRepository = projectDependencyModule.instancesRepository
        val webCredentialsUtils = WebCredentialsUtils(unprotectedSettings)

        markSubmissionFailed(instance, instancesRepository)

        val urlString = getUrlToSubmitTo(instance, deviceId, overrideURL, unprotectedSettings)
        var submissionUri = urlString.toUri()

        var contentLength = 10_000_000L

        // We already issued a head request and got a response, so we know it was an
        // OpenRosa-compliant server. We also know the proper URL to send the submission to and
        // the proper scheme.
        if (uriRemap.containsKey(submissionUri)) {
            submissionUri = uriRemap[submissionUri]!!
            Timber.i(
                "Using Uri remap for submission %s. Now: %s",
                instance.dbId,
                submissionUri.toString()
            )
        } else {
            if (submissionUri.host == null) {
                throw FormUploadException("$FAIL Host name may not be null")
            }

            val uri = try {
                URI.create(submissionUri.toString())
            } catch (e: IllegalArgumentException) {
                Timber.d(e.message ?: e.toString())
                throw FormUploadException(
                    Collect.getInstance().getLocalizedString(org.odk.collect.strings.R.string.url_error)
                )
            }

            val headResult: HttpHeadResult
            val responseHeaders: CaseInsensitiveHeaders
            try {
                headResult = httpInterface.executeHeadRequest(uri, webCredentialsUtils.getCredentials(uri))
                responseHeaders = headResult.headers

                if (responseHeaders.containsHeader(OpenRosaConstants.ACCEPT_CONTENT_LENGTH_HEADER)) {
                    val contentLengthString = responseHeaders.getAnyValue(OpenRosaConstants.ACCEPT_CONTENT_LENGTH_HEADER)
                    try {
                        contentLength = contentLengthString!!.toLong()
                    } catch (e: Exception) {
                        Timber.e(e, "Exception thrown parsing contentLength %s", contentLengthString)
                    }
                }
            } catch (e: Exception) {
                throw FormUploadException("$FAIL${e.message ?: e}")
            }

            when (headResult.statusCode) {
                HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                    throw FormUploadAuthRequestedException(
                        Collect.getInstance().getLocalizedString(
                            org.odk.collect.strings.R.string.server_auth_credentials,
                            submissionUri.host!!
                        ),
                        submissionUri
                    )
                }
                HttpsURLConnection.HTTP_NO_CONTENT -> {
                    // Redirect header received
                    if (responseHeaders.containsHeader("Location")) {
                        try {
                            var newURI = URLDecoder.decode(responseHeaders.getAnyValue("Location"), "utf-8").toUri()
                            // Allow redirects within same host. This could be redirecting to HTTPS.
                            if (submissionUri.host.equals(newURI.host, ignoreCase = true)) {
                                // Re-add params if server didn't respond with params
                                if (newURI.query == null) {
                                    newURI = newURI.buildUpon()
                                        .encodedQuery(submissionUri.encodedQuery)
                                        .build()
                                }
                                uriRemap[submissionUri] = newURI
                                submissionUri = newURI
                            } else {
                                // Don't follow a redirection attempt to a different host.
                                // We can't tell if this is a spoof or not.
                                throw FormUploadException(FAIL + "Unexpected redirection attempt to a different host: $newURI")
                            }
                        } catch (e: Exception) {
                            throw FormUploadException("$FAIL$urlString $e")
                        }
                    }
                }
                else -> {
                    if (headResult.statusCode in HttpsURLConnection.HTTP_OK until HttpsURLConnection.HTTP_MULT_CHOICE) {
                        throw FormUploadException(
                            "Failed to send to $uri. Is this an OpenRosa submission endpoint? " +
                                    "If you have a web proxy you may need to log in to your network.\n\n" +
                                    "HEAD request result status code: ${headResult.statusCode}"
                        )
                    }
                }
            }
        }

        // When encrypting submissions, there is a failure window that may mark the submission as
        // complete but leave the file-to-be-uploaded with the name "submission.xml" and the plaintext
        // submission files on disk.  In this case, upload the submission.xml and all the files in
        // the directory. This means the plaintext files and the encrypted files will be sent to the
        // server and the server will have to figure out what to do with them.
        val instanceFile = File(instance.instanceFilePath)
        var submissionFile = File(instanceFile.parentFile, "submission.xml")
        if (submissionFile.exists()) {
            Timber.w("submission.xml will be uploaded instead of %s", instanceFile.absolutePath)
        } else {
            submissionFile = instanceFile
        }

        if (!instanceFile.exists() && !submissionFile.exists()) {
            throw FormUploadException("${FAIL}instance XML file does not exist!")
        }

        val files = getFilesInParentDirectory(instanceFile, submissionFile)
            // TODO: when can this happen? It used to cause the whole submission attempt to fail. Should it?
            ?: throw FormUploadException("Error reading files to upload")

        val messageParser = ResponseMessageParser()

        try {
            val uri = URI.create(submissionUri.toString())
            val postResult = httpInterface.uploadSubmissionAndFiles(
                    submissionFile,
                    files,
                    uri,
                    webCredentialsUtils.getCredentials(uri),
                    contentLength
                )

            val responseCode = postResult.responseCode
            messageParser.setMessageResponse(postResult.httpResponse)

            if (responseCode != HttpsURLConnection.HTTP_CREATED && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                val exception = when {
                    responseCode == HttpsURLConnection.HTTP_OK -> FormUploadException("$FAIL Error: Network login failure? Again?")

                    responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED -> FormUploadException("$FAIL${postResult.reasonPhrase} ($responseCode) at $urlString")

                    messageParser.isValid -> FormUploadException("$FAIL${messageParser.messageResponse}")

                    responseCode == HttpsURLConnection.HTTP_BAD_REQUEST -> {
                        Timber.w("$FAIL${postResult.reasonPhrase} ($responseCode) at $urlString")
                        FormUploadException("Failed to upload. Please make sure the form is configured to accept submissions on the server")
                    }

                    else -> FormUploadException("$FAIL${postResult.reasonPhrase} ($responseCode) at $urlString")
                }

                throw exception
            }

        } catch (e: Exception) {
            throw FormUploadException(e.message ?: e.toString())
        }

        markSubmissionComplete(instance, instancesRepository)
        logOverrideURL(referrer, overrideURL)
        logUploadedForm(submissionUri)

        return if (messageParser.isValid) {
            messageParser.messageResponse
        } else {
            null
        }
    }

    private fun getFilesInParentDirectory(instanceFile: File, submissionFile: File): List<File>? {
        val files = mutableListOf<File>()

        // find all files in parent directory
        val allFiles = instanceFile.parentFile?.listFiles() ?: return null

        for (file in allFiles) {
            val fileName = file.name

            when {
                fileName.startsWith(".") -> continue // ignore invisible files
                fileName == instanceFile.name -> continue // the xml file has already been added
                fileName == submissionFile.name -> continue // the xml file has already been added
            }

            files.add(file)
        }

        return files
    }

    /**
     * Returns the URL this instance should be submitted to with appended deviceId.
     * <p>
     * If the upload was triggered by an external app and specified an override URL, use that one.
     * Otherwise, use the submission URL configured in the form
     * (https://getodk.github.io/xforms-spec/#submission-attributes). Finally, default to the
     * URL configured at the app level.
     */
    private fun getUrlToSubmitTo(currentInstance: Instance, deviceId: String?, overrideURL: String?, unprotectedSettings: Settings): String {
        val urlString = when {
            overrideURL != null -> overrideURL
            currentInstance.submissionUri != null -> currentInstance.submissionUri!!.trim()
            else -> getServerSubmissionURL(unprotectedSettings)
        }

        return urlString.toUriWithParam("deviceID", deviceId).toString()
    }

    private fun getServerSubmissionURL(unprotectedSettings: Settings): String {
        var serverBase = unprotectedSettings.getString(ProjectKeys.KEY_SERVER_URL)!!

        if (serverBase.endsWith(URL_PATH_SEP)) {
            serverBase = serverBase.substring(0, serverBase.length - 1)
        }

        return serverBase + OpenRosaConstants.SUBMISSION
    }

    private fun markSubmissionFailed(instance: Instance, instancesRepository: InstancesRepository) {
        instancesRepository.save(
            Instance.Builder(instance)
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )
    }

    private fun markSubmissionComplete(instance: Instance, instancesRepository: InstancesRepository) {
        instancesRepository.save(
            Instance.Builder(instance)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )
    }

    private fun logOverrideURL(referrer: String, overrideURL: String?) {
        if (overrideURL != null) {
            Analytics.log(
                AnalyticsEvents.INSTANCE_UPLOAD_CUSTOM_SERVER,
                "label",
                referrer
            )
        }
    }

    private fun logUploadedForm(submissionUri: Uri) {
        val isHttps = "https".equals(submissionUri.scheme, ignoreCase = true)

        Analytics.log(
            AnalyticsEvents.SUBMISSION,
            "label",
            if (isHttps) "HTTPS" else "HTTP",
        )
    }

    companion object {
        private const val URL_PATH_SEP = "/"
        const val FAIL = "Error: "
    }
}
