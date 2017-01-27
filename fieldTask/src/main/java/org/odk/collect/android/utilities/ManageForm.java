/*
 * Copyright (C) 2014 Smap Consulting Pty Ltd
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

package org.odk.collect.android.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TaskAssignment;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.STFileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ManageForm {
	
	public class ManageFormDetails {
         public long id = 0;
		 public String formName = null;
	     public String formPath = null;
	     public String submissionUri = null;
	     public boolean exists = false;
	}
	
	public ManageFormDetails getFormDetails(String formId, String formVersionString, String source) {
	
		ManageFormDetails fd = new ManageFormDetails();
   	 	Cursor c = null;
        
		try {
        	
        	String selectionClause = FormsColumns.JR_FORM_ID + "=? AND "
					+ FormsColumns.JR_VERSION + "=? AND "
                    + FormsColumns.SOURCE + "=?";
        	
        	String [] selectionArgs = new String[] { formId, formVersionString, source };
        	//String [] selectionArgs = new String [1];
        	//selectionArgs[0] = formId;
        	String [] proj = {FormsColumns._ID, FormsColumns.DISPLAY_NAME, FormsColumns.JR_FORM_ID,
        			FormsColumns.SUBMISSION_URI,FormsColumns.FORM_FILE_PATH}; 
        	
        	final ContentResolver resolver = Collect.getInstance().getContentResolver();
        	c = resolver.query(FormsColumns.CONTENT_URI, proj, selectionClause, selectionArgs, null);
            
        	if(c.getCount() > 0) {
        		
            	// Form is already on the phone
	        	 c.moveToFirst();
	        	 fd.id = c.getLong(c.getColumnIndex(FormsColumns._ID));
	             fd.formName = c.getString(c.getColumnIndex(FormsColumns.DISPLAY_NAME));
	             fd.submissionUri = c.getString(c.getColumnIndex(FormsColumns.SUBMISSION_URI));
	             fd.formPath = c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
	             fd.exists = true;
             
        	} else {
        		fd.exists = false;
        	}
		 } catch (Throwable e) {
       		 Log.e("ManageForm", e.getMessage());
    	 }
		c.close();
		
		return fd;
	}

    public void updateFormDetails(Long id, String displayName, boolean tasks_only) {


        try {

            Uri formUri =  Uri.withAppendedPath(FormsColumns.CONTENT_URI, id.toString());

            ContentValues values = new ContentValues();
            values.put(FormsColumns.DISPLAY_NAME, displayName);
            values.put(FormsColumns.TASKS_ONLY, tasks_only ? "yes" : "no");

            Collect.getInstance().getContentResolver().update(formUri, values, null, null);


        } catch (Throwable e) {
            Log.e("ManageForm", e.getMessage());
        }

    }
	
	private boolean isIncompleteInstance(String formId, String version) {
		
		boolean isIncomplete = false;
   	 	Cursor c = null;
        
		try {
        	
			// get all complete or failed submission instances
			String selection = null;
			String selectionArgs1 [] = { InstanceProviderAPI.STATUS_INCOMPLETE, 
					formId
					};
			
			String selectionArgs2 [] = 	{ InstanceProviderAPI.STATUS_INCOMPLETE, 
					formId,
                    version
					};
			
			if(version == null) {
				selection = InstanceColumns.STATUS + "=? and "
						+ InstanceColumns.JR_FORM_ID + "=? and "  
						+ InstanceColumns.JR_VERSION + " is null";
				
			} else {
				selection = InstanceColumns.STATUS + "=? and "
						+ InstanceColumns.JR_FORM_ID + "=? and "  
						+ InstanceColumns.JR_VERSION + "=?";			

			}
			
			

        	String [] proj = {InstanceColumns._ID}; 
        	
        	final ContentResolver resolver = Collect.getInstance().getContentResolver();
        	if(version == null) {
        		c = resolver.query(InstanceColumns.CONTENT_URI, proj, selection, selectionArgs1, null);
        	} else {
        		c = resolver.query(InstanceColumns.CONTENT_URI, proj, selection, selectionArgs2, null);
        	}
            
        	if(c.getCount() > 0) {
        		
            	isIncomplete = true;
             
        	} 
		 } catch (Exception e) {
       		 Log.e("ManageForm:isIncompleteInstance", "Error: " + e.getMessage());
    	 }
		c.close();
		
		return isIncomplete;
	}
    
    /*
     * Delete any forms not in the passed in HashMap unless there is an incomplete instance
     */
    public void deleteForms(HashMap <String, String> formMap, HashMap <String, String> results) {

        
   	 	Cursor c = null;
        
		try {
        	
        	String [] proj = {FormsColumns._ID, FormsColumns.JR_FORM_ID, FormsColumns.JR_VERSION}; 
        	
			String selectClause = FormsColumns.SOURCE + "='" + Utilities.getSource() + "' or " + 
					FormsColumns.SOURCE + " is null";
        	
        	final ContentResolver resolver = Collect.getInstance().getContentResolver();
        	c = resolver.query(FormsColumns.CONTENT_URI, proj, selectClause, null, null);
            
        	ArrayList<Long> formsToDelete = new ArrayList<Long> ();
        	if(c.getCount() > 0) {
        		
	        	 
	        	 while(c.moveToNext()) {
		        	 Long table_id = c.getLong(c.getColumnIndex(FormsColumns._ID));
		             String formId = c.getString(c.getColumnIndex(FormsColumns.JR_FORM_ID));
		             String version = c.getString(c.getColumnIndex(FormsColumns.JR_VERSION));

                     Log.i("   Delete Check: ", "Found Form Id: " + formId + " : " + version);

		             // Check to see if this form was downloaded
		             if(formMap.get(formId + "_v_" + version) == null) {
		            	 Log.i("   Delete: ", "Candidate 1");
		            	 if(!isIncompleteInstance(formId, version)) {
		            		 Log.i("   Delete: ", "Candidate 2 !!!!!!!!!!!");
		            		 formsToDelete.add(table_id);
		            	 }
		             } else {
		            	 Log.i("   Don't Delete: ", "Keep this one");
		             }
	        	 }
	        	 if(formsToDelete.size() > 0) {
	        			
	        			Long[] formArray = formsToDelete.toArray(new Long[formsToDelete.size()]);
	        			// delete files from database and then from file system
	        			for (int i = 0; i < formArray.length; i++) {
	        				
	        				try {
	        		            Uri deleteForm =
	        		                Uri.withAppendedPath(FormsColumns.CONTENT_URI, formArray[i].toString());
	        		            
	        		            int wasDeleted = resolver.delete(deleteForm, null, null); 
	        		            
	        		            if (wasDeleted > 0) {
	        		            	Collect.getInstance().getActivityLogger().logAction(this, "delete", deleteForm.toString());
	        		            }
	        				} catch ( Exception ex ) {
	        					Log.e("Error deleting forms: "," during delete of: " + formArray[i].toString() + " exception: "  + ex.toString());
	        					results.put("Error " + formArray[i].toString() + ": ", " during delete of form "  + " : " + ex.toString());
	        				}
	        		    } 
	
	        	 }
             
        	} 
		 } catch (Throwable e) {
       		 Log.e("ManageForm", "Error: " + e.getMessage());
    	 }
		c.close();
         
    }
    
    /*
	 * Parameters
	 *   formId:  	Stored as jrFormId in the forms database.
	 *   			Extracted by odk from the id attribute on top level data element in the downloaded xml
	 *   formURL:	URL to download the form
	 *   			Not stored
	 *   instanceDataURL:
	 *   			URL to download the data.
	 *   			Not stored
	 *    
	 */
    public ManageFormResponse insertInstance(TaskAssignment ta, long assignmentId, String source, String serverUrl, int version) {

        String formId = ta.task.form_id;
        int formVersion = ta.task.form_version;
        String initialDataURL = ta.task.initial_data;

        String instancePath = null;
        String formVersionString = String.valueOf(formVersion);	
        
        ManageFormResponse mfResponse = new ManageFormResponse();
        
    	ManageFormDetails fd = getFormDetails(formId, formVersionString, source);    // Get the form details
		
    	if(fd.exists) {
         
	  		 // Get the instance path
	         instancePath = getInstancePath(fd.formPath, assignmentId);
	         if(instancePath != null && initialDataURL != null) {
	        	 File f = new File(instancePath);
                 try {
                     Utilities.downloadInstanceFile(f, initialDataURL, serverUrl, formId, version);
                 } catch (Exception e) {
                     e.printStackTrace();
                     mfResponse.isError = true;
                     mfResponse.statusMsg = "Unable to download initial data from " + initialDataURL + " into file: "
                             + instancePath + " " + e.getMessage();
                     return mfResponse;
                 }

	         }

            if(ta.task.title == null) {
                ta.task.title = "local: " + STFileUtils.getName(fd.formPath);
            }
			    
	         // Write the new instance entry into the instance content provider
	         try {
	        	 mfResponse.mUri = writeInstanceDatabase(formId, formVersionString, fd.formName, fd.submissionUri,
                         instancePath, ta, fd.formPath);
	         } catch (Throwable e) {
	        	 e.printStackTrace();
	       		 mfResponse.isError = true;
	    		 mfResponse.statusMsg = "Unable to insert instance " + formId + " into instance database.";
	        	 return mfResponse;
	         }
    	} else {
            mfResponse.isError = true;
    	}
         
         mfResponse.isError = false;
         mfResponse.formPath = fd.formPath;
         mfResponse.instancePath = instancePath;
         return mfResponse;
    }
    
    private Uri writeInstanceDatabase(String jrformid, String jrVersion, String formName, 
			String submissionUri, String instancePath, TaskAssignment ta, String formPath) throws Throwable {
    
    	ContentValues values = new ContentValues();
	 
    	values.put(InstanceColumns.JR_FORM_ID, jrformid);
    	values.put(InstanceColumns.SOURCE, Utilities.getSource());
    	values.put(InstanceColumns.JR_VERSION, jrVersion);
    	values.put(InstanceColumns.SUBMISSION_URI, submissionUri);
    	values.put(InstanceColumns.INSTANCE_FILE_PATH, instancePath);
    	values.put(InstanceColumns.DISPLAY_NAME, formName);
    	values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);

        values.put(InstanceColumns.ACT_LON, 0.0);
        values.put(InstanceColumns.ACT_LAT, 0.0);
        values.put(InstanceColumns.T_TITLE, ta.task.title);
        values.put(InstanceColumns.T_ASS_ID, ta.assignment.assignment_id);
        values.put(InstanceColumns.T_TASK_STATUS, ta.assignment.assignment_status);
		values.put(InstanceColumns.T_REPEAT, ta.task.repeat ? 1 : 0);
		values.put(InstanceColumns.T_UPDATEID, ta.task.update_id);
		values.put(InstanceColumns.T_LOCATION_TRIGGER, ta.task.location_trigger);
        if(ta.task.scheduled_at != null) {
            values.put(InstanceColumns.T_SCHED_START, ta.task.scheduled_at.getTime());
        }
        values.put(InstanceColumns.FORM_PATH, formPath);
        values.put(InstanceColumns.T_ADDRESS, ta.task.address);
        values.put(InstanceColumns.T_IS_SYNC, InstanceProviderAPI.STATUS_SYNC_YES);

        // Add target location
        if (ta.location != null && ta.location.geometry != null && ta.location.geometry.coordinates != null && ta.location.geometry.coordinates.length >= 1) {
            // Set the location of the task to the first coordinate pair
            String firstCoord = ta.location.geometry.coordinates[0];
            String [] fc = firstCoord.split(" ");
            if(fc.length > 1) {
                values.put(InstanceColumns.SCHED_LON, fc[0]);
                values.put(InstanceColumns.SCHED_LAT, fc[1]);
            }
            StringBuilder builder = new StringBuilder();
            for(String coord : ta.location.geometry.coordinates) {
                builder.append(coord);
                builder.append(",");
            }
            values.put(InstanceColumns.T_GEOM, builder.toString());
            values.put(InstanceColumns.T_GEOM_TYPE, ta.location.geometry.type);
        }


        //values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(false));
	
    	return Collect.getInstance().getContentResolver()
    			.insert(InstanceColumns.CONTENT_URI, values);
    }
    
    /*
     * Instance path is based on basepath, filename, timestamp and the task id
     * Paramters
     *  formPath:   Used to obtain the filename
     *  assignment_id:	    Used to guarantee uniqueness when multiple tasks for the same form are assigned
     */
    public String getInstancePath(String formPath, long assignmentId) {
        String instancePath = null;
        
        if(formPath != null) {
	    	String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
	                        .format(Calendar.getInstance().getTime());
            String file =
                formPath.substring(formPath.lastIndexOf('/') + 1, formPath.lastIndexOf('.'));
            String path = Collect.INSTANCES_PATH + "/" + file + "_" + time + "_" + assignmentId;
            if (FileUtils.createFolder(path)) {
                instancePath = path + "/" + file + "_" + time + ".xml";
            }
        }
            
        return instancePath;
    }

}
