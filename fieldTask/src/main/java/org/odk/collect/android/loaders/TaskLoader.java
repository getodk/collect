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

/*
 * Responsible for displaying loading tasks
 * 
 * @author Neil Penman (neilpenman@gmail.com)
 */
package org.odk.collect.android.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of AsyncTaskLoader which loads a {@code List<TaskEntry>}
 * containing all tasks on the device.
 */
public class TaskLoader extends AsyncTaskLoader<List<TaskEntry>> {

	private List<TaskEntry> mTasks = null;
	private TaskObserver mTaskObserver;	// Monitor changes to task data

	public TaskLoader(Context ctx) {
		super(ctx);
	}

	/**
	 * This method is called on a background thread and generates a List of
	 * {@link TaskEntry} objects. 
	 */
	@Override
	public List<TaskEntry> loadInBackground() {

		// Create corresponding array of entries and load their labels.
		ArrayList<TaskEntry> entries = new ArrayList<TaskEntry>(10);
		getForms(entries);
        Utilities.getTasks(entries, false);

		return entries;
	}

	private void getForms(ArrayList<TaskEntry> entries) {

        String [] proj = {FormsColumns._ID,
                FormsColumns.JR_FORM_ID,
                FormsColumns.JR_VERSION,
                FormsColumns.PROJECT,
                FormsColumns.DISPLAY_NAME};

        String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
        String selectClause = "(" + FormsColumns.SOURCE + "='" + Utilities.getSource() + "' or " +
                FormsColumns.SOURCE + " is null)" +
                " and " + FormsColumns.TASKS_ONLY + " = 'no'";


        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor formListCursor = resolver.query(FormsColumns.CONTENT_URI, proj, selectClause, null, sortOrder);


		if(formListCursor != null) {
    		 
			formListCursor.moveToFirst();
			while (!formListCursor.isAfterLast()) {
        		 
        		 TaskEntry entry = new TaskEntry();
	            
        		 entry.type = "form";
        		 entry.ident = formListCursor.getString(formListCursor.getColumnIndex(FormsColumns.JR_FORM_ID));
	             entry.formVersion = formListCursor.getInt(formListCursor.getColumnIndex(FormsColumns.JR_VERSION));
	             entry.name = formListCursor.getString(formListCursor.getColumnIndex(FormsColumns.DISPLAY_NAME));
	             entry.project = formListCursor.getString(formListCursor.getColumnIndex(FormsColumns.PROJECT));
	             entry.id = formListCursor.getLong(formListCursor.getColumnIndex(FormsColumns._ID));
	             
	             entries.add(entry);
	             formListCursor.moveToNext();
        	 }
    	}
        if(formListCursor != null) {
            formListCursor.close();
        }
	}

	/**
	 * Called when there is new data to deliver to the client. The superclass
	 * will deliver it to the registered listener (i.e. the LoaderManager),
	 * which will forward the results to the client through a call to
	 * onLoadFinished.
	 */
	@Override
	public void deliverResult(List<TaskEntry> tasks) {
		if (isReset()) {
			Log.w("taskloader",
					"+++ Warning! An async query came in while the Loader was reset! +++");

			if (tasks != null) {
				releaseResources(tasks);
				return;
			}
		}

		// Hold a reference to the old data so it doesn't get garbage collected.
		// We must protect it until the new data has been delivered.
		List<TaskEntry> oldTasks = mTasks;
		mTasks = tasks;

		if (isStarted()) {
			super.deliverResult(tasks);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldTasks != null && oldTasks != tasks) {
			releaseResources(oldTasks);
		}
	}

	@Override
	protected void onStartLoading() {

		if (mTasks != null) {
			deliverResult(mTasks);
		}

		// Register the observers that will notify the Loader when changes are
		// made.
		if (mTaskObserver == null) {
			mTaskObserver = new TaskObserver(this);
		}

		if (takeContentChanged()) {
			forceLoad();
		} else if (mTasks == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {

		cancelLoad();

	}

	@Override
	protected void onReset() {

		onStopLoading();

		// At this point we can release the resources associated with 'tasks'.
		if (mTasks != null) {
			releaseResources(mTasks);
			mTasks = null;
		}

		// The Loader is being reset, so we should stop monitoring for changes.
		if (mTaskObserver != null) {
			try {
				getContext().unregisterReceiver(mTaskObserver);
			} catch (Exception e) {
				
			}
			mTaskObserver = null;
		}

	}

	@Override
	public void onCanceled(List<TaskEntry> tasks) {
	
		super.onCanceled(tasks);
		releaseResources(tasks);
	}

	@Override
	public void forceLoad() {
		Log.i("SmapTaskLoader", "+++++++ forceLoad");
		super.forceLoad();
	}
	
	@Override
	protected void onForceLoad() {
		Log.i("SmapTaskLoader", "+++++++ onForceLoad");
		super.onForceLoad();
		
	}

	/**
	 * Helper method to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	private void releaseResources(List<TaskEntry> tasks) {

	}

}