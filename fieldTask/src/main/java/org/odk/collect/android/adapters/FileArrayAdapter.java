/*
 * Copyright (C) 2009 University of Washington
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.DriveListItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<DriveListItem> {

    private Context c;
    private int id;
    private List<DriveListItem> items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<DriveListItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public DriveListItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }
              
               /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        final DriveListItem o = items.get(position);
        if (o != null) {

            String dateModified = null;
            if (o.getDate() != null) {
                dateModified = new SimpleDateFormat(getContext().getString(
                        R.string.modified_on_date_at_time), Locale.getDefault())
                        .format(new Date(o.getDate().getValue()));
            }


            TextView t1 = (TextView) v.findViewById(R.id.text1);
            TextView t2 = (TextView) v.findViewById(R.id.text2);
            ImageView iv = (ImageView) v.findViewById(R.id.image);
            CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);

            if (o.getType() == 1) {
                Drawable d = c.getResources().getDrawable(R.drawable.ic_download);
                iv.setImageDrawable(d);
                cb.setVisibility(View.VISIBLE);
            }
            if (o.getType() == 3) {
                Drawable d = c.getResources().getDrawable(R.drawable.ic_back);
                iv.setImageDrawable(d);
            }
            if (o.getType() == 2 || o.getType() == 4 || o.getType() == 5) {
                Drawable d = c.getResources().getDrawable(R.drawable.ic_folder);
                iv.setImageDrawable(d);
            }

            if (t1 != null)
                t1.setText(o.getName());
            if (t2 != null)
                t2.setText(dateModified);

        }
        return v;
    }
}