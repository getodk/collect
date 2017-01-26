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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.odk.collect.android.R;

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private List<ListElement> mOriginalData = null;
    private List<ListElement> mFilteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();

    public SearchableAdapter(Context context, List<ListElement> data) {
        mFilteredData = data ;
        mOriginalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return mFilteredData.size();
    }

    public Object getItem(int position) {
        return mFilteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getOriginalItemsSize() {
        return mOriginalData.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.two_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.text1);
            holder.subText = (TextView) convertView.findViewById(R.id.text2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(mFilteredData.get(position).getName());
        holder.subText.setText(mFilteredData.get(position).getSubtext());

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView subText;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<ListElement> list = mOriginalData;

            int count = list.size();
            final ArrayList<ListElement> nlist = new ArrayList<>(count);

            long id;
            String filterableString ;
            String subtext;

            for (int i = 0; i < count; i++) {
                id = list.get(i).getId();
                filterableString = list.get(i).getName();
                subtext = list.get(i).getSubtext();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(new ListElement(id, filterableString, subtext));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredData = (ArrayList<ListElement>) results.values;
            notifyDataSetChanged();
        }
    }

    public static class ListElement {
        private long mId;
        private String mName;
        private String mSubtext;

        public ListElement(long id, String name, String subtext) {
            mId = id;
            mName = name;
            mSubtext = subtext;
        }

        public long getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        public String getSubtext() {
            return mSubtext;
        }
    }
}
