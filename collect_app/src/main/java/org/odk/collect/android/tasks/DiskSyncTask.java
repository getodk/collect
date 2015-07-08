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

package org.odk.collect.android.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for adding to the forms content provider, any forms that have been added to the
 * sdcard manually. Returns immediately if it detects an error.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DiskSyncTask extends AsyncTask<Void, String, String> {
    private final static String t = "DiskSyncTask";

    private static int counter = 0;

    int instance;
    
    DiskSyncListener mListener;

    String statusMessage;

    private static class UriFile {
    	public final Uri uri;
    	public final File file;
    	
    	UriFile(Uri uri, File file) {
    		this.uri = uri;
    		this.file = file;
    	}
    }
    
    @Override
    protected String doInBackground(Void... params) {
    	
    	instance = ++counter; // roughly track the scan # we're on... logging use only
    	Log.i(t, "["+instance+"] doInBackground begins!");
    	
    	try {
	    	// Process everything then report what didn't work.
	    	StringBuffer errors = new StringBuffer();
	    	
	        File formDir = new File(Collect.FORMS_PATH);
	        if (formDir.exists() && formDir.isDirectory()) {
	            // Get all the files in the /odk/foms directory
	            List<File> xFormsToAdd = new LinkedList<File>();
	            
	            // Step 1: assemble the candidate form files
	            //         discard files beginning with "." 
	            //         discard files not ending with ".xml" or ".xhtml"
	            {
	            	File[] formDefs = formDir.listFiles();
	            	for ( File addMe: formDefs ) {
	                    // Ignore invisible files that start with periods.
	                    if (!addMe.getName().startsWith(".")
	                            && (addMe.getName().endsWith(".xml") || addMe.getName().endsWith(".xhtml"))) {
	                    	xFormsToAdd.add(addMe);
	                    } else { 
	                    	Log.i(t, "["+instance+"] Ignoring: " + addMe.getAbsolutePath());
	                    }
	            	}
	            }
	
	            // Step 2: quickly run through and figure out what files we need to 
	            // parse and update; this is quick, as we only calculate the md5
	            // and see if it has changed.
	            List<UriFile> uriToUpdate = new ArrayList<UriFile>();
		        Cursor mCursor = null;
		        // open the cursor within a try-catch block so it can always be closed. 
		        try {
		            mCursor = Collect.getInstance().getContentResolver()
		                    .query(FormsColumns.CONTENT_URI, null, null, null, null);
			        if (mCursor == null) {
			            Log.e(t, "["+instance+"] Forms Content Provider returned NULL");
			            errors.append("Internal Error: Unable to access Forms content provider").append("\r\n");
			            return errors.toString();
			        }
		
			        mCursor.moveToPosition(-1);
		
		            while (mCursor.moveToNext()) {
		                // For each element in the provider, see if the file already exists
		                String sqlFilename =
		                    mCursor.getString(mCursor.getColumnIndex(FormsColumns.FORM_FILE_PATH));
		                String md5 = mCursor.getString(mCursor.getColumnIndex(FormsColumns.MD5_HASH));
		                File sqlFile = new File(sqlFilename);
		                if (sqlFile.exists()) {
		                    // remove it from the list of forms (we only want forms 
		                	// we haven't added at the end)
		                    xFormsToAdd.remove(sqlFile);
		                    if (!FileUtils.getMd5Hash(sqlFile).contentEquals(md5)) {
		                        // Probably someone overwrite the file on the sdcard
		                        // So re-parse it and update it's information
		                        String id = mCursor.getString(mCursor.getColumnIndex(FormsColumns._ID));
		                        Uri updateUri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, id);
		                        uriToUpdate.add(new UriFile(updateUri, sqlFile));
		                    }
		                } else {
		                	Log.w(t, "["+instance+"] file referenced by content provider does not exist " + sqlFile);
		                }
		            }
		        } finally {
		        	if ( mCursor != null ) {
		        		mCursor.close();
		        	}
		        }
	            
		        // Step3: go through uriToUpdate to parse and update each in turn.
		        // This is slow because buildContentValues(...) is slow.
		        Collections.shuffle(uriToUpdate); // Big win if multiple DiskSyncTasks running
		        for ( UriFile entry : uriToUpdate ) {
		        	Uri updateUri = entry.uri;
		        	File formDefFile = entry.file;
	                // Probably someone overwrite the file on the sdcard
	                // So re-parse it and update it's information
		        	ContentValues values;
		        	
		        	try {
		        		values = buildContentValues(formDefFile);
		        	} catch ( IllegalArgumentException e) {
		        		errors.append(e.getMessage()).append("\r\n");
		        		File badFile = new File(formDefFile.getParentFile(), formDefFile.getName() + ".bad");
		        		badFile.delete();
		        		formDefFile.renameTo(badFile);
		        		continue;
		        	}
	                
	                // update in content provider
	                int count =
	                        Collect.getInstance().getContentResolver()
	                                .update(updateUri, values, null, null);
	                    Log.i(t, "["+instance+"] " + count + " records successfully updated");
		        }
		        uriToUpdate.clear();
		        
		        // Step 4: go through the newly-discovered files in xFormsToAdd and add them.
		        // This is slow because buildContentValues(...) is slow.
		        //
		        Collections.shuffle(xFormsToAdd); // Big win if multiple DiskSyncTasks running
		        while ( !xFormsToAdd.isEmpty() ) {
		        	File formDefFile = xFormsToAdd.remove(0);
		        	
		        	// Since parsing is so slow, if there are multiple tasks, 
		        	// they may have already updated the database.  
		        	// Skip this file if that is the case.
		        	if ( isAlreadyDefined(formDefFile) ) {
		        		Log.i(t, "["+instance+"] skipping -- definition already recorded: " + formDefFile.getAbsolutePath());
		        		continue;
		        	}
		        	
	                // Parse it for the first time...
	                ContentValues values;
		        	
		        	try {
		        		values = buildContentValues(formDefFile);
		        	} catch ( IllegalArgumentException e) {
		        		errors.append(e.getMessage()).append("\r\n");
		        		File badFile = new File(formDefFile.getParentFile(), formDefFile.getName() + ".bad");
		        		badFile.delete();
		        		formDefFile.renameTo(badFile);
		        		continue;
		        	}
	                
	                // insert into content provider
		        	try {
		        		// insert failures are OK and expected if multiple 
		        		// DiskSync scanners are active.
		        		Collect.getInstance().getContentResolver()
		            				.insert(FormsColumns.CONTENT_URI, values);
		        	} catch ( SQLException e ) {
		        		Log.i(t, "["+instance+"] " + e.toString());
		        	}
	            }
	        }
	        if ( errors.length() != 0 ) {
	        	statusMessage = errors.toString();
	        } else {
	        	statusMessage = Collect.getInstance().getString(R.string.finished_disk_scan);
	        }
	        return statusMessage;
    	} finally {
    		Log.i(t, "["+instance+"] doInBackground ends!");
    	}
    }

    private boolean isAlreadyDefined(File formDefFile) {
        // first try to see if a record with this filename already exists...
        String[] projection = {
                FormsColumns._ID, FormsColumns.FORM_FILE_PATH
        };
        String[] selectionArgs = { formDefFile.getAbsolutePath() };
        String selection = FormsColumns.FORM_FILE_PATH + "=?";
        Cursor c = null;
        try {
        	c = Collect.getInstance().getContentResolver()
    				.query(FormsColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        	return ( c.getCount() > 0 );
        } finally {
        	if ( c != null ) {
        		c.close();
        	}
        }
    }
    
    public String getStatusMessage() {
    	return statusMessage;
    }
    
    /**
     * Attempts to parse the formDefFile as an XForm.
	 * This is slow because FileUtils.parseXML is slow
     * 
     * @param formDefFile
     * @return key-value list to update or insert into the content provider
     * @throws IllegalArgumentException if the file failed to parse or was missing fields
     */
    public ContentValues buildContentValues(File formDefFile) throws IllegalArgumentException {
        // Probably someone overwrite the file on the sdcard
        // So re-parse it and update it's information
        ContentValues updateValues = new ContentValues();

        HashMap<String, String> fields = null;
        try {
            fields = FileUtils.parseXML(formDefFile);
        } catch (RuntimeException e) {
        	throw new IllegalArgumentException(formDefFile.getName() + " :: " + e.toString());
        }

        String title = fields.get(FileUtils.TITLE);
        String version = fields.get(FileUtils.VERSION);
        String formid = fields.get(FileUtils.FORMID);
        String submission = fields.get(FileUtils.SUBMISSIONURI);
        String base64RsaPublicKey = fields.get(FileUtils.BASE64_RSA_PUBLIC_KEY);

        // update date
        Long now = Long.valueOf(System.currentTimeMillis());
        updateValues.put(FormsColumns.DATE, now);

        if (title != null) {
            updateValues.put(FormsColumns.DISPLAY_NAME, title);
        } else {
        	throw new IllegalArgumentException(Collect.getInstance().getString(R.string.xform_parse_error,
        			formDefFile.getName(), "title"));
        }
        if (formid != null) {
            updateValues.put(FormsColumns.JR_FORM_ID, formid);
        } else {
        	throw new IllegalArgumentException(Collect.getInstance().getString(R.string.xform_parse_error,
        			formDefFile.getName(), "id"));
        }
        if (version != null) {
            updateValues.put(FormsColumns.JR_VERSION, version);
        }
        if (submission != null) {
            updateValues.put(FormsColumns.SUBMISSION_URI, submission);
        }
        if (base64RsaPublicKey != null) {
        	updateValues.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, base64RsaPublicKey);
        }
        // Note, the path doesn't change here, but it needs to be included so the
        // update will automatically update the .md5 and the cache path.
        updateValues.put(FormsColumns.FORM_FILE_PATH, formDefFile.getAbsolutePath());
        
        return updateValues;
    }

    public void setDiskSyncListener(DiskSyncListener l) {
        mListener = l;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.SyncComplete(result);
        }
    }

}
