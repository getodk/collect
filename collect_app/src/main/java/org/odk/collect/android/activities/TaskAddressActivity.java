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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.R;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.utilities.Utilities;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskAddressActivity extends Activity implements OnClickListener {

	private class Address {
		String name;
		String value;
	}

    TaskEntry taskEntry = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.task_address);  
        
        // Get the id of the selected list item
        Bundle bundle = getIntent().getExtras();

        taskEntry = Utilities.getTaskWithIdOrPath(bundle.getLong("id"), null);

    	try {

    		//String assignment_status = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_STATUS));
    		//String taskTitle = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_TITLE));
        	//String taskAddress = c.getString(c.getColumnIndex(FileDbAdapter.KEY_T_ADDRESS));
        	
        	// Formatting
   			LinearLayout.LayoutParams textLayout = 
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			textLayout.setMargins(1, 1, 1, 1);
			
			TableRow.LayoutParams trLayout = 
					new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			trLayout.setMargins(1, 1, 1, 1);
        	
            TableLayout tableLayout = (TableLayout)findViewById(R.id.task_address_values);
            
        	// Add Title
        	TextView title = (TextView)findViewById(R.id.task_title);
        	title.setText(taskEntry.name);
        	
        	// Add Status
   			TableRow r = new TableRow(this);
			r.setLayoutParams(trLayout);
			r.setBackgroundColor(0xff0000); 
	        TextView text1 = new TextView(this);
	        text1.setText(R.string.smap_status);
	        text1.setBackgroundColor(0xff0000);
	        TextView text2 = new TextView(this);
	        text2.setText(taskEntry.taskStatus);
	        text2.setBackgroundColor(0xff0000);
   	        r.addView(text1);
	        r.addView(text2);
	        tableLayout.addView(r);
	        
        	// Put the Address items in the table
    		Type type = new TypeToken<ArrayList<Address>>(){}.getType();		
    		ArrayList<Address> aArray = new Gson().fromJson(taskEntry.taskAddress, type);
    		if(aArray != null) {
	    		for(int i = 0; i < aArray.size(); i++) {
	    	        
	    			r = new TableRow(this);
	    			r.setLayoutParams(trLayout);
	    			r.setBackgroundColor(0xff0000);
	    	        
	    	        text1 = new TextView(this);
	    	        text1.setText(aArray.get(i).name);
	    	        text1.setBackgroundColor(0xff0000);
	    	        
	    	        text2 = new TextView(this);
	    	        text2.setText(aArray.get(i).value);
	    	        text2.setBackgroundColor(0xff0000);
	    	        
	    	        r.addView(text1);
	    	        r.addView(text2);
	    	        tableLayout.addView(r);
	    		}
    		}
    		
            // Create the buttons
            LinearLayout buttons = (LinearLayout)findViewById(R.id.task_address_buttons);
    		//menu.setHeaderTitle(taskTitle);
    		if(Utilities.canReject(taskEntry.taskStatus)) {

    	        Button b = new Button(this);
    	        b.setText(getString(R.string.smap_reject_task));
    	        b.setId(R.id.reject_button);
    	        b.setOnClickListener(this);
    	        buttons.addView(b);
    		}
    		if(Utilities.canComplete(taskEntry.taskStatus)) {
    	        Button b = new Button(this);
    	        b.setText(R.string.smap_complete_task);
    	        b.setId(R.id.complete_button);
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

    			Log.i("Complete Button", "");

    			boolean canComplete = Utilities.canComplete(taskEntry.taskStatus);
    			String taskForm = taskEntry.taskForm;
    			String formPath = Collect.FORMS_PATH + taskForm;
    			String instancePath = taskEntry.instancePath;
    			
    			if(canComplete) {
    				completeTask(instancePath, formPath, taskEntry.id);
    			} else {
        			Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_complete),
    		                Toast.LENGTH_SHORT).show();
    			}

    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		break;
    		
        case R.id.reject_button:
        	try {

                Log.i("Reject Button", "");

	    		if(Utilities.canReject(taskEntry.taskStatus)) {
                    Utilities.setStatusForTask(taskEntry.id, Utilities.STATUS_T_REJECTED);
                    Intent intent = new Intent("refresh");      // Notify map and task list of change
                    LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
	    		} else {
	    			Toast.makeText(getApplicationContext(), getString(R.string.smap_cannot_reject),
			                Toast.LENGTH_SHORT).show();
	    		}

	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
        	finish();
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
			// Start activity to complete form
			Intent i = new Intent(Intent.ACTION_EDIT, instanceUri);

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
