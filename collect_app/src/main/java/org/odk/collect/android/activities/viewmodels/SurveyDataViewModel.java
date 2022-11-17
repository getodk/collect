package org.odk.collect.android.activities.viewmodels;

import android.content.ContentResolver;
import android.database.Cursor;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.SurveyData;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class SurveyDataViewModel extends ViewModel {

    private MutableLiveData<SurveyData> surveyData;

    private String formSortOrder = "BY_NAME_ASC";
    private String taskSortOrder = "BY_NAME_ASC";
    private CharSequence filter = "";

    public LiveData<SurveyData> getSurveyData() {
        if (surveyData == null) {
            surveyData = new MutableLiveData<SurveyData>();
            loadData();
        }
        return surveyData;
    }

    public void loadData() {

        SurveyData data = new SurveyData();

        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                // on background thread, obtain a fresh set of data
                // Create corresponding array of entries and load their labels.
                data.points = new ArrayList<PointEntry>(100);
                data.tasks = new ArrayList<TaskEntry> (10);
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

        String [] proj = {FormsProviderAPI.FormsColumns._ID,
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                FormsProviderAPI.FormsColumns.PROJECT,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                FormsProviderAPI.FormsColumns.READ_ONLY};
        //FormsProviderAPI.FormsColumns.GEOMETRY_XPATH};  // smap


        String selectClause = "(lower(" + FormsProviderAPI.FormsColumns.SOURCE + ")='" + Utilities.getSource() + "' or " +
                FormsProviderAPI.FormsColumns.SOURCE + " is null)" +
                " and " + FormsProviderAPI.FormsColumns.TASKS_ONLY + " = 'no'";

        String[] selectArgs = null;
        if(filter.toString().trim().length() > 0 ) {
            selectClause += " and " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " LIKE ?";
            selectArgs = new String[] {"%" + filter + "%"};
        }

        final ContentResolver resolver = Collect.getInstance().getContentResolver();
        Cursor formListCursor = resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, proj, selectClause, selectArgs, getFormSortOrderExpr(formSortOrder));


        if(formListCursor != null) {

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
                String ro = formListCursor.getString(formListCursor.getColumnIndex(FormsProviderAPI.FormsColumns.READ_ONLY));
                if(ro != null && ro.equals("yes")) {
                    entry.readOnly = true;
                }

                entries.add(entry);
                formListCursor.moveToNext();
            }
        }
        if(formListCursor != null) {
            formListCursor.close();
        }
    }

    /*
     * Change sort order
     */
    public void updateFormSortOrder(String sortOrder) {
        this.formSortOrder = sortOrder;
    }
    public void updateTaskSortOrder(String sortOrder) {
        this.taskSortOrder = sortOrder;
    }

    /*
     * Change filter
     */
    public void updateFilter(CharSequence filter) {
        this.filter = filter;
    }

    private String getFormSortOrderExpr(String sortOrder) {

        String sortOrderExpr = "";

        if(sortOrder.equals("BY_NAME_ASC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
        } else if(sortOrder.equals("BY_NAME_DESC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        } else if(sortOrder.equals("BY_DATE_ASC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " ASC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " ASC";
        } else if(sortOrder.equals("BY_DATE_DESC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.DATE + " DESC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        } else if(sortOrder.equals("BY_PROJECT_ASC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.PROJECT + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        } else if(sortOrder.equals("BY_PROJECT_DESC")) {
            sortOrderExpr = FormsProviderAPI.FormsColumns.PROJECT + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        }
        return sortOrderExpr;
    }
}
