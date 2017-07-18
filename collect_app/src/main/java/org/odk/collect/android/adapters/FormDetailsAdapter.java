/*
 * Copyright (C) 2011 Smap Consulting Pty Ltd
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
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;

public class FormDetailsAdapter implements SpinnerAdapter {

	private ArrayList<FormDetails> data;
	private Context context; 
	private int layoutResourceId;
	
	public FormDetailsAdapter(ArrayList<FormDetails> data) {
		this.data = data;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView v = new TextView(context);
        v.setText(data.get(position).formName);
        
        return v;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return Long.parseLong(data.get(position).formID);
	}

	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return android.R.layout.simple_spinner_dropdown_item;
	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return data.isEmpty();
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return this.getView(position, convertView, parent);
	}
	
}
