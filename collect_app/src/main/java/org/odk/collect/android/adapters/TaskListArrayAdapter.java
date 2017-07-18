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

/*
 * Responsible for displaying tasks in a list view
 * 
 * @author Neil Penman (neilpenman@gmail.com)
 */
package org.odk.collect.android.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.utilities.KeyValueJsonFns;
import org.odk.collect.android.utilities.Utilities;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class TaskListArrayAdapter extends ArrayAdapter<TaskEntry> {
    
    private int mLayout;
    LayoutInflater mInflater;
    static String TAG = "TaskListArrayAdapter";
	
    public TaskListArrayAdapter(Context context) {
		super(context, R.layout.main_list);
		mLayout = R.layout.task_row;
		mInflater = LayoutInflater.from(context);
	}

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

    	View view;
    	
    	if (convertView == null) {
    		view = mInflater.inflate(mLayout, parent, false);
    	} else {
    		view = convertView;
    	}
    
    	TaskEntry item = getItem(position);

    	ImageView icon = (ImageView) view.findViewById(R.id.icon);
    	if(item.type.equals("form")) {
    		icon.setImageResource(R.drawable.ic_form);
    	} else if (item.taskStatus != null) {
    		if(item.taskStatus.equals(Utilities.STATUS_T_ACCEPTED)) {
				if(item.locationTrigger != null && !item.repeat) {
                    icon.setImageResource(R.drawable.ic_task_triggered);
                } else if (item.locationTrigger != null && item.repeat) {
                    icon.setImageResource(R.drawable.ic_task_triggered_repeat);
                } else if(item.repeat) {
					icon.setImageResource(R.drawable.ic_task_repeat);
				} else {
					icon.setImageResource(R.drawable.ic_task_open);
				}
    		} else if(item.taskStatus.equals(Utilities.STATUS_T_COMPLETE)) {
    			icon.setImageResource(R.drawable.ic_task_done);
    		} else if(item.taskStatus.equals(Utilities.STATUS_T_REJECTED) || item.taskStatus.equals(Utilities.STATUS_T_CANCELLED)) {
    			icon.setImageResource(R.drawable.ic_task_reject);
    		} else if(item.taskStatus.equals(Utilities.STATUS_T_SUBMITTED)) {
    			icon.setImageResource(R.drawable.ic_task_submitted);
    		}
    	}
    	
    	
    	TextView taskNameText = (TextView) view.findViewById(R.id.toptext);
    	if (taskNameText != null) {
            taskNameText.setText(item.name + " (v:" + item.formVersion + ")");
        }

    	TextView taskStartText = (TextView) view.findViewById(R.id.middletext);
    	if(taskStartText != null) {
	    	if(item.type.equals("form")) {
                taskStartText.setText(getContext().getString(R.string.smap_project) + ": " + item.project);
	    	} else {
                String line2 = Utilities.getTaskTime(item.taskStatus, item.actFinish, item.taskStart);
                taskStartText.setText(line2);
	    	}
    	}

        TextView taskEndText = (TextView) view.findViewById(R.id.bottomtext);
        if(taskEndText != null) {

            if(item.type.equals("form")) {
                taskEndText.setText("");
            } else {
                String addressText = KeyValueJsonFns.getValues(item.taskAddress);
                if (addressText != null) {
                    taskEndText.setText(addressText);
                }
            }
        }

    	 
    	return view;
    }
    
    public void setData(List<TaskEntry> data) {
        clear();
        if (data != null) {
          for (int i = 0; i < data.size(); i++) {
            add(data.get(i));
          }
        }
      }
    

}
