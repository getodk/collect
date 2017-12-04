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

    private ArrayList<HashMap<String, String>> filteredFormList;
    private HashMap<String, FormDetails> formNamesAndURLs;

    public FormDownloadListAdapter(Context context, ArrayList<HashMap<String, String>> filteredFormList, HashMap<String, FormDetails> formNamesAndURLs) {
        super(context, R.layout.two_item_multiple_choice, filteredFormList);
        this.filteredFormList = filteredFormList;
        this.formNamesAndURLs = formNamesAndURLs;
    }

    private class ViewHolder {
        TextView text1;
        TextView text2;
        TextView updateWarning;
        CheckBox checkBox;
    }

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        if (row == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.two_item_multiple_choice, parent, false);

            holder.text1 = (TextView) row.findViewById(R.id.text1);
            holder.text2 = (TextView) row.findViewById(R.id.text2);
            holder.updateWarning = (TextView) row.findViewById(R.id.update_warning);
            holder.checkBox = (CheckBox) row.findViewById(R.id.checkbox);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.text1.setText(filteredFormList.get(position).get(FORMNAME));
        holder.text2.setText(filteredFormList.get(position).get(FORMID_DISPLAY));

        boolean isNewerFormVersionAvailable = formNamesAndURLs.get(filteredFormList.get(position).get(FORM_ID_KEY)).isNewerFormVersionAvailable;
        boolean areNewerMediaFilesAvailable = formNamesAndURLs.get(filteredFormList.get(position).get(FORM_ID_KEY)).areNewerMediaFilesAvailable;

        holder.updateWarning.setVisibility(isNewerFormVersionAvailable || areNewerMediaFilesAvailable ? View.VISIBLE : View.GONE);
        if (isNewerFormVersionAvailable) {
            holder.updateWarning.setText(R.string.newer_version_of_this_form_is_available);
        } else if (areNewerMediaFilesAvailable) {
            holder.updateWarning.setText(R.string.newer_versions_of_media_files_are_available);
        }
        return row;
    }
}