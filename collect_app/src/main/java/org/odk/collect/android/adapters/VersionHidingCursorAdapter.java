/*
 * Copyright (C) 2012 University of Washington
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

package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProvider;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.Date;

/**
 * Implementation of cursor adapter that displays the version of a form if a form has a version.
 *
 * @author mitchellsundt@gmail.com
 */
public class VersionHidingCursorAdapter extends SimpleCursorAdapter {

    private final Context ctxt;
    private final String versionColumnName;
    private final ViewBinder originalBinder;

    public VersionHidingCursorAdapter(String versionColumnName, Context context, int layout,
            Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.versionColumnName = versionColumnName;
        ctxt = context;
        originalBinder = getViewBinder();
        setViewBinder(new ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor,
                    int columnIndex) {
                String columnName = cursor.getColumnName(columnIndex);
                if (columnName.equals(FormsProviderAPI.FormsColumns.DATE) || columnName.equals("MAX(" + FormsProviderAPI.FormsColumns.DATE + ")")) {
                    String subtext = FormsProvider.getDisplaySubtext(context, new Date(cursor.getLong(columnIndex)));
                    TextView v = (TextView) view;
                    ((TextView) view).setText(subtext);
                    v.setVisibility(View.VISIBLE);
                } else if (columnName.equals(VersionHidingCursorAdapter.this.versionColumnName)) {
                    String version = cursor.getString(columnIndex);
                    TextView v = (TextView) view;
                    v.setText("");
                    v.setVisibility(View.GONE);
                    if (version != null) {
                        v.append(String.format(ctxt.getString(R.string.version_number), version));
                        v.append(" ");
                        v.setVisibility(View.VISIBLE);
                    }
                    if (from.length > 3) {
                        int idColumnIndex = cursor.getColumnIndex(from[3]);
                        String id = cursor.getString(idColumnIndex);
                        if (id != null) {
                            v.append(String.format(ctxt.getString(R.string.id_number), id));
                            v.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    view.setVisibility(View.VISIBLE);
                    return originalBinder != null && originalBinder.setViewValue(view, cursor, columnIndex);
                }
                return true;
            }
        });
    }

}
