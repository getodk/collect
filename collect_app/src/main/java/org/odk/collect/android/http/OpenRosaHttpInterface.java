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

package org.odk.collect.android.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface OpenRosaHttpInterface {

    /**
     * Creates a http connection and sets up an input stream.
     *
     * @param uri of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @param credentials to use for this executeGetRequest request
     * @return HttpGetResult - An object containing the Stream, Hash and Headers
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @NonNull
    HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception;

    /**
     * Performs a Http Head request.
     *
     * @param uri of which to perform a Http head
     * @param credentials to use for this head request
     * @return HttpHeadResult containing status code and headers
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @NonNull
    HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception;

    /**
     * Performs a Http Post Request.
     *
     * @param uri of which to post
     * @param credentials to use on this post request
     * @return HttpPostResult containing response code and response message
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @NonNull
    HttpPostResult executePostRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception;

    /**
     * Uploads files to a Server.
     *
     * @param fileList List of Files to be uploaded
     * @param submissionFile The main file to be uploaded (Form file)
     * @param uri where to send the submissionFile and fileList
     * @param contentLength contentLength requested by the server
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @NonNull
    HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList,
                                        @NonNull File submissionFile,
                                        @NonNull URI uri,
                                        @Nullable HttpCredentialsInterface credentials,
                                        @NonNull long contentLength) throws Exception;

}
