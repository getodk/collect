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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.TraceUtilities;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;

import androidx.loader.content.AsyncTaskLoader;

/**
 * An implementation of AsyncTaskLoader which loads a {@code List<MapEntry>}
 * containing all tasks on the device.
 */
public class MapDataLoader extends AsyncTaskLoader<MapEntry> {

	private MapEntry mData = null;
	private MapDataObserver mMapDataObserver;	// Monitor changes to task data

    private String sortOrder = "BY_NAME_ASC";
    private CharSequence filter = "";

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
        TraceUtilities.getPoints(data.points, 500, true);
        getForms(data.tasks);
        Utilities.getTasks(data.tasks, false, sortOrder, filter.toString(), false, true);

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
			Log.w("dataloader",
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

    private void getForms(ArrayList<TaskEntry> entries) {

        String [] proj = {FormsProviderAPI.FormsColumns._ID,
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                FormsProviderAPI.FormsColumns.PROJECT,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                FormsProviderAPI.FormsColumns.GEOMETRY_XPATH};

        String selectClause = "(lower(" + FormsProviderAPI.FormsColumns.SOURCE + ")='" + Utilities.getSource() + "' or " +
                FormsProviderAPI.FormsColumns.SOURCE + " is null)" +
                " and " + FormsProviderAPI.FormsColumns.TASKS_ONLY + " = 'no'";

        String[] selectArgs = null;
        if(filter.toString().trim().length() > 0 ) {
            selectClause += " and " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " LIKE ?";
            selectArgs = new String[] {"%" + filter + "%"};
        }

        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor formListCursor = resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, proj, selectClause, selectArgs, getSortOrderExpr(sortOrder));


        if(formListCursor != null) {

            formListCursor.moveToFirst();
            while (!formListCursor.isAfterLast()) {

                TaskEntry entry = new TaskEntry();

                entry.type = "form";
                entry.ident = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
                entry.formVersion = formListCursor.getInt(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
                entry.name = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
                entry.project = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.PROJECT));
                entry.geometryXPath = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH));
                entry.id = formListCursor.getLong(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID));

                entries.add(entry);
                formListCursor.moveToNext();
            }
        }
        if(formListCursor != null) {
            formListCursor.close();
        }
    }

    /*
     * Change sort order
     */
    public void updateSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /*
     * Change filter
     */
    public void updateFilter(CharSequence filter) {
        this.filter = filter;
    }

    private String getSortOrderExpr(String sortOrder) {

        String sortOrderExpr = "";

        if(sortOrder.equals("BY_NAME_ASC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
        } else if(sortOrder.equals("BY_NAME_DESC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        } else if(sortOrder.equals("BY_DATE_ASC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " ASC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
        } else if(sortOrder.equals("BY_DATE_DESC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " DESC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        }
        return sortOrderExpr;
    }

}