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

import android.app.ListActivity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;

abstract class AppListActivity extends ListActivity {
    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();

    private static final int MENU_SORT = Menu.FIRST;
    public static final int MENU_FILTER = MENU_SORT + 1;

    private static final String SELECTED_INSTANCES = "selectedInstances";
    private static final String IS_SEARCH_BOX_SHOWN = "isSearchBoxShown";

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mSearchBoxLayout;
    private EditText mInputSearch;

    protected SimpleCursorAdapter mListAdapter;
    protected LinkedHashSet<Long> mSelectedInstances = new LinkedHashSet<>();
    protected String[] mSortingOptions;

    private boolean mIsSearchBoxShown;

    protected Integer mSelectedSortingOrder;

    @Override
    protected void onResume() {
        super.onResume();
        mSearchBoxLayout = (LinearLayout) findViewById(R.id.searchBoxLayout);
        setupSearchBox();
        setupDrawer();
        setupDrawerItems();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_INSTANCES, mSelectedInstances);
        outState.putBoolean(IS_SEARCH_BOX_SHOWN, mSearchBoxLayout.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mSelectedInstances = (LinkedHashSet<Long>) state.getSerializable(SELECTED_INSTANCES);
        mIsSearchBoxShown = state.getBoolean(IS_SEARCH_BOX_SHOWN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_SORT, 0, R.string.sort_the_list)
                .setIcon(R.drawable.ic_sort)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu
                .add(0, MENU_FILTER, 0, R.string.filter_the_list)
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SORT:
                if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                    mDrawerLayout.closeDrawer(Gravity.END);
                } else {
                    Collect.getInstance().hideKeyboard(mInputSearch);
                    mDrawerLayout.openDrawer(Gravity.END);
                }
                return true;

            case MENU_FILTER:
                if (mSearchBoxLayout.getVisibility() == View.GONE) {
                    showSearchBox();
                } else {
                    hideSearchBox();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    private void setupSearchBox() {
        mInputSearch = (EditText) findViewById(R.id.inputSearch);
        mInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAdapter();
            }
        });

        if (mIsSearchBoxShown) {
            showSearchBox();
            updateAdapter();
        }
    }

    private void hideSearchBox() {
        mInputSearch.setText("");
        mSearchBoxLayout.setVisibility(View.GONE);
        Collect.getInstance().hideKeyboard(mInputSearch);
    }

    private void showSearchBox() {
        mSearchBoxLayout.setVisibility(View.VISIBLE);
        Collect.getInstance().showKeyboard(mInputSearch);
    }

    private void setupDrawerItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSortingOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                Log.e("selected ",mSelectedSortingOrder+"");
                if(position == mSelectedSortingOrder) {
                    textView.setBackgroundColor(Color.parseColor("#2196F3"));
                }
                textView.setPadding(50, 0, 0, 0);
                return textView;
            }
        };

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("selected ",mSelectedSortingOrder+"");
                parent.getChildAt(mSelectedSortingOrder).setBackgroundColor(Color.parseColor("#DDDDDD"));
                view.setBackgroundColor(Color.parseColor("#2196F3"));
                performSelectedSearch(position);
                mDrawerLayout.closeDrawer(Gravity.END);
            }
        });
    }

    private void performSelectedSearch(int position) {
        saveSelectedSortingOrder(position);
        updateAdapter();
    }

    private void setupDrawer() {
        mDrawerList = (ListView) findViewById(R.id.sortingMenu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.sorting_menu_open, R.string.sorting_menu_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    protected void checkPreviouslyCheckedItems() {
        getListView().clearChoices();
        List<Integer> selectedPositions = new ArrayList<>();
        int listViewPosition = 0;
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long instanceId = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                if (mSelectedInstances.contains(instanceId)) {
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

    /** Returns the IDs of the checked items, using the ListView provided */
    protected long[] getCheckedIds(ListView lv) {
        // This method could be simplified by using getCheckedItemIds, if one ensured that
        // IDs were “stable” (see the getCheckedItemIds doc).
        int itemCount = lv.getCount();
        int checkedItemCount = lv.getCheckedItemCount();
        long[] checkedIds = new long[checkedItemCount];
        int resultIndex = 0;
        for (int posIdx = 0; posIdx < itemCount; posIdx++) {
            if (lv.isItemChecked(posIdx)) {
                checkedIds      [resultIndex] = lv.getItemIdAtPosition(posIdx);
                resultIndex++;
            }
        }
        return checkedIds;
    }

    protected int getCheckedCount() {
        return getListView().getCheckedItemCount();
    }

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
        if (lv == null) return false;

        boolean newCheckState = lv.getCount() > lv.getCheckedItemCount();
        setAllToCheckedState(lv, newCheckState);
        return newCheckState;
    }

    public static void setAllToCheckedState(ListView lv, boolean check) {
        // no-op if ListView null
        if (lv == null) return;

        for (int x = 0; x < lv.getCount(); x++) {
            lv.setItemChecked(x, check);
        }
    }

    // Function to toggle button label
    public static void toggleButtonLabel(Button mToggleButton, ListView lv) {
        if (lv.getCheckedItemCount() != lv.getCount()) {
            mToggleButton.setText(R.string.select_all);
        } else {
            mToggleButton.setText(R.string.clear_all);
        }
    }
	
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
           mDrawerLayout.closeDrawer(Gravity.END);
        } else {
           super.onBackPressed();
        }
    }

    private void saveSelectedSortingOrder(int selectedStringOrder) {
        mSelectedSortingOrder = selectedStringOrder;
        PreferenceManager.getDefaultSharedPreferences(Collect.getInstance())
                .edit()
                .putInt(getSortingOrderKey(), selectedStringOrder)
                .apply();
    }

    protected void restoreSelectedSortingOrder() {
        mSelectedSortingOrder = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance())
                .getInt(getSortingOrderKey(), BY_NAME_ASC);
    }

    protected CharSequence getFilterText() {
        return mInputSearch != null ? mInputSearch.getText() : "";
    }
}
