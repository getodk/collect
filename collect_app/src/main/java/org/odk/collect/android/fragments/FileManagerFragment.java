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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.SnackbarUtils;


public abstract class FileManagerFragment extends AppListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 0x01;
    protected Button deleteButton;
    protected Button toggleButton;
    protected LinearLayout llParent;
    protected ProgressBar progressBar;
    protected boolean canHideProgressBar;
    private boolean progressBarVisible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_layout, container, false);
        deleteButton = rootView.findViewById(R.id.delete_button);
        deleteButton.setText(getString(R.string.delete_file));
        toggleButton = rootView.findViewById(R.id.toggle_button);
        llParent = rootView.findViewById(R.id.llParent);
        progressBar = getActivity().findViewById(R.id.progressBar);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        deleteButton.setEnabled(false);

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
        deleteButton.setEnabled(areCheckedItems());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);
        logger.logAction(this, "onListItemClick", Long.toString(rowId));

        if (getListView().isItemChecked(position)) {
            selectedInstances.add(getListView().getItemIdAtPosition(position));
        } else {
            selectedInstances.remove(getListView().getItemIdAtPosition(position));
        }

        toggleButtonLabel(toggleButton, getListView());
        deleteButton.setEnabled(areCheckedItems());
    }

    @Override
    protected void updateAdapter() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        return getCursorLoader();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarIfAllowed();
        listAdapter.swapCursor(cursor);

        checkPreviouslyCheckedItems();
        toggleButtonLabel(toggleButton, getListView());
        deleteButton.setEnabled(areCheckedItems());

        if (getListView().getCount() == 0) {
            toggleButton.setEnabled(false);
        } else {
            toggleButton.setEnabled(true);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }

    protected abstract CursorLoader getCursorLoader();

    protected void hideProgressBarIfAllowed() {
        if (canHideProgressBar && progressBarVisible) {
            hideProgressBar();
        }
    }

    protected void hideProgressBarAndAllow() {
        this.canHideProgressBar = true;
        hideProgressBar();
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        progressBarVisible = false;
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        progressBarVisible = true;
    }

    protected void showSnackbar(@NonNull String result) {
        SnackbarUtils.showSnackbar(llParent, result);
    }
}
