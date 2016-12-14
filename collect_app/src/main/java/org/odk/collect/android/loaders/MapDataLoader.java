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
 * Responsible for displaying loading points
 * 
 * @author Neil Penman (neilpenman@gmail.com)
 */
package org.odk.collect.android.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.odk.collect.android.utilities.TraceUtilities;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;

/**
 * An implementation of AsyncTaskLoader which loads a {@code List<PointEntry>}
 * containing all tasks on the device.
 */
public class MapDataLoader extends AsyncTaskLoader<MapEntry> {

	private MapEntry mData = null;
	private MapDataObserver mMapDataObserver;	// Monitor changes to task data

	public MapDataLoader(Context ctx) {
		super(ctx);
	}

	/**
	 * This method is called on a background thread and generates a List of
	 * {@link org.odk.collect.android.loaders.PointEntry} objects.
	 */
	@Override
	public MapEntry loadInBackground() {

        MapEntry data = new MapEntry();

		// Create corresponding array of entries and load their labels.
		data.points = new ArrayList<PointEntry>(100);
        data.tasks = new ArrayList<TaskEntry> (10);
        TraceUtilities.getPoints(data.points);
        Utilities.getTasks(data.tasks, false);

		return data;
	}

	/**
	 * Called when there is new data to deliver to the client. The superclass
	 * will deliver it to the registered listener (i.e. the LoaderManager),
	 * which will forward the results to the client through a call to
	 * onLoadFinished.
	 */
	@Override
	public void deliverResult(MapEntry data) {
		if (isReset()) {
			Log.w("taskloader",
					"+++ Warning! An async query came in while the Loader was reset! +++");

			if (data != null) {
				releaseResources(data);
				return;
			}
		}

		// Hold a reference to the old data so it doesn't get garbage collected.
		// We must protect it until the new data has been delivered.
		MapEntry oldData = mData;
		mData = data;

		if (isStarted()) {
			super.deliverResult(data);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldData != null && oldData != data) {
			releaseResources(oldData);
		}
	}

	@Override
	protected void onStartLoading() {

		if (mData != null) {
			deliverResult(mData);
		}

		// Register the observers that will notify the Loader when changes are
		// made.
		if (mMapDataObserver == null) {
			mMapDataObserver = new MapDataObserver(this);
		}

		if (takeContentChanged()) {
			forceLoad();
		} else if (mData == null) {
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
		if (mData != null) {
			releaseResources(mData);
			mData = null;
		}

		// The Loader is being reset, so we should stop monitoring for changes.
		if (mMapDataObserver != null) {
			try {
				getContext().unregisterReceiver(mMapDataObserver);
			} catch (Exception e) {
				
			}
			mMapDataObserver = null;
		}

	}

	@Override
	public void onCanceled(MapEntry data) {
	
		super.onCanceled(data);
		releaseResources(data);
	}

	@Override
	public void forceLoad() {
		Log.i("SmapPointLoader", "+++++++ forceLoad");
		super.forceLoad();
	}
	
	@Override
	protected void onForceLoad() {
		Log.i("SmapPointLoader", "+++++++ onForceLoad");
		super.onForceLoad();
		
	}

	/**
	 * Helper method to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	private void releaseResources(MapEntry data) {

	}

}