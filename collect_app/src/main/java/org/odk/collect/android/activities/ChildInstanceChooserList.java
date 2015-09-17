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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormRelationsDb;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  Responsible for displaying all the valid child instances, given a parent.
 *
 *  Creator: James K. Pringle
 *  Email: jpringle@jhu.edu
 *  Created: 4 September 2015
 *  Last modified: 8 September 2015
 */
public class ChildInstanceChooserList extends ListActivity {

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private AlertDialog mAlertDialog;
    private boolean mRelativesSent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRelativesSent = false;

        // must be at the beginning of any activity that can be called from an
        // external intent
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        setContentView(R.layout.child_chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.review_data));
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setVisibility(View.GONE);

        long parentId = getIntent().getLongExtra("parent", -1);
        long[] childrenIds = FormRelationsDb.getChildren(parentId);

        Set<Long> familyIds = new HashSet<Long>();
        familyIds.add(parentId);
        for (int i = 0; i < childrenIds.length; i++) {
            familyIds.add(childrenIds[i]);
        }
        mRelativesSent = isAnySent(familyIds);

        if (childrenIds.length > 0) {
            StringBuilder listQuery = new StringBuilder();
            listQuery.append(InstanceColumns.STATUS);
            listQuery.append(" !=? AND (");
            for (int i = 0; i < childrenIds.length; i++) {
                listQuery.append(InstanceColumns._ID);
                listQuery.append(" = ");
                listQuery.append(childrenIds[i]);
                if ( i != childrenIds.length - 1 ) {
                    listQuery.append(" OR ");
                }
            }
            listQuery.append(")");

            String[] projection = {
                    InstanceColumns._ID,
                    InstanceColumns.DISPLAY_NAME,
                    InstanceColumns.DISPLAY_SUBTEXT,
                    InstanceColumns.STATUS,
                    InstanceColumns.CAN_EDIT_WHEN_COMPLETE
            };
            String selection = listQuery.toString();
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED
            };
            String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME
                    + " ASC";
            Cursor c = managedQuery(InstanceColumns.CONTENT_URI, projection, selection,
                    selectionArgs, sortOrder);

            String[] fromColumns = new String[] {
                    InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT
            };
            int[] toViews = new int[] {
                    R.id.text1, R.id.text2
            };

            SimpleCursorAdapter instances =
                    new SimpleCursorAdapter(this, R.layout.two_item, c, fromColumns, toViews);
            setListAdapter(instances);
        }

        final Uri formUri = Uri.withAppendedPath(InstanceColumns.CONTENT_URI,
                String.valueOf(parentId));
        Cursor form = managedQuery(formUri, null, null, null, null);
        form.moveToFirst();
        String name = form.getString(form.getColumnIndex(InstanceColumns.DISPLAY_NAME));
        String sub = form.getString(form.getColumnIndex(InstanceColumns.DISPLAY_SUBTEXT));

        TextView t = (TextView) findViewById(R.id.text1);
        t.setText(name);
        TextView ts = (TextView) findViewById(R.id.text2);
        ts.setText(sub);

        RelativeLayout parentHolder = (RelativeLayout) findViewById(R.id.parentFormHolder);
        parentHolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mRelativesSent) {
                    Toast.makeText(
                            ChildInstanceChooserList.this,
                            getString(R.string.no_editing_sub_forms),
                            Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(new Intent(Intent.ACTION_EDIT, formUri), 555);
                }
            }
        });
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor c = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(c);
        Uri instanceUri =
                ContentUris.withAppendedId(InstanceColumns.CONTENT_URI,
                        c.getLong(c.getColumnIndex(InstanceColumns._ID)));

        Collect.getInstance().getActivityLogger()
                .logAction(this, "onListItemClick", instanceUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(instanceUri));
        } else {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = c.getString(c.getColumnIndex(InstanceColumns.STATUS));
            String strCanEditWhenComplete =
                    c.getString(c.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

            boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE)
                    || Boolean.parseBoolean(strCanEditWhenComplete);
            if (!canEdit) {
                createErrorDialog(getString(R.string.cannot_edit_completed_form),
                        DO_NOT_EXIT);
                return;
            }
            if (mRelativesSent) {
                Toast.makeText(
                        ChildInstanceChooserList.this,
                        getString(R.string.no_editing_sub_forms),
                        Toast.LENGTH_SHORT).show();
            } else {
                startActivityForResult(new Intent(Intent.ACTION_EDIT, instanceUri), 555);
            }
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

    private boolean isAnySent(Set<Long> ids) {
        boolean toReturn = false;
        if ( !ids.isEmpty() ) {
            StringBuilder sb = new StringBuilder();
            sb.append(InstanceColumns.STATUS);
            sb.append(" =? AND (");
            for (Iterator<Long> it = ids.iterator(); it.hasNext(); ) {
                Long id = it.next();
                sb.append(InstanceColumns._ID);
                sb.append(" = ");
                sb.append(id);
                if ( it.hasNext() ) {
                    sb.append(" OR ");
                }
            }
            sb.append(")");

            String[] projection = {
                    InstanceColumns._ID
            };
            String selection = sb.toString();
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED
            };
            Cursor c = getContentResolver().query(InstanceColumns.CONTENT_URI, projection,
                    selection, selectionArgs, null);
            toReturn = c != null && c.getCount() > 0;
        }
        return toReturn;
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
                    case DialogInterface.BUTTON1:
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "createErrorDialog",
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
