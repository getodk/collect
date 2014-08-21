/*
 * Copyright 2012 Google Inc.
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
package org.odk.collect.android.picasa;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of an HTTP request.  Adapted from Ruby's "Rack" Web interface.
 */
public class Response {
    
    /**
     * The HTTP status code
     */
    public int status;
    
    /**
     * The HTTP headers received in the response
     */
    public  Map<String, List<String>> headers;
    
    /**
     * The response body, if any
     */
    public byte[] body;
    
    public Response(int status,  Map<String, List<String>> headers, byte[] body) {
        this.status = status; this.headers = headers; this.body = body;
    }
    public Response(int status,  Map<String, List<String>> headers, String body) {
        this.status = status; this.headers = headers; this.body = body.getBytes();
    }
}
