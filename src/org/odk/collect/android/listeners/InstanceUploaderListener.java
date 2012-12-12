/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.listeners;

import java.util.HashMap;

import android.net.Uri;

/**
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface InstanceUploaderListener {
    void uploadingComplete(HashMap<String, String> result);
    void progressUpdate(int progress, int total);
    void authRequest(Uri url, HashMap<String, String> doneSoFar);
}
