package org.odk.collect.android.smap.formmanagement;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.kxml2.io.KXmlParser;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dao.SmapReferencesDao;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.smap.local.LocalSQLiteOpenHelperSmap;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.taskModel.LinkedInstance;
import org.odk.collect.android.taskModel.LinkedSurvey;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import timber.log.Timber;

import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public class LocalDataManagerSmap {

    FormLoaderTask formLoaderTask;

    public LocalDataManagerSmap(FormLoaderTask formLoaderTask) {
        this.formLoaderTask = formLoaderTask;
    }

    private class FormData {
        String name;
        ContentValues values = new ContentValues();
        ArrayList<FormData> subForms = new ArrayList<> ();
    }

    public void loadLocalData(String surveyIdent, File formMediaDir) {

        StoragePathProvider storagePathProvider = new StoragePathProvider();
        SmapReferencesDao refDao = new SmapReferencesDao();
        Map<String, String> columnNamesCache = new HashMap<>();

        try {
            // 1. Get the hashmap of surveys referenced by the loading survey
            HashMap<String, LinkedSurvey> surveys = refDao.getLinkedSurveys(surveyIdent);

            // 2. Get the links to surveys whose data is referenced - from the references table
            if(surveys != null && surveys.size() > 0) {
                HashMap<String, ArrayList<ContentValues>> dataSets = new HashMap<> ();
                ArrayList<LinkedInstance> instances = getLinkedInstances(surveys);

                // 2.5 Delete existing local data
                for (String ref : surveys.keySet()) {
                    LinkedSurvey ls = surveys.get(ref);
                    File dbFile = new File(formMediaDir.getAbsolutePath(), ls.tableName + ".db");
                    if (!dbFile.exists()) {
                        FirebaseCrashlytics.getInstance().log("LocalCSV: csv table does not exist: " + dbFile.getAbsolutePath());
                    }
                    LocalSQLiteOpenHelperSmap localSQLiteOpenHelper = new LocalSQLiteOpenHelperSmap(dbFile);
                    localSQLiteOpenHelper.deleteLocal(formLoaderTask);
                }

                // 3. Process each instance
                if(instances.size() > 0) {
                    for (LinkedInstance li : instances) {

                        // 3. Convert contents of instance into a record
                        ArrayList<ContentValues> data = dataSets.get(li.survey.tableName);
                        if (data == null) {
                            data = new ArrayList<>();
                            dataSets.put(li.survey.tableName, data);
                        }

                        // Accumulate data in a FormData structure
                        FormData fd = new FormData();
                        FormData currentForm = fd;
                        currentForm.name = "main";
                        Stack<FormData> formDataStack = new Stack<>();

                        String absPath = getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES), li.instanceFilePath);
                        XmlPullParser parser = new KXmlParser();
                        parser.setInput(new InputStreamReader(new FileInputStream(absPath), StandardCharsets.UTF_8));

                        String tag;
                        parser.nextTag();
                        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                            tag = parser.getName();
                            String value = parser.getText();

                            Timber.i("@@@@@@@@: " + tag + " : " + parser.getEventType() + " : " + value);
                            switch (parser.getEventType()) {
                                case XmlPullParser.START_TAG:
                                    parser.next();
                                    value = parser.getText();
                                    Timber.i("%%%%%%%%: " + tag + " : " + parser.getEventType() + " : " + value);

                                    if(parser.getEventType() == XmlPullParser.TEXT) {
                                        Timber.i("#####################: " + tag + " : " + value);
                                        if (li.survey.columns.contains(tag)) {
                                            String safeColumnName = ExternalDataUtil.toSafeColumnName(tag, columnNamesCache);
                                            currentForm.values.put(safeColumnName, value);
                                        }
                                    } else if(parser.getEventType() == XmlPullParser.START_TAG) {
                                        Timber.i("#####################: Sub Form: %s", tag);
                                        if (!tag.equals("main")) {   // Top level form main already has a form definition which is an entry point to the graph
                                            FormData subFormData = new FormData();
                                            formDataStack.push(currentForm);
                                            currentForm = subFormData;
                                            currentForm.name = tag;
                                        }
                                    }
                                    break;

                                case XmlPullParser.END_TAG:
                                    if(tag.equals(currentForm.name) && !formDataStack.empty()) {
                                        FormData completedForm = currentForm;
                                        currentForm = formDataStack.pop();
                                        if(completedForm.values.size() > 0 || completedForm.subForms.size() > 0) {  // Add if not empty
                                            currentForm.subForms.add(completedForm);
                                        }
                                        Timber.i("#####################: End Sub Form: %s", tag);
                                    }
                                    parser.next();
                                    break;
                                default:
                                    parser.next();
                                    break;
                            }

                        }

                        // Convert FormData structure into records
                        addNode(data, fd, new ContentValues());

                    }

                    // 4. Write instance records to the database table
                    for (String tableName: dataSets.keySet()) {

                        File dbFile = new File(formMediaDir.getAbsolutePath(), tableName + ".db");
                        if (!dbFile.exists()) {
                            FirebaseCrashlytics.getInstance().log("LocalCSV: csv table does not exist: " + dbFile.getAbsolutePath());
                        }
                        LocalSQLiteOpenHelperSmap localSQLiteOpenHelper = new LocalSQLiteOpenHelperSmap(dbFile);
                        localSQLiteOpenHelper.append(dataSets.get(tableName), formLoaderTask);
                    }
                }

            }
        } catch (Exception e) {
            Timber.e(e);
            FirebaseCrashlytics.getInstance().recordException(e);
        }


    }

    /*
     * Get the instances of the linked surveys
     */
    private ArrayList<LinkedInstance> getLinkedInstances(HashMap<String, LinkedSurvey> surveys) {
        ArrayList<LinkedInstance> instances = new ArrayList<>();

        InstancesDao instancesDao = new InstancesDao();
        try (Cursor cursor = instancesDao.getFinalizedDateOrderInstancesCursor()) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    String surveyName =  cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    LinkedInstance li = new LinkedInstance();
                    li.survey = surveys.get(surveyName);
                    if(li.survey != null) {
                        // Need to process this survey
                        li.instanceFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                        instances.add(li);
                        Timber.i("xxxxxxxxxxxxxxxxxxx: %s", li.instanceFilePath);
                    } else {
                        Timber.i("xxxxxxxxxxxxxxxxxx: Survey " + surveyName + " is not referenced");
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return instances;
    }

    /*
     * Recursively convert nodes into records
     */
    private void addNode(ArrayList<ContentValues> data, FormData fd, ContentValues values) {
        ContentValues nodeValues = new ContentValues();
        nodeValues.putAll(values);      // Add what we have been passed
        nodeValues.putAll(fd.values);   // Add the values in this node

        // Process the subforms
        if(fd.subForms.size() == 0) {
            data.add(nodeValues);   // Reached a leaf node we are done
        } else {
            for(FormData sf : fd.subForms) {
                addNode(data, sf, nodeValues);
            }
        }
    }
}
