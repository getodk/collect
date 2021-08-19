/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.SortDialogAdapter;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.SnackbarUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import timber.log.Timber;

abstract class AppListActivity extends CollectAbstractActivity {

    protected static final int LOADER_ID = 0x01;
    private static final String SELECTED_INSTANCES = "selectedInstances";
    private static final String IS_SEARCH_BOX_SHOWN = "isSearchBoxShown";
    private static final String SEARCH_TEXT = "searchText";

    protected CursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    protected int[] sortingOptions;
    protected Integer selectedSortingOrder;
    protected ListView listView;
    protected LinearLayout llParent;
    protected ProgressBar progressBar;
    private BottomSheetDialog bottomSheetDialog;

    private String filterText;
    private String savedFilterText;
    private boolean isSearchBoxShown;

    private SearchView searchView;

    private boolean canHideProgressBar;
    private boolean progressBarVisible;

    // toggles to all checked or all unchecked
    // returns:
    // true if result is all checked
    // false if result is all unchecked
    //
    // Toggle behavior is as follows:
    // if ANY items are unchecked, check them all
    // if ALL items are checked, uncheck them all
    public static boolean toggleChecked(ListView lv) {
        // shortcut null case
        if (lv == null) {
            return false;
        }

        boolean newCheckState = lv.getCount() > lv.getCheckedItemCount();
        setAllToCheckedState(lv, newCheckState);
        return newCheckState;
    }

    public static void setAllToCheckedState(ListView lv, boolean check) {
        // no-op if ListView null
        if (lv == null) {
            return;
        }

        for (int x = 0; x < lv.getCount(); x++) {
            lv.setItemChecked(x, check);
        }
    }

    // Function to toggle button label
    public static void toggleButtonLabel(Button toggleButton, ListView lv) {
        if (lv.getCheckedItemCount() != lv.getCount()) {
            toggleButton.setText(R.string.select_all);
        } else {
            toggleButton.setText(R.string.clear_all);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        listView = findViewById(android.R.id.list);
        listView.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        listView.setEmptyView(findViewById(android.R.id.empty));
        progressBar = findViewById(R.id.progressBar);
        llParent = findViewById(R.id.llParent);

        // Use the nicer-looking drawable with Material Design insets.
        listView.setDivider(ContextCompat.getDrawable(this, R.drawable.list_item_divider));
        listView.setDividerHeight(1);

        setSupportActionBar(findViewById(R.id.toolbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSelectedSortingOrder();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_INSTANCES, selectedInstances);

        if (searchView != null) {
            outState.putBoolean(IS_SEARCH_BOX_SHOWN, !searchView.isIconified());
            outState.putString(SEARCH_TEXT, String.valueOf(searchView.getQuery()));
        } else {
            Timber.e("Unexpected null search view (issue #1412)");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        selectedInstances = (LinkedHashSet<Long>) state.getSerializable(SELECTED_INSTANCES);
        isSearchBoxShown = state.getBoolean(IS_SEARCH_BOX_SHOWN);
        savedFilterText = state.getString(SEARCH_TEXT);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        final MenuItem sortItem = menu.findItem(R.id.menu_sort);
        final MenuItem searchItem = menu.findItem(R.id.menu_filter);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterText = query;
                updateAdapter();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterText = newText;
                updateAdapter();
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sortItem.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sortItem.setVisible(true);
                return true;
            }
        });

        if (isSearchBoxShown) {
            searchItem.expandActionView();
            searchView.setQuery(savedFilterText, false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_sort:
                showBottomSheetDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performSelectedSearch(int position) {
        saveSelectedSortingOrder(position);
        updateAdapter();
    }

    protected void checkPreviouslyCheckedItems() {
        listView.clearChoices();
        List<Integer> selectedPositions = new ArrayList<>();
        int listViewPosition = 0;
        Cursor cursor = listAdapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long instanceId = cursor.getLong(cursor.getColumnIndex(DatabaseInstanceColumns._ID));
                if (selectedInstances.contains(instanceId)) {
                    selectedPositions.add(listViewPosition);
                }
                listViewPosition++;
            } while (cursor.moveToNext());
        }

        for (int position : selectedPositions) {
            listView.setItemChecked(position, true);
        }
    }

    protected abstract void updateAdapter();

    protected abstract String getSortingOrderKey();

    protected boolean areCheckedItems() {
        return getCheckedCount() > 0;
    }

    protected int getCheckedCount() {
        return listView.getCheckedItemCount();
    }

    private void saveSelectedSortingOrder(int selectedStringOrder) {
        selectedSortingOrder = selectedStringOrder;
        settingsProvider.getGeneralSettings().save(getSortingOrderKey(), selectedStringOrder);
    }

    protected void restoreSelectedSortingOrder() {
        selectedSortingOrder = settingsProvider.getGeneralSettings().getInt(getSortingOrderKey());
    }

    protected int getSelectedSortingOrder() {
        if (selectedSortingOrder == null) {
            restoreSelectedSortingOrder();
        }
        return selectedSortingOrder;
    }

    protected CharSequence getFilterText() {
        return filterText != null ? filterText : "";
    }

    protected void clearSearchView() {
        searchView.setQuery("", false);
    }

    private void showBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        final View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);

        final SortDialogAdapter adapter = new SortDialogAdapter(this, recyclerView, sortingOptions, getSelectedSortingOrder(), new RecyclerViewClickListener() {
            @Override
            public void onItemClicked(SortDialogAdapter.ViewHolder holder, int position) {
                holder.updateItemColor(selectedSortingOrder);
                performSelectedSearch(position);
                bottomSheetDialog.dismiss();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    protected void showSnackbar(@NonNull String result) {
        SnackbarUtils.showShortSnackbar(llParent, result);
    }

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
}
