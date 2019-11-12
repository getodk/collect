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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Implementation of cursor adapter that displays the version of a form if a form has a version.
 *
 * @author mitchellsundt@gmail.com
 */
public class VersionHidingCursorAdapter extends SimpleCursorAdapter {

    private final Context context;
    private final ListView listView;
    private final ViewBinder originalBinder;
    private final AdapterView.OnItemClickListener mapButtonListener;

    public VersionHidingCursorAdapter(ListView listView, String versionColumnName, Context context, int layout,
                                      AdapterView.OnItemClickListener mapButtonListener,
                                      String[] from, int[] to) {
        super(context, layout, null, from, to);
        this.context = context;
        this.listView = listView;
        this.mapButtonListener = mapButtonListener;
        originalBinder = getViewBinder();
        setViewBinder((view, cursor, columnIndex) -> {
            String columnName = cursor.getColumnName(columnIndex);
            if (columnName.equals(FormsColumns.DATE) || columnName.equals(FormsColumns.MAX_DATE)) {
                String subtext = getDisplaySubtext(context, new Date(cursor.getLong(columnIndex)));
                if (!subtext.isEmpty()) {
                    TextView v = (TextView) view;
                    ((TextView) view).setText(subtext);
                    v.setVisibility(View.VISIBLE);
                }
            } else if (columnName.equals(versionColumnName)) {
                String version = cursor.getString(columnIndex);
                TextView v = (TextView) view;
                v.setText("");
                v.setVisibility(View.GONE);
                if (version != null) {
                    v.append(String.format(this.context.getString(R.string.version_number), version));
                    v.append(" ");
                    v.setVisibility(View.VISIBLE);
                }
                if (from.length > 3) {
                    int idColumnIndex = cursor.getColumnIndex(from[3]);
                    String id = cursor.getString(idColumnIndex);
                    if (id != null) {
                        v.append(String.format(this.context.getString(R.string.id_number), id));
                        v.setVisibility(View.VISIBLE);
                    }
                }
            } else if (columnName.equals(FormsColumns.GEOMETRY_XPATH)) {
                String xpath = cursor.getString(columnIndex);
                view.setVisibility(xpath != null ? View.VISIBLE : View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
                return originalBinder != null && originalBinder.setViewValue(view, cursor, columnIndex);
            }
            return true;
        });
    }

    @Override public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        View mapButton = view.findViewById(R.id.map_button);
        if (mapButton != null) {
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            mapButton.setOnClickListener(v -> mapButtonListener.onItemClick(
                listView, view, cursor.getPosition(), id));
        }
    }

    private String getDisplaySubtext(Context context, Date date) {
        String displaySubtext = "";
        try {
            if (context != null) {
                displaySubtext = new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
        return displaySubtext;
    }

}
