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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.database.forms.DatabaseFormColumns;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/** An adapter for displaying form definitions in a list. */
public class FormListAdapter extends SimpleCursorAdapter {
    private final Context context;
    private final ListView listView;
    private final ViewBinder originalBinder;
    private final OnItemClickListener mapButtonListener;

    public FormListAdapter(
        ListView listView, String versionColumnName, Context context, int layoutId,
        OnItemClickListener mapButtonListener, String[] columnNames, int[] viewIds) {
        super(context, layoutId, null, columnNames, viewIds);
        this.context = context;
        this.listView = listView;
        this.mapButtonListener = mapButtonListener;

        originalBinder = getViewBinder();
        setViewBinder((view, cursor, columnIndex) -> {
            String columnName = cursor.getColumnName(columnIndex);
            if (columnName.equals(DatabaseFormColumns.DATE)) {
                Long dateOfCreation = cursor.getLong(columnIndex);
                Long dateOfLastAttachmentsUpdate = cursor.isNull(cursor.getColumnIndex(DatabaseFormColumns.LAST_DETECTED_ATTACHMENTS_UPDATE_DATE)) ? null : cursor.getLong(cursor.getColumnIndex(DatabaseFormColumns.LAST_DETECTED_ATTACHMENTS_UPDATE_DATE));
                String timestampText = getTimestampText(dateOfCreation, dateOfLastAttachmentsUpdate);
                if (!timestampText.isEmpty()) {
                    TextView v = (TextView) view;
                    v.setText(timestampText);
                    v.setVisibility(View.VISIBLE);
                }
            } else if (columnName.equals(versionColumnName)) {
                String versionIdText = "";
                String version = cursor.getString(columnIndex);
                if (version != null) {
                    versionIdText += getString(R.string.version_number, version);
                }
                if (Arrays.asList(columnNames).contains(DatabaseFormColumns.JR_FORM_ID)) {
                    String id = cursor.getString(cursor.getColumnIndex(DatabaseFormColumns.JR_FORM_ID));
                    if (version != null && id != null) {
                        versionIdText += "\n";
                    }
                    if (id != null) {
                        versionIdText += getString(R.string.id_number, id);
                    }
                }
                TextView v = (TextView) view;
                v.setVisibility(View.GONE);
                if (!versionIdText.isEmpty()) {
                    v.setText(versionIdText);
                    v.setVisibility(View.VISIBLE);
                }
            } else if (columnName.equals(DatabaseFormColumns.GEOMETRY_XPATH)) {
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
        View mapView = view.findViewById(R.id.map_view);
        if (mapView != null) {
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            mapView.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    mapButtonListener.onItemClick(listView, view, cursor.getPosition(), id);
                }
            });
        }
    }

    private String getTimestampText(Long dateOfCreation, Long dateOfLastAttachmentsUpdate) {
        try {
            if (context != null) {
                if (dateOfLastAttachmentsUpdate != null) {
                    return new SimpleDateFormat(context.getString(R.string.updated_on_date_at_time), Locale.getDefault()).format(new Date(dateOfLastAttachmentsUpdate));
                } else {
                    return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time), Locale.getDefault()).format(new Date(dateOfCreation));
                }
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Current locale: %s", Locale.getDefault());
        }
        return "";
    }

    private String getString(int id, Object... args) {
        return context != null ? context.getString(id, args) : "";
    }
}
