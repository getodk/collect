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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.adapters.SortDialogAdapter;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ThemeUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import timber.log.Timber;

public abstract class AppListFragment extends ListFragment {

    @Inject
    SettingsProvider settingsProvider;

    protected int[] sortingOptions;
    protected SimpleCursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    protected View rootView;
    private Integer selectedSortingOrder;
    private BottomSheetDialog bottomSheetDialog;
    private String filterText;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(requireActivity()).inject(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        listView.setDivider(ContextCompat.getDrawable(getContext(), R.drawable.list_item_divider));
        listView.setDividerHeight(1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.list_menu, menu);

        final MenuItem sortItem = menu.findItem(R.id.menu_sort);
        final MenuItem searchItem = menu.findItem(R.id.menu_filter);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(new ThemeUtils(getContext()).getColorOnPrimary());
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_sort:
                bottomSheetDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performSelectedSearch(int position) {
        saveSelectedSortingOrder(position);
        updateAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bottomSheetDialog == null) {
            setupBottomSheet();
        }
    }

    private void setupBottomSheet() {
        CollectAbstractActivity activity = (CollectAbstractActivity) getActivity();
        if (activity == null) {
            Timber.e("Activity is null");
            return;
        }

        bottomSheetDialog = new BottomSheetDialog(activity, new ThemeUtils(getContext()).getBottomDialogTheme());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);

        final SortDialogAdapter adapter = new SortDialogAdapter(getActivity(), recyclerView, sortingOptions, getSelectedSortingOrder(), new RecyclerViewClickListener() {
            @Override
            public void onItemClicked(SortDialogAdapter.ViewHolder holder, int position) {
                holder.updateItemColor(selectedSortingOrder);
                performSelectedSearch(position);
                bottomSheetDialog.dismiss();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bottomSheetDialog.setContentView(sheetView);
    }

    protected void checkPreviouslyCheckedItems() {
        getListView().clearChoices();
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
            getListView().setItemChecked(position, true);
        }
    }

    protected abstract void updateAdapter();

    protected abstract String getSortingOrderKey();

    protected boolean areCheckedItems() {
        return getCheckedCount() > 0;
    }

    /**
     * Returns the IDs of the checked items, as an array of Long
     */
    protected Long[] getCheckedIdObjects() {
        // This method could be simplified by using getCheckedItemIds, if one ensured that
        // IDs were “stable” (see the getCheckedItemIds doc).
        ListView lv = getListView();
        int itemCount = lv.getCount();
        int checkedItemCount = lv.getCheckedItemCount();
        Long[] checkedIds = new Long[checkedItemCount];
        int resultIndex = 0;
        for (int posIdx = 0; posIdx < itemCount; posIdx++) {
            if (lv.isItemChecked(posIdx)) {
                checkedIds[resultIndex] = lv.getItemIdAtPosition(posIdx);
                resultIndex++;
            }
        }
        return checkedIds;
    }

    protected int getCheckedCount() {
        return getListView().getCheckedItemCount();
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
}
