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

import org.odk.collect.android.utilities.ResponseMessageParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpInterface {

    /**
     * Creates a http connection and sets up an input stream.
     *
     * @param uri of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @return HttpInputStreamResult - An object containing the Stream, Hash and Headers
     * @throws Exception a multitude of Exceptions such as IOException can be thrown
     */
    @NonNull
    HttpInputStreamResult getHttpInputStream(@NonNull URI uri, @Nullable String contentType) throws Exception;

    /**
     * Performs a HTTP Head request.
     *
     * @param uri of which to perform a HTTP head
     * @param responseHeaders Map which is populated with the HTTP Headers
     * @return HTTP status code
     * @throws Exception a multitude of Exceptions such as IOException can be thrown
     */
    int httpHeadRequest(@NonNull URI uri, @NonNull Map<String, String> responseHeaders) throws Exception;

    /**
     * Uploads files to a Server.
     *
     * @param fileList List of Files to be uploaded
     * @param submissionFile The main file to be uploaded (Form file)
     * @param uri where to send the submissionFile and fileList
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    ResponseMessageParser uploadSubmissionFile(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri) throws IOException;

    /**
     * Clears the Cookie Stores
     */
    void clearCookieStore();

    /**
     * Clears the host credentials.
     *
     * @param host to clear the credentials of
     */
    void clearHostCredentials(String host);

    /**
     * Adds username and password to a particular host.
     *
     * @param username the user name
     * @param password the password
     * @param host the host to add the username / password to
     */
    void addCredentials(String username, String password, String host);

}
