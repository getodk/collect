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
package org.odk.collect.android.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.R;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.Utilities;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SmapTaskStatusActivity extends CollectAbstractActivity implements OnClickListener {

    @BindView(R.id.reject_reason) EditText rejectReason;

	private class Address {
		String name;
		String value;
	}

    TaskEntry taskEntry = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.smap_task_status);
        ButterKnife.bind(this);
        
        // Get the id of the selected list item
        Bundle bundle = getIntent().getExtras();

        taskEntry = Utilities.getTaskWithIdOrPath(bundle.getLong("id"), null);

    	try {
            
        	// Formatting
   			LinearLayout.LayoutParams textLayout = 
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			textLayout.setMargins(1, 1, 1, 1);
			
			TableRow.LayoutParams trLayout = 
					new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			trLayout.setMargins(1, 1, 1, 1);
            

        	TextView title = (TextView)findViewById(R.id.task_title);       // Add Title
        	title.setText(taskEntry.name);

            TextView status = (TextView)findViewById(R.id.task_status);     // Add Status
            status.setText(getString(R.string.smap_current_status) + "; " + taskEntry.taskStatus);

            // Create the buttons
            LinearLayout buttons = (LinearLayout)findViewById(R.id.task_address_buttons);
            rejectReason.setVisibility(View.GONE);
            boolean triggeredTask = false;
            if (taskEntry.type.equals("task") &&
                    taskEntry.locationTrigger != null &&
                    taskEntry.locationTrigger.trim().length() > 0) {
                triggeredTask = true;
            }

            // Require a reason if the task can be rejected and it is not a self allocate task
    		if(Utilities.canReject(taskEntry.taskStatus)) {

    		    if(!taskEntry.taskStatus.equals("new")) {
                    rejectReason.setVisibility(View.VISIBLE);
                }

    	        Button b = new Button(this);
    	        b.setText(getString(R.string.smap_reject_task));
    	        b.setId(R.id.reject_button);
    	        b.setOnClickListener(this);
    	        buttons.addView(b);
    		}

    		if(!triggeredTask && Utilities.canComplete(taskEntry.taskStatus, taskEntry.taskType)) {
    	        Button b = new Button(this);
    	        b.setText(R.string.smap_complete_task);
    	        b.setId(R.id.complete_button);
    	        b.setOnClickListener(this);
    	        buttons.addView(b);
    		}

            if(Utilities.canRestore(taskEntry.taskStatus)) {
                Button b = new Button(this);
                b.setText(R.string.smap_restore_task);
                b.setId(R.id.restore_button);
                b.setOnClickListener(this);
                buttons.addView(b);
            }

            if(Utilities.canAccept(taskEntry.taskStatus)) {
                Button b = new Button(this);
                b.setText(R.string.smap_accept_task);
                b.setId(R.id.accept_button);
                b.setOnClickListener(this);
                buttons.addView(b);
            }

    	} catch (Exception e) {
  			e.printStackTrace();
  	  	}


    }

    /*
     * Handle a click on one of the buttons
     */
	@Override
	public void onClick(View v) {
      	
        switch (v.getId()) {

            case R.id.complete_button:
                try {

                    boolean canComplete = Utilities.canComplete(taskEntry.taskStatus, taskEntry.taskType);
                    String taskForm = taskEntry.taskForm;
                    String formPath = new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + taskForm;
                    String instancePath = taskEntry.instancePath;

                    if(canComplete) {
                        completeTask(instancePath, formPath, taskEntry.id);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_complete),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.reject_button:
                try {

                    if(Utilities.canReject(taskEntry.taskStatus)) {

                        String reason = rejectReason.getText().toString();
                        if(!taskEntry.taskStatus.equals("new") && reason != null && reason.trim().length() < 5) {
                            Toast.makeText(getApplicationContext(), getString(R.string.smap_reason_not_specified),
                                    Toast.LENGTH_LONG).show();
                        } else {

                            Utilities.setStatusForTask(taskEntry.id, Utilities.STATUS_T_REJECTED, reason);
                            Intent intent = new Intent("org.smap.smapTask.refresh");      // Notify map and task list of change
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
                            Timber.i("######## send org.smap.smapTask.refresh from taskAddressActivity");
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_reject),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.restore_button:
                try {

                    if(Utilities.canRestore(taskEntry.taskStatus)) {
                        Utilities.setStatusForTask(taskEntry.id, Utilities.STATUS_T_ACCEPTED, "");
                        Intent intent = new Intent("org.smap.smapTask.refresh");      // Notify map and task list of change
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
                        Timber.i("######## send org.smap.smapTask.refresh from instanceUploaderActivity2");
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_restore),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.accept_button:
                try {

                    if(Utilities.canAccept(taskEntry.taskStatus)) {
                        Utilities.setStatusForTask(taskEntry.id, Utilities.STATUS_T_ACCEPTED, "");
                        Intent intent = new Intent("org.smap.smapTask.refresh");      // Notify map and task list of change
                        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
                        Timber.i("######## send org.smap.smapTask.refresh from instanceUploaderActivity2");
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_accept),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
        return;
	}
	
	/*
	 * The user has selected an option to edit / complete a task
	 */
	public void completeTask(String instancePath, String formPath, long taskId) {

		
		// Get the provider URI of the instance 
        String where = InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] whereArgs = {
            instancePath
        };
		Cursor cInstanceProvider = managedQuery(InstanceColumns.CONTENT_URI, 
				null, where, whereArgs, null);
		if(cInstanceProvider.getCount() != 1) {
			Log.e("TaskAddressActivity", "Unique instance not found: count is:" +
					cInstanceProvider.getCount());
		} else {
			cInstanceProvider.moveToFirst();
			Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI,
	                cInstanceProvider.getLong(
	                cInstanceProvider.getColumnIndex(InstanceColumns._ID)));

            // Use an explicit intent
            Intent i = new Intent(this, org.odk.collect.android.activities.FormEntryActivity.class);
            i.setData(instanceUri);

			i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);	// TODO Don't think this is needed
			i.putExtra(FormEntryActivity.KEY_TASK, taskId);			
			if(instancePath != null) {	// TODO Don't think this is needed
				i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instancePath);           
			}
			startActivity(i);
		} 
		cInstanceProvider.close();
	}
}
