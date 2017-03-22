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

package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.TaskListArrayAdapter;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.loaders.TaskLoader;
import org.odk.collect.android.receivers.LocationChangedReceiver;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.Constants;

import java.util.List;

/**
 * Responsible for displaying buttons to launch the major activities. Launches some activities based
 * on returns of others.
 * 
 * @author Neil Penman 
 */
public class MainListActivity extends FragmentActivity  {
	
	private LoaderManager.LoaderCallbacks<List<TaskEntry>> mCallbacks;
	private AlertDialog mAlertDialog;

    private LocationManager locationManager;
    protected PendingIntent locationListenerPendingIntent;
    private static MainTabsActivity tabsActivity;

	 @Override
	  public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         // Create the ListFragment
         FragmentManager fm = getSupportFragmentManager();
         if (fm.findFragmentById(android.R.id.content) == null) {
             TaskListFragment list = new TaskListFragment();
             fm.beginTransaction().add(android.R.id.content, list).commit();
         }

         // Setup the location update Pending Intents
         locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
         Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
         locationListenerPendingIntent = PendingIntent.getBroadcast(this, 1000, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

         tabsActivity = (MainTabsActivity) getParent();
     }

	
	/*
	 * Fragment to display list of tasks
	 */
	 public static class TaskListFragment extends ListFragment implements
     		LoaderManager.LoaderCallbacks<List<TaskEntry>> {
	
		private static final int TASK_LOADER_ID = 1;
		
		private TaskListArrayAdapter mAdapter;
	  	private MainListActivity mActivity;
		  	

	    public TaskListFragment() {
	    	super();    	
	    	mActivity = (MainListActivity) getActivity();
	    }
	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        mAdapter = new TaskListArrayAdapter(getActivity());
	        setListAdapter(mAdapter);
	        setListShown(false);
	        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
	        
	        registerForContextMenu(getListView());
	        
	        // Handle long item clicks
	        ListView lv = getListView();
	        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	            @Override
	            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
	                return onLongListItemClick(v,pos,id);
	            }
	        });
	
	    }
	    
	    @Override
	    public Loader<List<TaskEntry>> onCreateLoader(int id, Bundle args) {
	    	return new TaskLoader(getActivity());
	    }
	
	    @Override
	    public void onLoadFinished(Loader<List<TaskEntry>> loader, List<TaskEntry> data) {
	    	mAdapter.setData(data);
            tabsActivity.setLocationTriggers(data, false);      // NFC and geofence triggers

	    	if (isResumed()) {
	    		setListShown(true);
	    	} else {
	    		setListShownNoAnimation(true);
	    	}
	    }
	
	    @Override
	    public void onLoaderReset(Loader<List<TaskEntry>> loader) {
	      mAdapter.setData(null);
	    }

	    
	    @Override
	    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    	inflater.inflate(R.menu.task_context, menu);
	        super.onCreateOptionsMenu(menu, inflater);
	    }
	    
		
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        return super.onOptionsItemSelected(item);
	    }

	    
	    /*
	     * Handle a long click on a list item
	     */
	    protected boolean onLongListItemClick(View v, int position, long id) {
	    	
	    	TaskEntry task = (TaskEntry) getListAdapter().getItem(position);
	    	
	    	if(task.type.equals("task")) {
		    	Intent i = new Intent(getActivity(), org.odk.collect.android.activities.TaskAddressActivity.class);
		        i.putExtra("id", task.id);
		        
		    	startActivity(i);
	    	}
	        return true;
	    }
		    
	    
	    /**
	     * Starts executing the selected task
	     */
	    @Override
	    public void onListItemClick(ListView listView, View view, int position, long id) {
	 	       
	    	TaskEntry entry = (TaskEntry) getListAdapter().getItem(position);

	    	if(entry.type.equals("task")) {
                if(entry.locationTrigger != null && entry.locationTrigger.length() > 0) {
                    Toast.makeText(
                            tabsActivity,
                            getString(R.string.smap_must_start_from_nfc),
                            Toast.LENGTH_SHORT).show();
                } else {
                    tabsActivity.completeTask(entry);
                }
	    	} else {

                Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, entry.id);

                // Use an explicit intent
                Intent i = new Intent(tabsActivity, org.odk.collect.android.activities.FormEntryActivity.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                i.setData(formUri);
                startActivity(i);

	    		//startActivity(new Intent(Intent.ACTION_EDIT, formUri));
	    	}

	    }


	 }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MainListActivity", "onStart============================");
        requestLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainListActivity", "onStop============================");
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {
        dismissDialogs();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MainListActivity", "onDestroy============================");
        disableLocationUpdates();
    }


    /**
     * Dismiss any dialogs that we manage.
     */
    private void dismissDialogs() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    /**
     * Start listening for location updates.
     */
    protected void requestLocationUpdates() {
        // Normal updates while activity is visible.
        // TODO manage multiple providers
        // TODO Manage provder being enabled / disabled

        /*
         * Only use GPS to get locations for tracking the user
         *  Using less accurate sources is not feasible to collect a gpx trail
         *  However it may be useful if we were just recording location of survey
         */
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {     // Fix issue with errors on devices without GPS
            try {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPS_INTERVAL, Constants.GPS_DISTANCE, locationListenerPendingIntent);
			} catch (SecurityException e) {
				// Permission not granted
			}
        }
    }

    /**
     * Stop listening for location updates
     */
    protected void disableLocationUpdates() {


    try {
            locationManager.removeUpdates(locationListenerPendingIntent);
    } catch (Exception e) {
            // Ignore failures, we are exiting after all
    }


    }


}


