/*
 * Copyright (C) 2011 Smap Consulting Pty Ltd
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

package org.odk.collect.android.taskModel;

import org.odk.collect.android.forms.MediaFile;

import java.util.List;

public class FormLocator {
	public String ident;
	public String name;
	public int version;
	public String project;
    public boolean tasks_only;	    // Set true if this form should not be available for ad-hoc tasks
	public boolean read_only;
    public boolean search_local_data;
	public String url;
	public String manifestUrl;
	public boolean hasManifest;
    public boolean dirty;           // Force refresh - due to dynamicly created CSV
    public List<MediaFile> mediaFiles;
}
