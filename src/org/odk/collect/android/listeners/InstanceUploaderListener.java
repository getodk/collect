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

import java.net.URI;
import java.util.ArrayList;

/**
 * @author Carl Hartung (carlhartung@gmail.com)
 */
// TODO: more useful errors in results
public interface InstanceUploaderListener {
    /**
	 * Returns the outcomes of the task
	 * 
	 * @author mitchellsundt@gmail.com
	 */
	public static class UploadOutcome {
		public final boolean isSuccessful;
		public final boolean notAllFilesUploaded;
		public final String instanceDir;
		public final String errorMessage;
		
		public UploadOutcome(String instanceDir) {
			isSuccessful = true;
			notAllFilesUploaded = false;
			this.instanceDir = instanceDir;
			errorMessage = null;
		}
		
		public UploadOutcome(String instanceDir, boolean ignored) {
			isSuccessful = true;
			notAllFilesUploaded = true;
			this.instanceDir = instanceDir;
			errorMessage = null;
		}
		
		public UploadOutcome(String instanceDir, String uri, String message) {
			isSuccessful = false;
			notAllFilesUploaded = true;
			this.instanceDir = instanceDir;
			errorMessage = message + " while sending to: " + uri;
		}
		
		public UploadOutcome(String instanceDir, URI uri, String message) {
			isSuccessful = false;
			notAllFilesUploaded = true;
			this.instanceDir = instanceDir;
			errorMessage = message + " while sending to: " + uri.toString();
		}
	}
	
	void uploadingComplete(ArrayList<UploadOutcome> result);
    void progressUpdate(int progress, int total);
}
