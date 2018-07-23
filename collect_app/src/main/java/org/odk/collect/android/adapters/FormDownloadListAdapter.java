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
import android.widget.CheckBox;
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
        super(context, R.layout.two_item_multiple_choice, filteredFormList);
        this.filteredFormList = filteredFormList;
        this.formIdsToDetails = formIdsToDetails;
    }

    public void setFromIdsToDetails(HashMap<String, FormDetails> formIdsToDetails) {
        this.formIdsToDetails = formIdsToDetails;
    }

    private class ViewHolder {
        TextView text1;
        TextView text2;
        TextView updateInfo;
        CheckBox checkBox;
    }

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        if (row == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.two_item_multiple_choice, parent, false);

            holder.text1 = row.findViewById(R.id.text1);
            holder.text2 = row.findViewById(R.id.text2);
            holder.updateInfo = row.findViewById(R.id.update_info);
            holder.checkBox = row.findViewById(R.id.checkbox);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final HashMap<String, String> formAtPosition = filteredFormList.get(position);
        final String formIDAtPosition = formAtPosition.get(FORM_ID_KEY);

        holder.text1.setText(formAtPosition.get(FORMNAME));
        holder.text2.setText(formAtPosition.get(FORMID_DISPLAY));

        if (formIdsToDetails.get(formIDAtPosition) != null
                && (formIdsToDetails.get(formIDAtPosition).isNewerFormVersionAvailable()
                || formIdsToDetails.get(formIDAtPosition).areNewerMediaFilesAvailable())) {
            holder.updateInfo.setVisibility(View.VISIBLE);
        } else {
            holder.updateInfo.setVisibility(View.GONE);
        }
        
        return row;
    }
}