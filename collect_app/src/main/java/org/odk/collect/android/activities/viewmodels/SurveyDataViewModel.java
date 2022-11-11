package org.odk.collect.android.activities.viewmodels;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.SurveyData;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class SurveyDataViewModel extends ViewModel {

    private final SharedPreferences sharedPreferences;
    private MutableLiveData<SurveyData> surveyData;

    private CharSequence filter = "";

    public SurveyDataViewModel(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public LiveData<SurveyData> getSurveyData() {
        if (surveyData == null) {
            surveyData = new MutableLiveData<SurveyData>();
            loadData();
        }
        return surveyData;
    }

    public void loadData() {

        int taskSortOrder = getTaskSortingOrder();
        SurveyData data = new SurveyData();

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                // on background thread, obtain a fresh set of data
                // Create corresponding array of entries and load their labels.
                data.points = new ArrayList<PointEntry>(100);
                data.tasks = new ArrayList<TaskEntry>(10);
                TraceUtilities.getPoints(data.points, 500, true);
                getForms(data.tasks);
                Utilities.getTasks(data.tasks, false, taskSortOrder, filter.toString(), false, true, false);

                Timber.i("-------------------------------------- Retrieved data");
                surveyData.postValue(data);
            }
        });

        Timber.i("------------------------------------- load data");

    }

    private void getForms(ArrayList<TaskEntry> entries) {

        String[] proj = {FormsProviderAPI.FormsColumns._ID,
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                FormsProviderAPI.FormsColumns.PROJECT,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME};
        //FormsProviderAPI.FormsColumns.GEOMETRY_XPATH};  // smap


        String selectClause = "(lower(" + FormsProviderAPI.FormsColumns.SOURCE + ")='" + Utilities.getSource() + "' or " +
                FormsProviderAPI.FormsColumns.SOURCE + " is null)" +
                " and " + FormsProviderAPI.FormsColumns.TASKS_ONLY + " = 'no'";

        String[] selectArgs = null;
        if (filter.toString().trim().length() > 0) {
            selectClause += " and " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " LIKE ?";
            selectArgs = new String[]{"%" + filter + "%"};
        }

        int formSortOrder = getFormSortingOrder();
        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor formListCursor = resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, proj, selectClause, selectArgs, getFormSortOrderExpr(formSortOrder));


        if (formListCursor != null) {

            formListCursor.moveToFirst();
            while (!formListCursor.isAfterLast()) {

                TaskEntry entry = new TaskEntry();

                entry.type = "form";
                entry.ident = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
                entry.formVersion = formListCursor.getInt(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
                entry.name = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
                entry.project = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.PROJECT));
                //entry.geometryXPath = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH));   // smap
                entry.id = formListCursor.getLong(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID));

                entries.add(entry);
                formListCursor.moveToNext();
            }
        }
        if (formListCursor != null) {
            formListCursor.close();
        }
    }

    /*
     * Change filter
     */
    public void updateFilter(CharSequence filter) {
        this.filter = filter;
    }

    private String getFormSortOrderExpr(int sortOrder) {
        String sortOrderExpr = "";

        switch (sortOrder) {
            case ApplicationConstants.SortingOrder.BY_NAME_ASC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
                break;
            case ApplicationConstants.SortingOrder.BY_NAME_DESC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
                break;
            case ApplicationConstants.SortingOrder.BY_DATE_ASC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " ASC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
                break;
            case ApplicationConstants.SortingOrder.BY_DATE_DESC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " DESC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
                break;
            case ApplicationConstants.SortingOrder.BY_STATUS_ASC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.PROJECT + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
                break;
            case ApplicationConstants.SortingOrder.BY_STATUS_DESC:
                sortOrderExpr = FormsProviderAPI.FormsColumns.PROJECT + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
                break;
        }

        return sortOrderExpr;
    }

    private static final String TASK_MANAGER_LIST_SORTING_ORDER = "taskManagerListSortingOrder";
    private static final String FORM_MANAGER_LIST_SORTING_ORDER = "formManagerListSortingOrder";

    public void saveTaskSelectedSortingOrder(int selectedStringOrder) {
        saveSortOrder(TASK_MANAGER_LIST_SORTING_ORDER, selectedStringOrder);
    }

    public void saveFormSelectedSortingOrder(int selectedStringOrder) {
        saveSortOrder(FORM_MANAGER_LIST_SORTING_ORDER, selectedStringOrder);
    }

    private void saveSortOrder(String key, int sortOrder) {
        sharedPreferences
                .edit()
                .putInt(key, sortOrder)
                .apply();
    }

    public int getFormSortingOrder() {
        return sharedPreferences.getInt(
                FORM_MANAGER_LIST_SORTING_ORDER,
                ApplicationConstants.SortingOrder.BY_NAME_ASC
        );
    }

    public int getTaskSortingOrder() {
        return sharedPreferences.getInt(
                TASK_MANAGER_LIST_SORTING_ORDER,
                ApplicationConstants.SortingOrder.BY_NAME_ASC
        );
    }

}
