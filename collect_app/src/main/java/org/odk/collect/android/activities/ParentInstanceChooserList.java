/* The MIT License (MIT)
 *
 *       Copyright (c) 2015 PMA2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.ParentFormListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormRelationsDb;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.Iterator;
import java.util.Set;

/**
 *  Responsible for displaying all the valid instances in the instance directory.
 *
 *  Replaces `InstanceChooserList.java`.
 *
 *  Creator: James K. Pringle
 *  Email: jpringle@jhu.edu
 *  Created: 4 September 2015
 *  Last modified: 8 September 2015
 */
public class ParentInstanceChooserList extends ListActivity {

    private static final String TAG = "ParentChooser";
    private static final boolean LOCAL_LOG = true;

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private AlertDialog mAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an external intent
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        // ParentInstanceChooser looks exactly the same as original list chooser.
        setContentView(R.layout.chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.review_data));
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setVisibility(View.GONE);


        String[] projection = {
                InstanceColumns._ID,
                InstanceColumns.DISPLAY_NAME,
                InstanceColumns.DISPLAY_SUBTEXT,
                InstanceColumns.STATUS,
                InstanceColumns.CAN_EDIT_WHEN_COMPLETE
        };
        String selection = InstanceColumns.STATUS + " != ?";

        Set<Long> children = FormRelationsDb.getAllChildren();
        if ( children.size() > 0 ) {
            // Build extra selection string
            StringBuilder sb = new StringBuilder();
            for (Iterator<Long> it = children.iterator(); it.hasNext(); ) {
                Long child = it.next();
                sb.append(InstanceColumns._ID + " != " + String.valueOf(child));
                if (it.hasNext()) {
                    sb.append(" AND ");
                }
            }
            // Concatenate
            selection += " AND " + sb.toString();
        }

        if (LOCAL_LOG) {
            Log.d(TAG, "Building chooser query: " + selection);
        }

        String[] selectionArgs = {
                InstanceProviderAPI.STATUS_SUBMITTED
        };

        String sortOrder = InstanceColumns.STATUS + " DESC, " +
                InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor c = managedQuery(InstanceColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        if (LOCAL_LOG) {
            Log.d(TAG, "Query for parents returned " + c.getCount() + " view(s) to display");
        }

        int[] toViews = new int[] {
                R.id.text1, R.id.text2
        };

        // render total instance view
        String[] fromColumns = {
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT
        };

        SimpleCursorAdapter adapter =
                new ParentFormListAdapter(this,  R.layout.two_item, c, fromColumns, toViews);
        setListAdapter(adapter);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor c = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(c);
        Long parentId = c.getLong(c.getColumnIndex(InstanceColumns._ID));
        Uri instanceUri =
                ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                        parentId);

        Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                instanceUri.toString());

        long[] children = FormRelationsDb.getChildren(parentId);
        if ( children.length == 0 ) {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = c.getString(c.getColumnIndex(InstanceColumns.STATUS));
            String strCanEditWhenComplete =
                    c.getString(c.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

            boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE) ||
                    Boolean.parseBoolean(strCanEditWhenComplete);
            if (!canEdit) {
                createErrorDialog(getString(R.string.cannot_edit_completed_form),
                        DO_NOT_EXIT);
                return;
            }
            // caller wants to view/edit a form, so launch formentryactivity
            startActivity(new Intent(Intent.ACTION_EDIT, instanceUri));
        } else {
            Intent i = new Intent(this, ChildInstanceChooserList.class);
            i.putExtra("parent", parentId);
            startActivityForResult(i, 555);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {

        } else {
            setResult(RESULT_OK);
            finish();
        }
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

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog",
                                shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }
}
