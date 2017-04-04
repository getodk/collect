/*

Copyright 2017 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.collect.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.odk.collect.android.R;


public class FileManagerFragment extends AppListFragment {
    protected Button mDeleteButton;
    protected Button mToggleButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_layout, container, false);
        mDeleteButton = (Button) rootView.findViewById(R.id.delete_button);
        mDeleteButton.setText(getString(R.string.delete_file));
        mToggleButton = (Button) rootView.findViewById(R.id.toggle_button);

        setHasOptionsMenu(true);
        mSearchBoxLayout = (LinearLayout) rootView.findViewById(R.id.searchBoxLayout);
        setupSearchBox(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mDeleteButton.setEnabled(false);

        if (getListView().getCount() == 0) {
            mToggleButton.setEnabled(false);
        }
        mSortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
        mDeleteButton.setEnabled(areCheckedItems());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
        logger.logAction(this, "onListItemClick", Long.toString(rowId));

        if (getListView().isItemChecked(position)) {
            mSelectedInstances.add(getListView().getItemIdAtPosition(position));
        } else {
            mSelectedInstances.remove(getListView().getItemIdAtPosition(position));
        }

        toggleButtonLabel(mToggleButton, getListView());
        mDeleteButton.setEnabled(areCheckedItems());
    }

    private void setupSearchBox(View view) {
        mInputSearch = (EditText) view.findViewById(R.id.inputSearch);
        mInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s);
            }
        });
    }

    @Override
    protected void filter(CharSequence charSequence) {
        checkPreviouslyCheckedItems();
        mDeleteButton.setEnabled(areCheckedItems());
    }


    @Override
    protected void sortByNameAsc() {

    }

    @Override
    protected void sortByNameDesc() {

    }

    @Override
    protected void sortByDateAsc() {

    }

    @Override
    protected void sortByDateDesc() {

    }

    @Override
    protected void sortByStatusAsc() {

    }

    @Override
    protected void sortByStatusDesc() {

    }

    @Override
    protected void setupAdapter() {

    }
}
