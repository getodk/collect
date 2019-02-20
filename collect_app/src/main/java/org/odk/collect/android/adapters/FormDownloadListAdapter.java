/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;
import java.util.HashMap;

import static org.odk.collect.android.activities.FormDownloadList.FORMID_DISPLAY;
import static org.odk.collect.android.activities.FormDownloadList.FORMNAME;
import static org.odk.collect.android.activities.FormDownloadList.FORM_ID_KEY;

public class FormDownloadListAdapter extends ArrayAdapter {

    private final ArrayList<HashMap<String, String>> filteredFormList;
    private HashMap<String, FormDetails> formIdsToDetails;

    public FormDownloadListAdapter(Context context, ArrayList<HashMap<String, String>> filteredFormList,
                                   HashMap<String, FormDetails> formIdsToDetails) {
        super(context, R.layout.form_chooser_list_item_multiple_choice, filteredFormList);
        this.filteredFormList = filteredFormList;
        this.formIdsToDetails = formIdsToDetails;
    }

    public void setFromIdsToDetails(HashMap<String, FormDetails> formIdsToDetails) {
        this.formIdsToDetails = formIdsToDetails;
    }

    private static class ViewHolder {
        TextView formTitle;
        TextView formSubtitle;
        TextView formUpdateAlert;
    }

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        if (row == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.form_chooser_list_item_multiple_choice, parent, false);

            holder.formTitle = row.findViewById(R.id.form_title);
            holder.formSubtitle = row.findViewById(R.id.form_subtitle);
            holder.formUpdateAlert = row.findViewById(R.id.form_update_alert);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final HashMap<String, String> formAtPosition = filteredFormList.get(position);
        final String formIDAtPosition = formAtPosition.get(FORM_ID_KEY);

        holder.formTitle.setText(formAtPosition.get(FORMNAME));
        holder.formSubtitle.setText(formAtPosition.get(FORMID_DISPLAY));

        if (formIdsToDetails.get(formIDAtPosition) != null
                && (formIdsToDetails.get(formIDAtPosition).isNewerFormVersionAvailable()
                || formIdsToDetails.get(formIDAtPosition).areNewerMediaFilesAvailable())) {
            holder.formUpdateAlert.setVisibility(View.VISIBLE);
        } else {
            holder.formUpdateAlert.setVisibility(View.GONE);
        }
        
        return row;
    }
}
