/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.views.TwoTextItemCheckView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
public class LocalFileManagerList extends ListActivity {

	// delete an item
	private static final int MENU_DELETE = Menu.FIRST;

	private AlertDialog mAlertDialog;

	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_manage_list);

		Button b = (Button) findViewById(R.id.delete_button);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				createDeleteDialog();
			}
		});
		// buildView takes place in resume
	}

	private void buildView() {

		// get all mInstances that match the status.
		FileDbAdapter fda = new FileDbAdapter(this);
		fda.open();
		fda.addOrphanForms();
		Cursor c = fda.fetchAllFiles();
		startManagingCursor(c);

		String[] data = new String[] { FileDbAdapter.KEY_DISPLAY,
				FileDbAdapter.KEY_META };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
		// mInstances = new SimpleCursorAdapter(this,
		// R.layout.two_item_multiple_choice, c, data, view);
		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item_multiple_choice, c, data, view);
		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);

		// cleanup
		fda.close();

	}

	/**
	 * Create the file delete dialog
	 */
	private void createDeleteDialog() {
		mAlertDialog = new AlertDialog.Builder(this).create();

		mAlertDialog.setMessage(getString(R.string.delete_confirm, mSelected
				.size()));
		DialogInterface.OnClickListener dialogYesNoListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON1: // delete and
					deleteSelectedFiles();
					refreshData();
					break;
				case DialogInterface.BUTTON2: // do nothing
					break;
				}
			}

		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.yes), dialogYesNoListener);
		mAlertDialog.setButton2(getString(R.string.no), dialogYesNoListener);
		mAlertDialog.show();
	}

	private void refreshData() {
		if (mInstances != null) {
			mInstances.getCursor().requery();
		}
		mSelected.clear();
		buildView();
	}

	/**
	 * Deletes the selected files.First from the database then from the file
	 * system
	 */
	private void deleteSelectedFiles() {

		FileDbAdapter fda = new FileDbAdapter(this);
		fda.open();

		// delete removes the file from the database first
		int deleted = 0;
		for (int i = 0; i < mSelected.size(); i++) {
			if (fda.deleteFile(mSelected.get(i))) {
				deleted++;
			}
		}

		// remove the actual files and close db
		fda.removeOrphanForms();
		fda.removeOrphanInstances();
		fda.close();

		if (deleted > 0) {
			// all deletes were successful
			Toast.makeText(getApplicationContext(),
					getString(R.string.file_deleted_ok, deleted),
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			// had some failures
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.file_deleted_error, mSelected.size()
							- deleted + " of " + mSelected.size()),
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		TwoTextItemCheckView t = (TwoTextItemCheckView) v;

		// get row id from db
		Cursor c = (Cursor) getListAdapter().getItem(position);
		long k = c.getLong(c.getColumnIndex(FileDbAdapter.KEY_ID));
		
		// add/remove from selected list
		if (mSelected.contains(k))
			mSelected.remove(k);
		else
			mSelected.add(k);
	}

	@Override
	protected void onPause() {
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// update the list (for returning from the remote manager)
		refreshData();
		super.onResume();
	}

}
