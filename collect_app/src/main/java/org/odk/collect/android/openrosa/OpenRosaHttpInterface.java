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

package org.odk.collect.android.openrosa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.taskModel.TaskResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

public interface OpenRosaHttpInterface {

    /**
     * Creates a http connection and sets up an input stream.
     *
     * @param uri         of the stream
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
     * @param uri         of which to perform a Http head
     * @param credentials to use for this head request
     * @return HttpHeadResult containing status code and headers
     * @throws Exception various Exceptions such as IOException can be thrown
     */
    @NonNull
    HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception;

    /**
     * Uploads submission files and then list of other files to server
     *
     * @param submissionFile The main file to be uploaded (Form file)
     * @param fileList       List of Files to be uploaded
     * @param uri            where to send the submissionFile and fileList
     * @param contentLength  contentLength requested by the server
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @NonNull
    HttpPostResult uploadSubmissionFile(@NonNull List<File> fileList,
                                               @NonNull File submissionFile,
                                               @NonNull URI uri,
                                               @Nullable HttpCredentialsInterface credentials,
                                               String status,               // smap
                                               String location_trigger,     // smap
                                               String survey_notes,         // smap
                                               String assignment_id,        // smap
                                               @NonNull long contentLength) throws Exception;

    /**
     * smap
     * Updates tasks on a Server.
     *
     * @param updateResponse Task data to be updated
     * @param uri where to send the submissionFile and fileList
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @NonNull
    HttpPostResult uploadTaskStatus(@NonNull TaskResponse updateResponse,
                                               @NonNull URI uri,
                                               @Nullable HttpCredentialsInterface credentials
                                             ) throws IOException;

    /**
     * smap
     * Updates location
     *
     * @param lat Latitude
     * @param lon longitude
     * @param uri where to send the submissionFile and fileList
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @NonNull
    HttpPostResult uploadLocation(String lat,
                                    String lon,
                                    @NonNull URI uri,
                                    @Nullable HttpCredentialsInterface credentials
    ) throws IOException;

    /**
     * smap
     * Updates tasks on a Server.
     *
     * @param fileName Name of file to be submitted
     * @param file file to be submitted
     * @return ResponseMessageParser object that contains the response XML
     * @throws IOException can be thrown if files do not exist
     */
    @NonNull
    String SubmitFileForResponse(@NonNull String fileName,
                                            @NonNull File file,
                                            @NonNull URI uri,
                                            @Nullable HttpCredentialsInterface credentials
    ) throws IOException;

    /**
     * Creates a http connection and sets up an input stream. Thats all it does as opposed to the
     * odk service that expects an xml form in the response
     * smap
     *
     * @param uri of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @return HttpGetResult - An object containing the Stream, Hash and Headers
     * @throws Exception a multitude of Exceptions such as IOException can be thrown
     */
    @NonNull
    String getRequest(@NonNull URI uri, @Nullable String contentType,
                      @Nullable HttpCredentialsInterface credentials,
                      HashMap<String, String> headers) throws Exception;

    // smap
    @NonNull
    String loginRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception;

    interface FileToContentTypeMapper {

        @NonNull
        String map(String fileName);
    }
}
