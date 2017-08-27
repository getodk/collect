/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
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

package org.odk.collect.android.fragments;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.SmapMain;
import org.odk.collect.android.adapters.SortDialogAdapter;
import org.odk.collect.android.adapters.TaskListArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.loaders.TaskLoader;
import org.odk.collect.android.preferences.AboutPreferencesActivity;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.LinkedHashSet;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 */
public class SmapTaskListFragment extends ListFragment
        implements //View.OnClickListener,
        LoaderManager.LoaderCallbacks<List<TaskEntry>> {

    // request codes for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;
    private static final int TASK_LOADER_ID = 1;
    private static final int INSTANCE_UPLOADER = 2;

    private static final int MENU_SORT = Menu.FIRST;
    private static final int MENU_FILTER = Menu.FIRST + 1;
    private static final int MENU_ENTERDATA = Menu.FIRST + 2;
    private static final int MENU_GETFORMS = Menu.FIRST + 3;
    private static final int MENU_SENDDATA = Menu.FIRST + 4;
    private static final int MENU_MANAGEFILES = Menu.FIRST + 5;

    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();
    protected String[] sortingOptions;
    View rootView;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private TaskLoader mTaskLoader;

    protected LinearLayout searchBoxLayout;
    protected SimpleCursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    protected EditText inputSearch;

    private Integer selectedSortingOrder;
    private BottomSheetDialog bottomSheetDialog;

    private static final String TASK_MANAGER_LIST_SORTING_ORDER = "taskManagerListSortingOrder";

    DeleteInstancesTask deleteInstancesTask = null;
    private AlertDialog alertDialog;
    private InstanceSyncTask instanceSyncTask;

    private TaskListArrayAdapter mAdapter;

    public static SmapTaskListFragment newInstance() {
        return new SmapTaskListFragment();
    }

    public SmapTaskListFragment() {
    }

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.smap_task_layout, container, false);

        setHasOptionsMenu(true);
        searchBoxLayout = (LinearLayout) rootView.findViewById(R.id.searchBoxLayout);
        setupSearchBox(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle b) {
        super.onActivityCreated(b);

        Timber.i("############### onActivityCreated: " + mAdapter);
        mAdapter = new TaskListArrayAdapter(getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        super.onViewCreated(rootView, savedInstanceState);

    }


    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher);

        if (bottomSheetDialog == null) {
            setupBottomSheet();
        }
    }

    private void setupBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MaterialDialogSheet);
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final RecyclerView recyclerView = (RecyclerView) sheetView.findViewById(R.id.recyclerView);

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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        /*
        if (!isVisibleToUser) {
            // close the drawer if open
            if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END);
            }
        }
        */
    }

    @Override
    public Loader<List<TaskEntry>> onCreateLoader(int id, Bundle args) {
        mTaskLoader = new TaskLoader(getContext());
        return mTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<TaskEntry>> loader, List<TaskEntry> data) {

        mAdapter.setData(data);
        ((SmapMain) getActivity()).setLocationTriggers(data, false);      // NFC and geofence triggers

    }

    @Override
    public void onLoaderReset(Loader<List<TaskEntry>> loader) {
        mAdapter.setData(null);
    }

    //@Override
    //public void syncComplete(String result) {
    //    TextView textView = (TextView) rootView.findViewById(R.id.status_text);
    //    textView.setText(result);
    //}

    /*
    private void setupAdapter() {

        String[] data = new String[]{InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
        int[] view = new int[]{R.id.text1, R.id.text2};

        listAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.two_item_multiple_choice, getCursor(), data, view);
        setListAdapter(listAdapter);
    }
    */

    protected String getSortingOrderKey() {
        return TASK_MANAGER_LIST_SORTING_ORDER;
    }

    private Cursor getCursor() {
        return new InstancesDao().getSavedInstancesCursor(getFilterText(), getSortingOrder());
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId) {
        super.onListItemClick(l, v, position, rowId);

        TaskEntry entry = (TaskEntry) getListAdapter().getItem(position);

        if (entry.type.equals("task")) {
            if (entry.locationTrigger != null && entry.locationTrigger.length() > 0) {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.smap_must_start_from_nfc),
                        Toast.LENGTH_SHORT).show();
            } else {
                ((SmapMain) getActivity()).completeTask(entry);
            }
        } else {

            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, entry.id);

            // Use an explicit intent
            Intent i = new Intent(getActivity(), org.odk.collect.android.activities.FormEntryActivity.class);
            i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
            i.setData(formUri);
            startActivity(i);

            //startActivity(new Intent(Intent.ACTION_EDIT, formUri));
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");

        getActivity().getMenuInflater().inflate(R.menu.smap_menu, menu);

        menu
                .add(0, MENU_SORT, 0, R.string.sort_the_list)
                .setIcon(R.drawable.ic_sort_black_36dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu
                .add(0, MENU_FILTER, 0, R.string.filter_the_list)
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        boolean odkMenus = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getBoolean(PreferenceKeys.KEY_SMAP_ODK_STYLE_MENUS, true);

        if(odkMenus) {
            menu
                    .add(0, MENU_ENTERDATA, 0, R.string.enter_data)
                    .setIcon(android.R.drawable.ic_menu_edit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            menu
                    .add(0, MENU_GETFORMS, 0, R.string.get_forms)
                    .setIcon(android.R.drawable.ic_input_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            menu
                    .add(0, MENU_SENDDATA, 0, R.string.send_data)
                    .setIcon(android.R.drawable.ic_menu_send)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            menu
                    .add(0, MENU_MANAGEFILES, 0, R.string.manage_files)
                    .setIcon(android.R.drawable.ic_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                Collect.getInstance()
                        .getActivityLogger()
                        .logAction(this, "onOptionsItemSelected",
                                "MENU_ABOUT");
                Intent aboutIntent = new Intent(getActivity(), AboutPreferencesActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.menu_general_preferences:
                Collect.getInstance()
                        .getActivityLogger()
                        .logAction(this, "onOptionsItemSelected",
                                "MENU_PREFERENCES");
                Intent ig = new Intent(getActivity(), PreferencesActivity.class);
                startActivity(ig);
                return true;
            case R.id.menu_gettasks:
                ((SmapMain) getActivity()).processGetTask();
                return true;
            case MENU_ENTERDATA:
                processEnterData();
                return true;
            case MENU_GETFORMS:
                processGetForms();
                return true;
            case MENU_SENDDATA:
                processSendData();
                return true;
            case MENU_MANAGEFILES:
                processManageFiles();
                return true;
            case MENU_SORT:
                /*
                if (drawerLayout.isDrawerOpen(Gravity.END)) {
                    drawerLayout.closeDrawer(Gravity.END);
                } else {
                    Collect.getInstance().hideKeyboard(inputSearch);
                    drawerLayout.openDrawer(Gravity.END);
                }
                return true;
                */
                Collect.getInstance().hideKeyboard(inputSearch);
                bottomSheetDialog.show();
                return true;

            case MENU_FILTER:
                if (searchBoxLayout.getVisibility() == View.GONE) {
                    showSearchBox();
                } else {
                    hideSearchBox();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performSelectedSearch(int position) {
        saveSelectedSortingOrder(position);
        updateAdapter();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //if (drawerToggle != null) {
        //    drawerToggle.onConfigurationChanged(newConfig);
        //}
    }

    /*
    private void setupDrawerItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, sortingOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                if (position == getSelectedSortingOrder()) {
                    textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tintColor));
                }
                textView.setPadding(50, 0, 0, 0);
                return textView;
            }
        };
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parent.getChildAt(selectedSortingOrder).setBackgroundColor(Color.TRANSPARENT);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tintColor));
                performSelectedSort(position);
                drawerLayout.closeDrawer(Gravity.END);
            }
        });
    }

    private void setupDrawer(View rootView) {
        drawerList = (ListView) rootView.findViewById(R.id.sortingMenu);
        drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(), drawerLayout,
                R.string.clear_answer_ask, R.string.clear_answer_no) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActivity().invalidateOptionsMenu();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
    }
    */

    private void hideSearchBox() {
        inputSearch.setText("");
        searchBoxLayout.setVisibility(View.GONE);
        Collect.getInstance().hideKeyboard(inputSearch);
    }

    private void showSearchBox() {
        searchBoxLayout.setVisibility(View.VISIBLE);
        Collect.getInstance().showKeyboard(inputSearch);
    }


    private void performSelectedSort(int position) {
        saveSelectedSortingOrder(position);
        updateAdapter();
    }

    //to get present drawer status
    public Boolean getDrawerStatus() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.END);
    }

    protected String getSortingOrder() {
        //String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
        String sortOrder = "BY_NAME_ASC";
        switch (getSelectedSortingOrder()) {

            case BY_NAME_ASC:
                //sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                sortOrder = "BY_NAME_ASC";
                break;
            case BY_NAME_DESC:
                //sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " DESC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                sortOrder = "BY_NAME_DESC";
                break;
            case BY_DATE_ASC:
                //sortOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                sortOrder = "BY_DATE_ASC";
                break;
            case BY_DATE_DESC:
                //sortOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                sortOrder = "BY_DATE_DESC";
                break;
        }
        return sortOrder;
    }

    private void setupSearchBox(View view) {
        inputSearch = (EditText) view.findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
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
    }

    private void saveSelectedSortingOrder(int selectedStringOrder) {
        selectedSortingOrder = selectedStringOrder;
        PreferenceManager.getDefaultSharedPreferences(Collect.getInstance())
                .edit()
                .putInt(getSortingOrderKey(), selectedStringOrder)
                .apply();
    }

    protected void restoreSelectedSortingOrder() {
        selectedSortingOrder = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getInt(getSortingOrderKey(), BY_NAME_ASC);
    }

    protected int getSelectedSortingOrder() {
        if (selectedSortingOrder == null) {
            restoreSelectedSortingOrder();
        }
        return selectedSortingOrder;
    }

    protected CharSequence getFilterText() {
        return inputSearch != null ? inputSearch.getText() : "";
    }

    protected void updateAdapter() {
        Timber.i("################ update adapter");
        if(mTaskLoader != null) {
            mTaskLoader.updateSortOrder(getSortingOrder());
            mTaskLoader.updateFilter(getFilterText());
            mTaskLoader.forceLoad();
        }
    }

    private void processEnterData() {
        Intent i = new Intent(getContext(), org.odk.collect.android.activities.FormChooserList.class);
        startActivityForResult(i, FORM_CHOOSER);
    }

    // Get new forms
    private void processGetForms() {

        Collect.getInstance().getActivityLogger().logAction(this, "downloadBlankForms", "click");
        Intent i = new Intent(getContext(), FormDownloadList.class);
        startActivity(i);
    }

    // Send data
    private void processSendData() {
        Intent i = new Intent(getContext(), org.odk.collect.android.activities.InstanceUploaderList.class);
        startActivityForResult(i, INSTANCE_UPLOADER);
    }

    private void processManageFiles() {
        Intent i = new Intent(getContext(), org.odk.collect.android.activities.FileManagerTabs.class);
        startActivity(i);
    }

}
