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

import org.odk.collect.android.utilities.ResponseMessageParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpInterface {

    @NonNull
    HttpInputStreamResult getHTTPInputStream(@NonNull URI uri, String contentType, boolean calculateHash) throws Exception;

    int httpHeadRequest(@NonNull URI uri, Map<String, String> responseHeaders) throws Exception;

    ResponseMessageParser uploadFiles(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri) throws IOException;

    void clearCookieStore();

    void clearHostCredentials(String host);

    void addCredentials(String username, String password, String host);
}
