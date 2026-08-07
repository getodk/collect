package org.odk.collect.openrosa.http

import java.io.File
import java.net.URI
import java.util.function.Supplier

interface OpenRosaHttpInterface {

    /**
     * Creates an http connection and sets up an input stream.
     *
     * @param uri         of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @param credentials to use for this executeGetRequest request
     * @return HttpGetResult - An object containing the Stream, Hash and Headers
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @Throws(Exception::class)
    fun executeGetRequest(uri: URI, contentType: String?, credentials: HttpCredentialsInterface): HttpGetResult

    /**
     * Performs an Http Head request.
     *
     * @param uri         of which to perform a Http head
     * @param credentials to use for this head request
     * @return HttpHeadResult containing status code and headers
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @Throws(Exception::class)
    fun executeHeadRequest(uri: URI, credentials: HttpCredentialsInterface): HttpHeadResult

    /**
     * Uploads submission files and then list of other files to server
     *
     * @param submissionFile The main file to be uploaded (Form file)
     * @param fileList       List of Files to be uploaded
     * @param uri            where to send the submissionFile and fileList
     * @param contentLength  contentLength requested by the server
     * @param isCancelled    when non-null, the upload is aborted as soon as this starts returning
     *                       {@code true}; leave as null when cancellation is not needed
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @Throws(Exception::class)
    fun uploadSubmissionAndFiles(
        submissionFile: File,
        fileList: List<@JvmSuppressWildcards File>,
        uri: URI,
        credentials: HttpCredentialsInterface,
        contentLength: Long,
        isCancelled: Supplier<Boolean>? = null
    ): HttpPostResult

    interface FileToContentTypeMapper {
        fun map(fileName: String): String
    }
}
