package org.odk.collect.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.odk.collect.android.R;

/**
 * Created by shobhit on 12/3/17.
 */

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
        toggleButtonLabel(mToggleButton, getListView());
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
    protected void setupAdapter(String sortOrder) {

    }
}
