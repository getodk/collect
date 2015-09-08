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

package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FormRelationsDb;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

/**
 *  Responsible for creating views for ParentInstnaceChooserList
 *
 *  Creator: James K. Pringle
 *  Email: jpringle@jhu.edu
 *  Created: 4 September 2015
 *  Last modified: 8 September 2015
 */
public class ParentFormListAdapter extends SimpleCursorAdapter {

    public ParentFormListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);


        long id = cursor.getLong(cursor.getColumnIndex(InstanceColumns._ID));
        long[] children = FormRelationsDb.getChildren(id);

        if (children.length > 0) {
            TextView t = (TextView) view.findViewById(R.id.text2);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < children.length; i++) {
                sb.append(InstanceColumns._ID + " = " + children[i]);
                if (i != children.length - 1) {
                    sb.append(" OR ");
                }
            }

            String selection = "( " + InstanceColumns.STATUS + " = ? OR " +
                    InstanceColumns.STATUS + " = ? OR " + InstanceColumns.STATUS + " = ? )" +
                    " AND (" + sb.toString() + ")";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED
            };
            String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME
                    + " ASC";
            Cursor c = context.getContentResolver().query(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, sortOrder);
            int count = c.getCount();
            c.close();
            t.setText("Completed: " + count + "/" + children.length);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.two_item, null);
        return view;
    }
}
