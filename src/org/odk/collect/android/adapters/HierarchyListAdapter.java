package org.odk.collect.android.adapters;

import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.views.HierarchyElementView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class HierarchyListAdapter extends BaseAdapter {

    private Context mContext;
    private List<HierarchyElement> mItems = new ArrayList<HierarchyElement>();


    public HierarchyListAdapter(Context context) {
        mContext = context;
    }


    public int getCount() {
        return mItems.size();
    }


    public Object getItem(int position) {
        return mItems.get(position);
    }


    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        HierarchyElementView hev;
        if (convertView == null) {
            hev = new HierarchyElementView(mContext, mItems.get(position));
        } else {
            hev = (HierarchyElementView) convertView;
            hev.setPrimaryText(mItems.get(position).getPrimaryText());
            hev.setSecondaryText(mItems.get(position).getSecondaryText());
            hev.setIcon(mItems.get(position).getIcon());
        }
        return hev;

    }


    public void setListItems(List<HierarchyElement> it) {
        mItems = it;
    }

}
