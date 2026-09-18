package org.odk.collect.openrosa.forms

import org.odk.collect.entities.javarosa.parse.toUri
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
import org.odk.collect.forms.FormSourceException.FetchError
import org.odk.collect.forms.FormSourceException.SecurityError
import org.odk.collect.forms.FormSourceException.ServerNotOpenRosaError
import org.odk.collect.forms.FormSourceException.Unreachable
import org.odk.collect.forms.ManifestFile
import org.odk.collect.openrosa.forms.OpenRosaXmlFetcher.WebCredentialsProvider
import org.odk.collect.openrosa.http.OpenRosaConstants
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.openrosa.parse.OpenRosaResponseParser
import org.odk.collect.shared.result.Result
import org.odk.collect.shared.result.runAndCatch
import org.odk.collect.shared.result.toError
import org.odk.collect.shared.result.toSuccess
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.Callable
import javax.net.ssl.SSLException

class OpenRosaClient(
    serverURL: String,
    openRosaHttpInterface: OpenRosaHttpInterface?,
    private val webCredentialsProvider: WebCredentialsProvider,
    private val openRosaResponseParser: OpenRosaResponseParser,
    deviceId: String
) : FormSource, EntitySource {
    private val openRosaXMLFetcher =
        OpenRosaXmlFetcher(openRosaHttpInterface, this.webCredentialsProvider, deviceId)

    private var serverUrl: String = serverURL

    @Throws(FormSourceException::class)
    override fun fetchFormList(): List<FormListItem> {
        val result = mapException { openRosaXMLFetcher.getXML(getFormListURL()) }

        if (result.errorMessage != null) {
            when (result.responseCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    throw AuthRequired()
                }

                HttpURLConnection.HTTP_NOT_FOUND -> {
                    throw Unreachable(serverUrl)
                }

                else -> {
                    throw FormSourceException.ServerError(result.responseCode, serverUrl)
                }
            }
        }

        if (result.isOpenRosaResponse) {
            val formList = openRosaResponseParser.parseFormList(result.doc)

            if (formList != null) {
                return formList
            } else {
                throw FormSourceException.ParseError(serverUrl)
            }
        } else {
            throw ServerNotOpenRosaError()
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchManifest(manifestURL: String): ManifestFile {
        val result = mapException { openRosaXMLFetcher.getXML(manifestURL) }

        if (result.errorMessage != null) {
            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                throw FormSourceException.ServerError(result.responseCode, serverUrl)
            } else {
                throw FetchError()
            }
        }

        if (!result.isOpenRosaResponse) {
            throw FormSourceException.ParseError(serverUrl)
        }

        val mediaFiles = openRosaResponseParser.parseManifest(result.doc)
        if (mediaFiles != null) {
            return ManifestFile(result.hash, mediaFiles)
        } else {
            throw FormSourceException.ParseError(serverUrl)
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchForm(formURL: String): InputStream {
        val result = mapException { openRosaXMLFetcher.fetch(formURL, null) }

        if (result.inputStream == null) {
            throw FormSourceException.ServerError(result.statusCode, serverUrl)
        } else {
            return result.inputStream
        }
    }

    @Throws(FormSourceException::class)
    override fun fetchMediaFile(mediaFileURL: String): InputStream {
        val result = mapException { openRosaXMLFetcher.fetch(mediaFileURL, null) }

        if (result.inputStream == null) {
            throw FormSourceException.ServerError(result.statusCode, serverUrl)
        } else {
            return result.inputStream
        }
    }

    fun updateUrl(url: String) {
        this.serverUrl = url
    }

    fun updateWebCredentialsUtils(webCredentialsProvider: WebCredentialsProvider?) {
        openRosaXMLFetcher.updateWebCredentialsProvider(webCredentialsProvider)
    }

    @Throws(FormSourceException::class)
    private fun <T> mapException(callable: Callable<T>): T {
        try {
            val result = callable.call()

            if (result != null) {
                return result
            } else {
                throw FetchError()
            }
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    private fun mapException(e: Exception): FormSourceException {
        return when (e) {
            is UnknownHostException -> Unreachable(serverUrl)
            is SocketTimeoutException -> FetchError()
            is SSLException -> SecurityError(serverUrl)
            else -> FetchError()
        }
    }

    private fun getFormListURL(): String {
        var downloadListUrl = serverUrl

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length - 1)
        }

        downloadListUrl += OpenRosaConstants.FORM_LIST
        return downloadListUrl
    }

    override fun fetchDeletedStates(
        integrityUrl: String,
        ids: List<String>
    ): Result<List<Pair<String, Boolean>>, FormSourceException> {
        val uri = integrityUrl.toUri("id" to ids.joinToString(","))
        val documentFetchResult = runAndCatch {
            openRosaXMLFetcher.getXML(uri.toString())
        }

        return when (documentFetchResult) {
            is Result.Error -> mapException(documentFetchResult.value).toError()
            is Result.Success -> {
                if (documentFetchResult.value.isOpenRosaResponse) {
                    val doc = documentFetchResult.value.doc
                    val parsedResponse = openRosaResponseParser.parseIntegrityResponse(doc)

                    if (parsedResponse != null) {
                        parsedResponse.map { Pair(it.id, it.deleted) }.toSuccess()
                    } else {
                        FormSourceException.ParseError(serverUrl).toError()
                    }
                } else {
                    FormSourceException.ParseError(serverUrl).toError()
                }
            }
        }
    }
}
