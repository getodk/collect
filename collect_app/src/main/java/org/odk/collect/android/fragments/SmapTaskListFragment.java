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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
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

    private static final int MENU_ENTERDATA = Menu.FIRST + 2;
    private static final int MENU_GETFORMS = Menu.FIRST + 3;
    private static final int MENU_SENDDATA = Menu.FIRST + 4;
    private static final int MENU_MANAGEFILES = Menu.FIRST + 5;

    private static final int PASSWORD_DIALOG = 1;

    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();
    protected String[] sortingOptions;
    View rootView;

    private TaskLoader mTaskLoader;

    protected LinearLayout searchBoxLayout;
    protected SimpleCursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    protected EditText inputSearch;
    private String filterText;

    private Integer selectedSortingOrder;
    private BottomSheetDialog bottomSheetDialog;

    private static final String TASK_MANAGER_LIST_SORTING_ORDER = "taskManagerListSortingOrder";

    private SharedPreferences adminPreferences;

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
        //searchBoxLayout = (LinearLayout) rootView.findViewById(R.id.searchBoxLayout);
        //setupSearchBox(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle b) {
        super.onActivityCreated(b);

        mAdapter = new TaskListArrayAdapter(getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc)
        };

        // Handle long item clicks
        ListView lv = getListView();
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                return onLongListItemClick(v,pos,id);
            }
        });

        adminPreferences = getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

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

        super.onCreateOptionsMenu(menu, inflater);

        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");

        getActivity().getMenuInflater().inflate(R.menu.smap_menu, menu);


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

        final MenuItem sortItem = menu.findItem(R.id.menu_sort);
        final MenuItem searchItem = menu.findItem(R.id.menu_filter);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
            case R.id.menu_admin_preferences:
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "onOptionsItemSelected", "MENU_ADMIN");
                String pw = adminPreferences.getString(
                        AdminKeys.KEY_ADMIN_PW, "");
                if ("".equalsIgnoreCase(pw)) {
                    Intent i = new Intent(getActivity(),
                            AdminPreferencesActivity.class);
                    startActivity(i);
                } else {
                    getActivity().showDialog(PASSWORD_DIALOG);
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "createAdminPasswordDialog", "show");
                }
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected String getSortingOrder() {
        String sortOrder = "BY_NAME_ASC";
        switch (getSelectedSortingOrder()) {

            case BY_NAME_ASC:
                sortOrder = "BY_NAME_ASC";
                break;
            case BY_NAME_DESC:
                sortOrder = "BY_NAME_DESC";
                break;
            case BY_DATE_ASC:
                sortOrder = "BY_DATE_ASC";
                break;
            case BY_DATE_DESC:
                sortOrder = "BY_DATE_DESC";
                break;
        }
        return sortOrder;
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
        return filterText != null ? filterText : "";
        //return inputSearch != null ? inputSearch.getText() : "";
    }

    protected void updateAdapter() {
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

    /*
     * Handle a long click on a list item
     */
    protected boolean onLongListItemClick(View v, int position, long id) {

        TaskEntry task = (TaskEntry) getListAdapter().getItem(position);

        if(task.type.equals("task")) {
            Intent i = new Intent(getActivity(), org.odk.collect.android.activities.TaskAddressActivity.class);
            i.putExtra("id", task.id);

            startActivity(i);
        }
        return true;
    }
}
