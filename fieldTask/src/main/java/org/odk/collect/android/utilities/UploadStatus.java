package org.odk.collect.android.utilities;

/*
 * This class contains the results from submission of results
 */
public class UploadStatus {
	final public static int UNKNOWN_HOST = -1;
	
	public int responseCode = 0;
	public String instanceName = null;
	public String message = null;
	
	public UploadStatus(int code, String file, String mesg) {
		responseCode = code;
		instanceName = file;
		message = mesg;
	}
}
