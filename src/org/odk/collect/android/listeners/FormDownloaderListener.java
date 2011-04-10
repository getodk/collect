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

/**
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface FormDownloaderListener {

	public static final class FormDetails {
		public final String stringValue;
		
		public final String formName;
		public final String formId;
		public final Integer modelVersion;
		public final Integer uiVersion;
		public final String description;
		public final String downloadUrl;
		public final String manifestUrl;
		
		public FormDetails(String stringValue) {
			this.stringValue = stringValue;
			
			formName = null;
			formId = null;
			modelVersion = null;
			uiVersion = null;
			description = null;
			downloadUrl = null;
			manifestUrl = null;
		}
		
		public FormDetails(String formName,
					String formId,
					Integer modelVersion,
					Integer uiVersion,
					String description,
					String downloadUrl,
					String manifestUrl) {
			this.stringValue = null;
			this.formName = formName;
			this.formId = formId;
			this.modelVersion = modelVersion;
			this.uiVersion = uiVersion;
			this.description = description;
			this.downloadUrl = downloadUrl;
			this.manifestUrl = manifestUrl;
		}
	}

    void formDownloadingComplete(HashMap<String, FormDetails> result);
    void progressUpdate(String currentFile, int progress, int total);
}
