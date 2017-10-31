/*
 * Copyright (C) 2011 Cloudtec Pty Ltd
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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.Assignment;
import org.odk.collect.android.database.TaskAssignment;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceUploader.Outcome;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.taskModel.FormLocator;
import org.odk.collect.android.taskModel.TaskCompletionInfo;
import org.odk.collect.android.taskModel.TaskResponse;
import org.odk.collect.android.utilities.ManageForm;
import org.odk.collect.android.utilities.ManageForm.ManageFormDetails;
import org.odk.collect.android.utilities.ManageFormResponse;
import org.odk.collect.android.utilities.TraceUtilities;
import org.odk.collect.android.utilities.Utilities;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.NameValuePair;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.client.methods.HttpHead;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.message.BasicNameValuePair;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;

/**
 * Background task for downloading tasks 
 * 
 * @author Neil Penman (neilpenman@gmail.com)
 */
public class DownloadTasksTask extends AsyncTask<Void, String, HashMap<String, String>> {

    static String TAG = "DownloadTasksTask";
	private TaskDownloaderListener mStateListener;
	HashMap<String, String> results = null;
    SharedPreferences sharedPreferences = null;
    ArrayList<TaskEntry> tasks = new ArrayList<TaskEntry>();
    HashMap<Long, TaskStatus> taskMap = new HashMap<Long, TaskStatus>();
    HttpResponse getResponse = null;
    Gson gson = null;
    TaskResponse tr = null;                         // Data returned from the server
    int statusCode;
    String serverUrl = null;                        // Current server
    String source = null;                           // Server name
    String taskURL = null;                          // Url to get tasks
    int count;                                      // Record number of deletes

    String username = null;
    String password = null;
	
	/*
	 * class used to store status of existing tasks in the database and their database id
	 * A hash is created of the data stored in these object to uniquely identify the task
	 */
	
	private class TaskStatus {
		@SuppressWarnings("unused")
		public long tid;
		@SuppressWarnings("unused")
		public String status;
		@SuppressWarnings("unused")
		public boolean keep;
		
		public TaskStatus(long tid, String status) {
			this.tid = tid;
			this.status = status;
			keep = false;
		}
	}

    /*
     * Add a custom date parser as old versions of the server will send an invalid date format
     */
    public class DateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            SimpleDateFormat sdfOld = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            SimpleDateFormat sdfNew = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            sdfOld.setTimeZone(TimeZone.getTimeZone("UTC"));
            sdfNew.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                Timber.i("Date string primitive: " + json.getAsJsonPrimitive().getAsString());
                try {
                    date = sdfNew.parse(json.getAsJsonPrimitive().getAsString());
                } catch (Exception e) {
                    date = sdfOld.parse(json.getAsJsonPrimitive().getAsString());
                }
                Timber.i("Parsed date: " + date.getTime());
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return date;
        }
    }


    @Override
    public HashMap<String, String> doInBackground(Void... values) {
	
		results = new HashMap<String,String>();
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
        source = Utilities.getSource();
        serverUrl = sharedPreferences.getString(PreferenceKeys.KEY_SERVER_URL, null);
        taskURL = serverUrl + "/surveyKPI/myassignments";

        // Get the username and password
        username = sharedPreferences.getString(PreferenceKeys.KEY_USERNAME, null);
        password = sharedPreferences.getString(PreferenceKeys.KEY_PASSWORD, null);

        // Should mostly work may be better to add a lock however any error is recoverable
        if(Collect.getInstance().isDownloading()) {
            return null;
        } else {
            Collect.getInstance().setDownloading(true);
        }

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotifyMgr =
                (NotificationManager) Collect.getInstance().getBaseContext().getSystemService(NOTIFICATION_SERVICE);

        // Set refresh notification icon
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Collect.getInstance().getBaseContext())
                        .setSmallIcon(R.drawable.notification_icon_go)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setProgress(0, 0, true)
                        .setContentTitle(Collect.getInstance().getBaseContext().getString(R.string.app_name))
                        .setContentText(Collect.getInstance().getBaseContext().getString(R.string.smap_refresh_started));
        mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());

        synchronise();      // Synchronise the phone with the server

        // Refresh task list
        Intent intent = new Intent("org.smap.smapTask.refresh");
        LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);

        // Set refresh done notification icon
        StringBuilder message = Utilities.getUploadMessage(results);

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_KEY, message.toString().trim());
        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder =
                new NotificationCompat.Builder(Collect.getInstance().getBaseContext())
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                R.drawable.ic_launcher))
                        .setContentTitle(Collect.getInstance().getBaseContext().getString(R.string.app_name))
                        .setProgress(0,0,false)
                        .setSound(uri)
                        .setContentIntent(pendingNotify)
                        .setContentText(message.toString().trim());
        mNotifyMgr.notify(NotificationActivity.NOTIFICATION_ID, mBuilder.build());

        Collect.getInstance().setDownloading(false);

        return results;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.taskDownloadingComplete(value);
            }
        }
    }

    /*
     * Clean up after cancel
     */
    @Override
    protected void onCancelled() {

    }

    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null && values.length > 0) {
                mStateListener.progressUpdate(values[0]);
            }
        }

    }

    public void setDownloaderListener(TaskDownloaderListener sl, Context context) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
  
  
    /*
     * Synchronise the tasks stored on the phone with those on the server
     */
    private void synchronise() {

    	Timber.i("Synchronise()");
        
        if(source != null) {
	        try {

                /*
                 * Delete tasks which were cancelled on the phone and and
                 *  have been synchronised with the server
                 */
                count = Utilities.deleteTasksWithStatus(Utilities.STATUS_T_CANCELLED);
                if(count > 0) {
                    results.put(Collect.getInstance().getString(R.string.smap_cancelled), count +
                            " " + Collect.getInstance().getString(R.string.smap_deleted));
                }

                /*
                 * Mark closed any surveys that were submitted last time and not deleted
                 */
                Utilities.closeTasksWithStatus(Utilities.STATUS_T_SUBMITTED);

	            if(isCancelled()) { throw new CancelException("cancelled"); };		// Return if the user cancels

                /*
                 * Submit any completed forms
                 */
                Outcome submitOutcome = submitCompletedForms();
                if(submitOutcome != null && submitOutcome.results != null) {
                    for (String key : submitOutcome.results.keySet()) {
                        results.put(key, submitOutcome.results.get(key));
                    }
                }

                /*
                 * Get an array of the existing server tasks on the phone and create a hashmap indexed on the assignment id
                 */
                Utilities.getTasks(tasks, false, "", "", true);
                for(TaskEntry t : tasks) {
                    TaskStatus ts = new TaskStatus(t.assId, t.taskStatus);
                    taskMap.put(t.assId, ts);
                }

                /*
	        	 * Get new forms and tasks from the server
	        	 */
                publishProgress(Collect.getInstance().getString(R.string.smap_new_forms));

                if(taskURL.startsWith("null")) {
                    throw new Exception(Collect.getInstance().getString(R.string.smap_no_server));
                }
                HttpContext localContext = Collect.getInstance().getHttpContext();
                HttpClient client = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
                Uri u = Uri.parse(taskURL);
                if(username != null && password != null) {
                    WebUtils.addCredentials(username, password, u.getHost());
                }

                URL url = new URL(taskURL);
                URI uri = url.toURI();
                HttpGet req = new HttpGet();
                req.setURI(uri);

                HttpResponse response = client.execute(req, localContext);
                int statusCode = response.getStatusLine().getStatusCode();

                InputStream is = null;
                if(statusCode != HttpStatus.SC_OK) {
                    Timber.w("Error:" + statusCode + " for URL " + taskURL);
                    results.put(Collect.getInstance().getString(R.string.smap_get_tasks),
                            Utilities.translateMsg(response.getStatusLine().getReasonPhrase()));
                    throw new Exception(response.getStatusLine().getReasonPhrase());
                }

                try {
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    // De-serialise
                    GsonBuilder gb = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer());
                    gson = gb.create();
                    Reader isReader = new InputStreamReader(is);
                    tr = gson.fromJson(isReader, TaskResponse.class);
                    Timber.i("Message:" + tr.message);
                } finally {
                    if (is != null) {
                        try {
                            // ensure stream is consumed...
                            final long count = 1024L;
                            while (is.skip(count) == count)
                                ;
                        } catch (Exception e) {
                            // no-op
                        }
                        try {
                            is.close();
                        } catch (Exception e) {
                        }
                    }
                }

                if(isCancelled()) { throw new CancelException("cancelled"); };		// Return if the user cancels

                if(tr.settings !=null ) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PreferenceKeys.KEY_STORE_SMAP_USER_TRAIL, tr.settings.ft_send_trail);
                    editor.putBoolean(PreferenceKeys.KEY_SMAP_LOCATION_TRIGGER, tr.settings.ft_location_trigger);
                    editor.putBoolean(PreferenceKeys.KEY_SMAP_ODK_STYLE_MENUS, tr.settings.ft_odk_style_menus);
                    editor.putBoolean(PreferenceKeys.KEY_SMAP_REVIEW_FINAL, tr.settings.ft_review_final);
                    editor.putBoolean(PreferenceKeys.KEY_SMAP_AUTOSEND_WIFI, tr.settings.ft_send_wifi);
                    editor.putBoolean(PreferenceKeys.KEY_SMAP_AUTOSEND_WIFI_CELL, tr.settings.ft_send_wifi_cell);

                    // update settings in phone app that are over ridden by the server (Only overridden to be enabled
                    // TODO allow the server to force auto send off
                    String autoSend = (String) GeneralSharedPreferences.getInstance().get(KEY_AUTOSEND);
                    if (tr.settings.ft_send_wifi_cell) {
                        autoSend = "wifi_and_cellular";
                    } else if (tr.settings.ft_send_wifi) {
                        autoSend = "wifi_only";
                    }
                    GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_AUTOSEND, autoSend);

                    // Update settings in phone for delete after submit
                    editor.putBoolean(PreferenceKeys.KEY_DELETE_AFTER_SEND, tr.settings.ft_delete_submitted);

                    editor.apply();
                }

                /*
                 * Synchronise forms
                 *  Get any forms the user does not currently have
                 *  Delete any forms that are no longer accessible to the user
                 */
                HashMap<FormDetails, String> outcome = synchroniseForms(tr.forms);
                if(outcome != null) {
                    for (FormDetails key : outcome.keySet()) {
                        results.put(key.formName, outcome.get(key));
                    }
                }

                if(isCancelled()) { throw new CancelException("cancelled"); };		// Return if the user cancels

                /*
                 * Apply task changes
                 *  Add new tasks
                 *  Update the status of tasks on the phone that have been cancelled on the server
                 */
                addAndUpdateEntries();

            	/*
            	 * Notify the server of the phone state
            	 *  (1) Update on the server all tasks that have a status of "accepted", "rejected" or "submitted" or "cancelled" or "completed"
            	 *      Note in the case of "cancelled" the client is merely acknowledging that it received the cancellation notice
            	 *  (2) Pass the list of forms and versions that have been applied back to the server
            	 */
		        updateTaskStatusToServer();

                if(isCancelled()) { throw new CancelException("cancelled"); };		// Return if the user cancels

            	/*
            	 * Delete all entries in the database that we are finished with
            	 */
                count = Utilities.deleteTasksWithStatus(Utilities.STATUS_T_REJECTED);
                if(count > 0) {
                    results.put(Collect.getInstance().getString(R.string.smap_rejected), count +
                            " " + Collect.getInstance().getString(R.string.smap_deleted));
                }

                if(tr.settings !=null && tr.settings.ft_delete_submitted) {
                    count = Utilities.deleteTasksWithStatus(Utilities.STATUS_T_SUBMITTED);
                    if(count > 0) {
                        results.put(Collect.getInstance().getString(R.string.smap_submitted), count +
                                " " + Collect.getInstance().getString(R.string.smap_deleted));
                    }
                    count = Utilities.deleteTasksWithStatus(Utilities.STATUS_T_CLOSED);
                    if(count > 0) {
                        results.put(Collect.getInstance().getString(R.string.smap_closed), count +
                                " " + Collect.getInstance().getString(R.string.smap_deleted));
                    }
                }

	        } catch(JsonSyntaxException e) {
	        	
	        	Timber.e("JSON Syntax Error:" + " for URL " + taskURL);
	        	publishProgress(e.getMessage());
	        	e.printStackTrace();
	        	results.put(Collect.getInstance().getString(R.string.smap_error) + ":", e.getMessage());
	        	
	        } catch (CancelException e) {	
	        	
	        	Timber.i("Info: Download cancelled by user.");

	        } catch (Exception e) {	
	        	
	        	Timber.e("Error:" + " for URL " + taskURL);
	        	e.printStackTrace();
	        	publishProgress(e.getMessage());
                String msg = Utilities.translateMsg(e.getMessage());
	        	results.put(Collect.getInstance().getString(R.string.smap_error) + ":", msg);
	
	        }
        }

        
  
    }

    private Outcome submitCompletedForms() {
       
        String selection = InstanceColumns.SOURCE + "=? and (" + InstanceColumns.STATUS + "=? or " + 
        		InstanceColumns.STATUS + "=?)";
        String selectionArgs[] = {
        		Utilities.getSource(),
        		InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED                 
            };

        ArrayList<Long> toUpload = new ArrayList<Long>();
        Cursor c = null;
        try {
            c = Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection,
                selectionArgs, null);
            
            if (c != null && c.getCount() > 0) {
                c.move(-1);
                while (c.moveToNext()) {
                    Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                    toUpload.add(Long.valueOf(l));
                }
            }

            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            if(toUpload.size() > 0) {
                InstanceServerUploader instanceUploaderTask = new InstanceServerUploader();
                publishProgress(Collect.getInstance().getString(R.string.smap_submitting, toUpload.size()));
                instanceUploaderTask.setUploaderListener((InstanceUploaderListener) mStateListener);

                Long[] toSendArray = new Long[toUpload.size()];
                toUpload.toArray(toSendArray);
                Timber.i("Submitting " + toUpload.size() + " finalised surveys");

                Outcome o = instanceUploaderTask.doInBackground(toSendArray);	// Already running a background task so call direct
            	instanceUploaderTask.onPostExecute(o);
                return o;
            } else {
            	return null;
            }
        
    }

    /*
	 * Loop through the task entries in the database
	 *  (1) Update on the server all that have a status of "accepted", "rejected" or "submitted"
	 *  (2) Send details on submitted tasks, such as where they were completed and optionally the trace of user movements, to the server
	 */
	private void updateTaskStatusToServer() throws Exception {


        TaskResponse updateResponse = new TaskResponse();
        updateResponse.forms = tr.forms;
        
        // Add device id to response
        updateResponse.deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                .getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);

        // Get tasks that have not been synchronised
        ArrayList<TaskEntry> nonSynchTasks = new ArrayList<TaskEntry>();
        Utilities.getTasks(nonSynchTasks, true, "", "", true);

        /*
         * Set updates to task status
         */
        updateResponse.taskAssignments = new ArrayList<TaskAssignment> ();          // Updates to task status

        for(TaskEntry t : nonSynchTasks) {
  	  		if(t.taskStatus != null && t.isSynced.equals(Utilities.STATUS_SYNC_NO)) {
  	  			TaskAssignment ta = new TaskAssignment();
  	  			ta.assignment = new Assignment();
  	  			ta.assignment.assignment_id = (int) t.assId;
  	  			ta.assignment.dbId = (int) t.id;
  	  			ta.assignment.assignment_status = t.taskStatus;

	            updateResponse.taskAssignments.add(ta);
  	  		}
        }

        /*
         * Set details on submitted tasks
         */
        if(tr.settings != null && tr.settings.ft_send_trail) {
            updateResponse.taskCompletionInfo = new ArrayList<TaskCompletionInfo>();   // Details on completed tasks

            for (TaskEntry t : nonSynchTasks) {
                if ((t.taskStatus.equals(Utilities.STATUS_T_SUBMITTED) || t.taskStatus.equals(Utilities.STATUS_T_CLOSED))
                        && t.isSynced.equals(Utilities.STATUS_SYNC_NO)) {
                    TaskCompletionInfo tci = new TaskCompletionInfo();
                    tci.actFinish = t.actFinish;
                    tci.lat = t.actLat;
                    tci.lon = t.actLon;
                    tci.ident = t.ident;
                    tci.uuid = t.uuid;

                    updateResponse.taskCompletionInfo.add(tci);
                }
            }

            // Get Points
            updateResponse.userTrail = new ArrayList<PointEntry>(100);
            TraceUtilities.getPoints(updateResponse.userTrail);
        }

        if(updateResponse.taskAssignments.size() > 0 ||
                (updateResponse.taskCompletionInfo != null &&
                        (updateResponse.taskCompletionInfo.size() > 0 || updateResponse.userTrail.size() > 0))) {

            publishProgress(Collect.getInstance().getString(R.string.smap_update_task_status));


            // Call the service
            HttpContext localContext = Collect.getInstance().getHttpContext();
            HttpClient client = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);
            Uri u = Uri.parse(taskURL);
            if (username != null && password != null) {
                WebUtils.addCredentials(username, password, u.getHost());
            }

            /*
             * Use a head request as per instance uploader
             * This will set the authentication as digest and set the nonce
             *
             * There must be a more efficient way to do this!
             */
            HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);
            try {
                client.execute(httpHead, localContext);
            } catch (Exception e) {

            }

            HttpPost postRequest = new HttpPost(URI.create(u.toString()));

            Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd").create();
            String resp = gson.toJson(updateResponse);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("assignInput", resp));
            postRequest.setEntity(new UrlEncodedFormEntity(dataToSend));

            HttpResponse response = null;
            int statusCode = 0;
            try {
                response = client.execute(postRequest, localContext);
                statusCode = response.getStatusLine().getStatusCode();
            } catch (Exception e) {

            }

            if (statusCode != HttpStatus.SC_OK) {
                Timber.e("Error:" + statusCode + " for URL " + taskURL);
                results.put(Collect.getInstance().getString(R.string.smap_get_tasks),
                        Utilities.translateMsg(response.getStatusLine().getReasonPhrase()));
                WebUtils.discardEntityBytes(response);
                throw new Exception(response.getStatusLine().getReasonPhrase());
            }
            WebUtils.discardEntityBytes(response);

            for (TaskAssignment ta : updateResponse.taskAssignments) {
                Utilities.setTaskSynchronized((long) ta.assignment.dbId);        // Mark the task status as synchronised
            }
            TraceUtilities.deleteSource();
        }
	}
	
	/*
     * Loop through the entries from the source
     *   (1) Add entries that have a status of "new", "pending" or "accepted" and are not already on the phone
     *   (2) Update the status of database entries where the source status is set to "cancelled"
     */
	private void addAndUpdateEntries() throws Exception {

    	if(tr.taskAssignments != null) {
            int count = 1;
        	for(TaskAssignment ta : tr.taskAssignments) {

                if(isCancelled()) { throw new CancelException("cancelled"); };		// Return if the user cancels
	            
        		if(ta.task.type.equals("xform")) {
        			Assignment assignment = ta.assignment;
        			
    				Timber.i("Task: " + assignment.assignment_id + " Status:" +
    						assignment.assignment_status + " Mode:" + ta.task.assignment_mode +
    						" Address: " + ta.task.address +
                            " NFC: " + ta.task.location_trigger +
    						" Form: " + ta.task.form_id + " version: " + ta.task.form_version + 
    						" Type: " + ta.task.type + "Assignee: " + assignment.assignee + "Username: " + username);
            		

            		// Find out if this task is already on the phone
	          	  	TaskStatus ts = taskMap.get(Long.valueOf((long) assignment.assignment_id));
	          	  	if(ts == null) {
	          	  		Timber.i("New task: " + assignment.assignment_id);
	          	  		// New task
	          	  		if(assignment.assignment_status.equals(Utilities.STATUS_T_ACCEPTED)) {

                            // Ensure the instance data is available on the phone
                            // Use update_id in preference to initial_data url
                            if(ta.task.update_id != null) {
                                if(tr.version < 1) {
                                    ta.task.initial_data = serverUrl + "/instanceXML/" +
                                            ta.task.form_id + "/0?key=instanceid&keyval=" + ta.task.update_id;
                                } else {
                                    ta.task.initial_data = serverUrl + "/webForm/instance/" +
                                            ta.task.form_id + "/" + ta.task.update_id;
                                }
                                Timber.i("Instance url: " + ta.task.initial_data);
                            } else {
                                // Make sure the initial_data url is sensible (ie null or a URL
                                if (ta.task.initial_data != null && !ta.task.initial_data.startsWith("http")) {
                                    ta.task.initial_data = null;
                                }
                            }
	                		
	          	  			// Add instance data
	          	  			ManageForm mf = new ManageForm();
	          	  			ManageFormResponse mfr = mf.insertInstance(ta, assignment.assignment_id, source, serverUrl, tr.version);
	          	  			if(!mfr.isError) {
	          	  				results.put(ta.task.title, Collect.getInstance().getString(R.string.smap_created));
                                publishProgress(ta.task.title, Integer.valueOf(count).toString(), Integer.valueOf(tr.taskAssignments.size())
                                        .toString());
	          	  			} else {
                                publishProgress(ta.task.title + " : Failed", Integer.valueOf(count).toString(), Integer.valueOf(tr.taskAssignments.size())
                                        .toString());
	          	  				results.put(ta.task.title, "Creation failed: " + mfr.statusMsg );
	          	  			}

	          	  		}
	          	  	} else {        	// Existing task
	          	  		Timber.i("Existing Task: " + assignment.assignment_id + " : " + assignment.assignment_status);

	          	  		if(assignment.assignment_status.equals(Utilities.STATUS_T_CANCELLED) && !ts.status.equals(Utilities.STATUS_T_CANCELLED)) {
                            Utilities.setStatusForAssignment(assignment.assignment_id, assignment.assignment_status);
                            results.put(ta.task.title, assignment.assignment_status);
	          	  		}

	          	  		// Update the task if its status is not incomplete
                        Utilities.updateParametersForAssignment(assignment.assignment_id, ta);
	          	  	}

        			
        		}// end process for xform task
        	}// end tasks loop
    	}

        // Remove any tasks that have been deleted from the server
        Utilities.deleteObsoleteTasks(tr.taskAssignments);
    	
    	return;
	}
	
	/*
     * Synchronise the forms on the server with those on the phone
     *   (1) Download forms on the server that are not on the phone
     *   (2) Delete forms not on the server or older versions of forms
     *       unless there is an uncompleted data instance using that form
     */
	private HashMap<FormDetails, String> synchroniseForms(List<FormLocator> forms) throws Exception {
    	

		HashMap<FormDetails, String> dfResults = null;
    	
    	if(forms == null) {
        	publishProgress(Collect.getInstance().getString(R.string.smap_no_forms));
    	} else {
    		
    		HashMap <String, String> formMap = new HashMap <String, String> ();
          	ManageForm mf = new ManageForm();
    		ArrayList<FormDetails> toDownload = new ArrayList<FormDetails> ();
    		
    		// Create an array of ODK form details
        	for(FormLocator form : forms) {
        		String formVersionString = String.valueOf(form.version);
        		ManageFormDetails mfd = mf.getFormDetails(form.ident, formVersionString, source);    // Get the form details
                Timber.i("+++ Form: " + form.ident + ":" + formVersionString);
        		if(!mfd.exists || form.dirty) {
                    Timber.i("+++ Form does not exist or is dirty: " + form.ident + ":" + formVersionString +
                            " dirty: " + form.dirty);
        			form.url = serverUrl + "/formXML?key=" + form.ident;	// Set the form url from the server address and form ident
        			if(form.hasManifest) {
        				form.manifestUrl = serverUrl + "/xformsManifest?key=" + form.ident;
        			}
        			
        			FormDetails fd = new FormDetails(form.name, form.url, form.manifestUrl, form.ident, formVersionString, form.tasks_only);
        			toDownload.add(fd);
        		} else {
                    // Update form details
                    mf.updateFormDetails(mfd.id, form.name, form.tasks_only);
                }

        		// Store a hashmap of new forms so we can delete existing forms not in the list
        		String entryHash = form.ident + "_v_" + form.version;
        		formMap.put(entryHash, entryHash);
        	}

            if(toDownload.size() > 0) {
                DownloadFormsTask downloadFormsTask = new DownloadFormsTask();
                publishProgress(Collect.getInstance().getString(R.string.smap_downloading, toDownload.size()));

                Timber.i("Downloading " + toDownload.size() + " forms");
                downloadFormsTask.setDownloaderListener((FormDownloaderListener) mStateListener);
                dfResults = downloadFormsTask.doInBackground(toDownload);   // Not in background as called directly
            }

          	// Delete any forms no longer required
        	mf.deleteForms(formMap, results);
    	}
    	
    	return dfResults;
	}

	/*
     * Return true if the passed in instance file is in the odk instance database
     * Assume that if it has been deleted from the database then it can't be sent although
     * it is probably still on the sdcard
     */
    boolean instanceExists(String instancePath) {
    	boolean exists = true;
    	
    	// Get the provider URI of the instance 
        String where = InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] whereArgs = {
            instancePath
        };
        
    	ContentResolver cr = Collect.getInstance().getContentResolver();
		Cursor cInstanceProvider = cr.query(InstanceColumns.CONTENT_URI, 
				null, where, whereArgs, null);
		if(cInstanceProvider.getCount() != 1) {
			Timber.e("Unique instance not found: count is:" +
					cInstanceProvider.getCount());
			exists = false;
		}
		cInstanceProvider.close();
    	return exists;
    }
    
}
