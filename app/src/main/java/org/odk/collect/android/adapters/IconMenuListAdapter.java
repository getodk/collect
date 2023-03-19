/*
 * Copyright 2017 Yura Laguta
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.model.IconMenuItem;

import java.util.List;

/**
 * Adapter for List of options with icons
 */
public class IconMenuListAdapter extends BaseAdapter {

    private final Context context;
    private final List<IconMenuItem> items;

    public IconMenuListAdapter(Context context, List<IconMenuItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!(convertView instanceof TextView)) {
            convertView = createView(parent);
        }
        refreshView((IconMenuItem) getItem(position), (TextView) convertView);
        return convertView;
    }

    private View createView(ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_view_option, parent, false);
    }

    private void refreshView(IconMenuItem item, TextView convertView) {
        convertView.setText(item.getTextResId());
        convertView.setCompoundDrawablesRelativeWithIntrinsicBounds(item.getImageResId(), 0, 0, 0);
    }
}
