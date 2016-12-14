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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TaskAssignment;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.STFileUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.Header;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.taskModel.InstanceXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
public class Utilities {

    // Valid values for task status
    public static final String STATUS_T_ACCEPTED = "accepted";
    public static final String STATUS_T_REJECTED = "rejected";
    public static final String STATUS_T_COMPLETE = "complete";
    public static final String STATUS_T_SUBMITTED = "submitted";
    public static final String STATUS_T_CANCELLED = "cancelled";
    public static final String STATUS_T_CLOSED = "closed";

    // Valid values for is synced
    public static final String STATUS_SYNC_YES = "synchronized";
    public static final String STATUS_SYNC_NO = "not synchronized";
	
	// Get the task source
	public static String getSource() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(Collect.getInstance()
						.getBaseContext());
		String serverUrl = settings.getString(
                PreferencesActivity.KEY_SERVER_URL, null);
		String source = STFileUtils.getSource(serverUrl);
		
		

		return source;
	}

    public static TaskEntry getTaskWithIdOrPath(long id, String instancePath) {

        TaskEntry entry = null;

        // Get cursor
        String [] proj = {
                InstanceColumns._ID,
                InstanceColumns.T_TITLE,
                InstanceColumns.T_TASK_STATUS,
                InstanceColumns.T_SCHED_START,
                InstanceColumns.T_ADDRESS,
                InstanceColumns.FORM_PATH,
                InstanceColumns.JR_FORM_ID,
                InstanceColumns.INSTANCE_FILE_PATH,
                InstanceColumns.SCHED_LON,
                InstanceColumns.SCHED_LAT,
                InstanceColumns.ACT_LON,
                InstanceColumns.ACT_LAT,
                InstanceColumns.T_ACT_FINISH,
                InstanceColumns.T_IS_SYNC,
                InstanceColumns.T_ASS_ID,
                InstanceColumns.UUID

        };

        String selectClause = InstanceColumns._ID + " = " + id;
        if(instancePath != null) {
            selectClause = InstanceColumns.INSTANCE_FILE_PATH + " = '" + instancePath + "'";
        }


        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor c = resolver.query(InstanceColumns.CONTENT_URI, proj, selectClause, null, null);

        try {
            c.moveToFirst();
            DateFormat dFormat = DateFormat.getDateTimeInstance();


            entry = new TaskEntry();

            entry.type = "task";
            entry.id = c.getLong(c.getColumnIndex(InstanceColumns._ID));
            entry.name = c.getString(c.getColumnIndex(InstanceColumns.T_TITLE));
            entry.taskStatus = c.getString(c.getColumnIndex(InstanceColumns.T_TASK_STATUS));
            entry.repeat = (c.getInt(c.getColumnIndex(InstanceColumns.T_REPEAT)) > 0);
            entry.taskStart = c.getLong(c.getColumnIndex(InstanceColumns.T_SCHED_START));
            entry.taskAddress = c.getString(c.getColumnIndex(InstanceColumns.T_ADDRESS));
            entry.taskForm = c.getString(c.getColumnIndex(InstanceColumns.FORM_PATH));
            entry.jrFormId = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
            entry.instancePath = c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
            entry.schedLon = c.getDouble(c.getColumnIndex(InstanceColumns.SCHED_LON));
            entry.schedLat = c.getDouble(c.getColumnIndex(InstanceColumns.SCHED_LAT));
            entry.actLon = c.getDouble(c.getColumnIndex(InstanceColumns.ACT_LON));
            entry.actLat = c.getDouble(c.getColumnIndex(InstanceColumns.ACT_LAT));
            entry.actFinish = c.getLong(c.getColumnIndex(InstanceColumns.T_ACT_FINISH));
            entry.isSynced = c.getString(c.getColumnIndex(InstanceColumns.T_IS_SYNC));
            // entry.taskId = c.getLong(c.getColumnIndex(InstanceColumns.T_TASK_ID));
            entry.uuid = c.getString(c.getColumnIndex(InstanceColumns.UUID));

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
        }

        return entry;
    }

    public static void duplicateTask(String originalInstancePath, String newInstancePath, TaskEntry entry) {


        try {

            ContentValues values = new ContentValues();
            values.put(InstanceColumns.T_TITLE, entry.name);
            values.put(InstanceColumns.DISPLAY_NAME, entry.displayName);
            values.put(InstanceColumns.T_TASK_STATUS, entry.taskStatus);
            values.put(InstanceColumns.STATUS, entry.taskStatus);
            values.put(InstanceColumns.T_REPEAT, true);     // Duplicated task should also be a repeat
            values.put(InstanceColumns.T_SCHED_START, entry.taskStart);
            values.put(InstanceColumns.T_ADDRESS, entry.taskAddress);
            values.put(InstanceColumns.FORM_PATH, entry.taskForm);
            values.put(InstanceColumns.JR_FORM_ID, entry.jrFormId);
            values.put(InstanceColumns.JR_VERSION, entry.formVersion);
            values.put(InstanceColumns.INSTANCE_FILE_PATH, newInstancePath);    // Set the new path
            values.put(InstanceColumns.SCHED_LON, entry.schedLon);
            values.put(InstanceColumns.SCHED_LAT, entry.schedLat);
            values.put(InstanceColumns.SOURCE, entry.source);
            values.put(InstanceColumns.T_LOCATION_TRIGGER, entry.locationTrigger);
            values.put(InstanceColumns.T_ASS_ID, entry.assId);

            final ContentResolver resolver = Collect.getInstance().getContentResolver();
            resolver.insert(InstanceColumns.CONTENT_URI, values);


        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Common routine to download an instance XML document including any attachments
     *
     * Smap Specific
     *
     * @param file        the final file
     * @param downloadUrl the url to get the contents from.
     * @throws Exception
     */
    public static void downloadInstanceFile(File file, String downloadUrl, String serverUrl, String formId, int version) throws Exception {

        String t = "DownloadInstanceFile";

        URI uri;
        try {
            // assume the downloadUrl is escaped properly
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();

        HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);

        // Add credentials
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

        String username = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String password = settings.getString(PreferencesActivity.KEY_PASSWORD, null);

        if(username != null && password != null) {
            Uri u = Uri.parse(downloadUrl);
            WebUtils.addCredentials(username, password, u.getHost());
        }


        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(uri);
        req.addHeader(WebUtils.ACCEPT_ENCODING_HEADER, WebUtils.GZIP_CONTENT_ENCODING);

        HttpResponse response;
        try {
            response = httpclient.execute(req, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                WebUtils.discardEntityBytes(response);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    Collect.getInstance().getCookieStore().clear();
                }
                String errMsg =
                        Collect.getInstance().getString(org.odk.collect.android.R.string.file_fetch_failed, downloadUrl,
                                response.getStatusLine().getReasonPhrase(), statusCode);
                Log.e(t, errMsg);
                throw new Exception(errMsg);
            }

            // Create instance object
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm").create();

            InputStream is = null;
            OutputStream os = null;
            try {
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                Header contentEncoding = entity.getContentEncoding();
                if ( contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(WebUtils.GZIP_CONTENT_ENCODING) ) {
                    is = new GZIPInputStream(is);
                }

                Reader isReader = new InputStreamReader(is);

                os = new FileOutputStream(file);
                byte buf[] = new byte[4096];
                int len;

                if(version < 1) {
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                } else {
                    InstanceXML instance = gson.fromJson(isReader, InstanceXML.class);

                    os.write(instance.instanceStrToEdit.getBytes());

                    if(instance.files != null && instance.files.size() > 0) {
                        for(String media : instance.files) {
                            DownloadFormsTask dft = new DownloadFormsTask();
                            String mediaUrl = serverUrl + "/attachments/" +
                                    formId + "/" + media;
                            String mediaPath = file.getParent() + "/" + media;
                            try {
                                File f = new File(mediaPath);
                                dft.downloadFile(f, mediaUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                os.flush();



            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                    }
                }
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
        } catch (Exception e) {
            Log.e(t, e.toString());
            // silently retry unless this is the last attempt,
            // in which case we rethrow the exception.

            FileUtils.deleteAndReport(file);


            throw e;
        }






    }

    public static void getTasks(ArrayList<TaskEntry> tasks, boolean all_non_synchronised) {

        // Get cursor
        String [] proj = {
                InstanceColumns._ID,
                InstanceColumns.T_TITLE,
                InstanceColumns.DISPLAY_NAME,
                InstanceColumns.T_TASK_STATUS,
                InstanceColumns.T_REPEAT,
                InstanceColumns.T_SCHED_START,
                InstanceColumns.T_ADDRESS,
                InstanceColumns.FORM_PATH,
                InstanceColumns.JR_FORM_ID,
                InstanceColumns.JR_VERSION,
                InstanceColumns.INSTANCE_FILE_PATH,
                InstanceColumns.SCHED_LON,
                InstanceColumns.SCHED_LAT,
                InstanceColumns.ACT_LON,
                InstanceColumns.ACT_LAT,
                InstanceColumns.T_ACT_FINISH,
                InstanceColumns.T_IS_SYNC,
                InstanceColumns.T_ASS_ID,
                InstanceColumns.UUID,
                InstanceColumns.SOURCE,
                InstanceColumns.T_LOCATION_TRIGGER

        };

        String selectClause = null;
        if(all_non_synchronised) {
            selectClause = "(" + InstanceColumns.SOURCE + " = ?" +
                    " or " + InstanceColumns.SOURCE + " = 'local')" +
                    " and " + InstanceColumns.T_IS_SYNC + " = ? ";
        } else {
            selectClause = "(" + InstanceColumns.SOURCE + " = ?" +
                    " or " + InstanceColumns.SOURCE + " = 'local')" +
                    " and " + InstanceColumns.T_TASK_STATUS + " != ? ";
        }

        String [] selectArgs = {"",""};
        selectArgs[0] = Utilities.getSource();
        if(all_non_synchronised) {
            selectArgs[1] = Utilities.STATUS_SYNC_NO;
        } else {
            selectArgs[1] = Utilities.STATUS_T_CLOSED;
        }

        String sortOrder = InstanceColumns.T_SCHED_START + " DESC";

        Cursor c = Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, proj, selectClause, selectArgs, sortOrder);

        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                TaskEntry entry = new TaskEntry();

                entry.type = "task";
                entry.name = c.getString(c.getColumnIndex(InstanceColumns.T_TITLE));
                entry.displayName = c.getString(c.getColumnIndex(InstanceColumns.DISPLAY_NAME));
                entry.taskStatus = c.getString(c.getColumnIndex(InstanceColumns.T_TASK_STATUS));
                entry.repeat = (c.getInt(c.getColumnIndex(InstanceColumns.T_REPEAT)) > 0);
                entry.taskStart = c.getLong(c.getColumnIndex(InstanceColumns.T_SCHED_START));
                entry.taskAddress = c.getString(c.getColumnIndex(InstanceColumns.T_ADDRESS));
                entry.taskForm = c.getString(c.getColumnIndex(InstanceColumns.FORM_PATH));
                entry.jrFormId = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
                entry.formVersion = c.getInt(c.getColumnIndex(InstanceColumns.JR_VERSION));
                entry.instancePath = c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                entry.id = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                entry.schedLon = c.getDouble(c.getColumnIndex(InstanceColumns.SCHED_LON));
                entry.schedLat = c.getDouble(c.getColumnIndex(InstanceColumns.SCHED_LAT));
                entry.actLon = c.getDouble(c.getColumnIndex(InstanceColumns.ACT_LON));
                entry.actLat = c.getDouble(c.getColumnIndex(InstanceColumns.ACT_LAT));
                entry.actFinish = c.getLong(c.getColumnIndex(InstanceColumns.T_ACT_FINISH));
                entry.isSynced = c.getString(c.getColumnIndex(InstanceColumns.T_IS_SYNC));
                entry.assId = c.getLong(c.getColumnIndex(InstanceColumns.T_ASS_ID));
                entry.uuid = c.getString(c.getColumnIndex(InstanceColumns.UUID));
                entry.source = c.getString(c.getColumnIndex(InstanceColumns.SOURCE));
                entry.locationTrigger = c.getString(c.getColumnIndex(InstanceColumns.T_LOCATION_TRIGGER));

                tasks.add(entry);
                c.moveToNext();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
        }

    }

    /*
     * Delete the task
     */
    public static void deleteTask(Long id) {

        Uri taskUri =  Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id.toString());
        final ContentResolver cr = Collect.getInstance().getContentResolver();
        cr.delete(taskUri, null, null);
    }

    /*
     * Delete any tasks with the matching status
     * Only delete if the task status has been successfully synchronised with the server
     */
    public static int deleteTasksWithStatus(String status) {

        Uri dbUri =  InstanceColumns.CONTENT_URI;

        String selectClause = InstanceColumns.T_TASK_STATUS + " = ? and "
                + InstanceColumns.SOURCE + " = ? and "
                + InstanceColumns.T_IS_SYNC + " = ?";

        String [] selectArgs = {"","",""};
        selectArgs[0] = status;
        selectArgs[1] = Utilities.getSource();
        selectArgs[2] = Utilities.STATUS_SYNC_YES;

        return Collect.getInstance().getContentResolver().delete(dbUri, selectClause, selectArgs);
    }

    /*
 * Delete the task
 */
    public static void closeTasksWithStatus(String status) {

        Uri dbUri =  InstanceColumns.CONTENT_URI;

        ContentValues values = new ContentValues();
        values.put(InstanceColumns.T_TASK_STATUS, Utilities.STATUS_T_CLOSED);

        String selectClause = InstanceColumns.T_TASK_STATUS + " = ? and "
                + InstanceColumns.SOURCE + "= ? ";

        String [] selectArgs = {"",""};
        selectArgs[0] = status;
        selectArgs[1] = Utilities.getSource();

        Collect.getInstance().getContentResolver().update(dbUri, values, selectClause, selectArgs);

    }

    /*
     * Mark the task as synchronised
     */
    public static void setTaskSynchronized(Long id) {

        Uri taskUri =  Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id.toString());

        ContentValues values = new ContentValues();
        values.put(InstanceColumns.T_IS_SYNC, STATUS_SYNC_YES);

        Collect.getInstance().getContentResolver().update(taskUri, values, null, null);

    }

    /*
     * Mark the status of a task
     */
    public static void setStatusForTask(Long id, String status) {

        Uri taskUri =  Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id.toString());

        ContentValues values = new ContentValues();
        values.put(InstanceColumns.T_TASK_STATUS, status);
        values.put(InstanceColumns.T_IS_SYNC, Utilities.STATUS_SYNC_NO);

        Collect.getInstance().getContentResolver().update(taskUri, values, null, null);

    }



 /*
 * Set the status for the provided assignment id
 */
    public static void setStatusForAssignment(long assId, String status) {

        Uri dbUri =  InstanceColumns.CONTENT_URI;

        String selectClause = InstanceColumns.T_ASS_ID + " = " + assId + " and "
                + InstanceColumns.SOURCE + " = ?";


        String [] selectArgs = {""};
        selectArgs[0] = Utilities.getSource();

        ContentValues values = new ContentValues();
        values.put(InstanceColumns.T_TASK_STATUS, status);

        Collect.getInstance().getContentResolver().update(dbUri, values, selectClause, selectArgs);

    }

    /*
     * Set the status for the provided assignment id
     */
    public static void updateParametersForAssignment(long assId, TaskAssignment ta) {

        Uri dbUri =  InstanceColumns.CONTENT_URI;

        String selectClause = InstanceColumns.T_ASS_ID + " = " + assId + " and "
                + InstanceColumns.SOURCE + " = ?";


        String [] selectArgs = {""};
        selectArgs[0] = Utilities.getSource();

        ContentValues values = new ContentValues();
        values.put(InstanceColumns.T_REPEAT, ta.task.repeat ? 1 : 0);
        if(ta.task.scheduled_at != null) {
            values.put(InstanceColumns.T_SCHED_START, ta.task.scheduled_at.getTime());
        }
        values.put(InstanceColumns.T_LOCATION_TRIGGER, ta.task.location_trigger);

        Collect.getInstance().getContentResolver().update(dbUri, values, selectClause, selectArgs);

    }

    /*
     * Return true if the current task status allows it to be rejected
     */
    public static boolean canReject(String currentStatus) {
        boolean valid = false;
        if(currentStatus.equals(Utilities.STATUS_T_ACCEPTED)) {
            valid = true;
        }
        return valid;
    }

    /*
     * Return true if the current task status allows it to be completed
     */
    public static boolean canComplete(String currentStatus) {

        boolean valid = false;
        if(currentStatus.equals(STATUS_T_ACCEPTED)) {
            valid = true;
        }

        return valid;
    }

    /*
     * Return true if the current task status allows it to be accepted
     */
    public static boolean canAccept(String currentStatus) {
        boolean valid = false;
        if(currentStatus.equals(STATUS_T_REJECTED)) {
            valid = true;
        }

        return valid;
    }

    /*
     * Copy instance files to a new location
     */
    public static void copyInstanceFiles(String fromInstancePath, String toInstancePath) {
        String fromFolderPath = fromInstancePath.substring(0, fromInstancePath.lastIndexOf('/'));   // InstancePaths are for the XML files
        String toFolderPath = toInstancePath.substring(0, toInstancePath.lastIndexOf('/'));   // InstancePaths are for the XML files

        File fromInstanceFolder = new File(fromFolderPath);
        File toInstanceFolder = new File(toFolderPath);
        toInstanceFolder.mkdir();
        File[] instanceFiles = fromInstanceFolder.listFiles();
        if (instanceFiles != null && instanceFiles.length > 0) {
            for (File f : instanceFiles) {
                try {
                    String fileName = f.getName();
                    if(fileName.endsWith("xml")) {
                        // Copy xml file to new instance path
                        File toFile = new File(toInstancePath);
                        org.apache.commons.io.FileUtils.copyFile(f, toFile, false);
                    } else {
                        // Copy attachments to new folder
                        org.apache.commons.io.FileUtils.copyFileToDirectory(f, toInstanceFolder, false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
