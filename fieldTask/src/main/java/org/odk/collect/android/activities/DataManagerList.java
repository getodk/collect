/*
 * Copyright (C) 2009 University of Washington
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

import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Responsible for displaying and deleting all the valid forms in the forms
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DataManagerList extends ListActivity implements
		DeleteInstancesListener {
	private static final String t = "DataManagerList";
	private AlertDialog mAlertDialog;
	private Button mDeleteButton;
	private Button mToggleButton;

	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();

	DeleteInstancesTask mDeleteInstancesTask = null;

	private static final String SELECTED = "selected";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_manage_list);

		mDeleteButton = (Button) findViewById(R.id.delete_button);
		mDeleteButton.setText(getString(R.string.delete_file));
		mDeleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	Collect.getInstance().getActivityLogger().logAction(this, "deleteButton", Integer.toString(mSelected.size()));
				if (mSelected.size() > 0) {
					createDeleteInstancesDialog();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.noselect_error, Toast.LENGTH_SHORT).show();
				}
			}
		});

		mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkAll = false;
                // if everything is checked, uncheck
                if (mSelected.size() == mInstances.getCount()) {
                    checkAll = false;
                    mSelected.clear();
                    mDeleteButton.setEnabled(false);
                } else {
                    // otherwise check everything
                    checkAll = true;
                    for (int pos = 0; pos < DataManagerList.this.getListView().getCount(); pos++) {
                        Long id = getListAdapter().getItemId(pos);
                        if (!mSelected.contains(id)) {
                            mSelected.add(id);
                        }
                    }
                    mDeleteButton.setEnabled(true);
                }
                for (int pos = 0; pos < DataManagerList.this.getListView().getCount(); pos++) {
                    DataManagerList.this.getListView().setItemChecked(pos, checkAll);
                }
            }
        });

		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, null, null,
				InstanceColumns.DISPLAY_NAME + " ASC");

		String[] data = new String[] { InstanceColumns.DISPLAY_NAME,
				InstanceColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item_multiple_choice, c, data, view);
		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);
		mDeleteButton.setEnabled(false);

		mDeleteInstancesTask = (DeleteInstancesTask) getLastNonConfigurationInstance();
	}

    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
    	super.onStop();
    }

	@Override
	public Object onRetainNonConfigurationInstance() {
		// pass the tasks on orientation-change restart
		return mDeleteInstancesTask;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long[] selectedArray = savedInstanceState.getLongArray(SELECTED);
		for (int i = 0; i < selectedArray.length; i++) {
			mSelected.add(selectedArray[i]);
		}
		mDeleteButton.setEnabled(selectedArray.length > 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		long[] selectedArray = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++) {
			selectedArray[i] = mSelected.get(i);
		}
		outState.putLongArray(SELECTED, selectedArray);
	}

	@Override
	protected void onResume() {
		// hook up to receive completion events
		if (mDeleteInstancesTask != null) {
			mDeleteInstancesTask.setDeleteListener(this);
		}
		super.onResume();
		// async task may have completed while we were reorienting...
		if (mDeleteInstancesTask != null
				&& mDeleteInstancesTask.getStatus() == AsyncTask.Status.FINISHED) {
			deleteComplete(mDeleteInstancesTask.getDeleteCount());
		}
	}

	@Override
	protected void onPause() {
		if (mDeleteInstancesTask != null ) {
			mDeleteInstancesTask.setDeleteListener(null);
		}
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
		super.onPause();
	}

	/**
	 * Create the instance delete dialog
	 */
	private void createDeleteInstancesDialog() {
        Collect.getInstance().getActivityLogger().logAction(this, "createDeleteInstancesDialog", "show");

		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setTitle(getString(R.string.delete_file));
		mAlertDialog.setMessage(getString(R.string.delete_confirm,
				mSelected.size()));
		DialogInterface.OnClickListener dialogYesNoListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE: // delete
			    	Collect.getInstance().getActivityLogger().logAction(this, "createDeleteInstancesDialog", "delete");
					deleteSelectedInstances();
					break;
				case DialogInterface. BUTTON_NEGATIVE: // do nothing
			    	Collect.getInstance().getActivityLogger().logAction(this, "createDeleteInstancesDialog", "cancel");
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.delete_yes),
				dialogYesNoListener);
		mAlertDialog.setButton2(getString(R.string.delete_no),
				dialogYesNoListener);
		mAlertDialog.show();
	}

	/**
	 * Deletes the selected files. Content provider handles removing the files
	 * from the filesystem.
	 */
	private void deleteSelectedInstances() {
		if (mDeleteInstancesTask == null) {
			mDeleteInstancesTask = new DeleteInstancesTask();
			mDeleteInstancesTask.setContentResolver(getContentResolver());
			mDeleteInstancesTask.setDeleteListener(this);
			mDeleteInstancesTask.execute(mSelected.toArray(new Long[mSelected
					.size()]));
		} else {
			Toast.makeText(this, getString(R.string.file_delete_in_progress),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get row id from db
		Cursor c = (Cursor) getListAdapter().getItem(position);
		long k = c.getLong(c.getColumnIndex(InstanceColumns._ID));

		// add/remove from selected list
		if (mSelected.contains(k))
			mSelected.remove(k);
		else
			mSelected.add(k);

		Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", Long.toString(k));

		mDeleteButton.setEnabled(!(mSelected.size() == 0));
	}

	@Override
	public void deleteComplete(int deletedInstances) {
		Log.i(t, "Delete instances complete");
        Collect.getInstance().getActivityLogger().logAction(this, "deleteComplete", Integer.toString(deletedInstances));
		if (deletedInstances == mSelected.size()) {
			// all deletes were successful
			Toast.makeText(this,
					getString(R.string.file_deleted_ok, deletedInstances),
					Toast.LENGTH_SHORT).show();
		} else {
			// had some failures
			Log.e(t, "Failed to delete "
					+ (mSelected.size() - deletedInstances) + " instances");
			Toast.makeText(
					this,
					getString(R.string.file_deleted_error, mSelected.size()
							- deletedInstances, mSelected.size()),
					Toast.LENGTH_LONG).show();
		}
		mDeleteInstancesTask = null;
		mSelected.clear();
		getListView().clearChoices(); // doesn't unset the checkboxes
		for ( int i = 0 ; i < getListView().getCount() ; ++i ) {
			getListView().setItemChecked(i, false);
		}
		mDeleteButton.setEnabled(false);
	}
}
