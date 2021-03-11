package org.odk.collect.android.smap.formmanagement;

import android.database.Cursor;

import org.kxml2.io.KXmlParser;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public class LocalDataManagerSmap {

    private class LinkedSurvey {
        String name;
    };

    private class LinkedInstance {
        LinkedSurvey survey;
        String instanceFilePath;
    };

    public void loadLocalData() {

        StoragePathProvider storagePathProvider = new StoragePathProvider();

        // 1. Get the hashmap of surveys referenced by the loading survey
        HashMap<String, LinkedSurvey> surveys = getLinkedSurveys();

        // 2. Get the links to surveys whose data is referenced - from the references table
        if(surveys.size() > 0) {
            ArrayList<LinkedInstance> instances = getLinkedInstances(surveys);

            // 3. TEMP Read contents of instance
            if(instances.size() > 0) {
                for(LinkedInstance li : instances) {
                    try {
                        String absPath = getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES), li.instanceFilePath);
                        XmlPullParser parser = new KXmlParser();
                        parser.setInput(new InputStreamReader(new FileInputStream(absPath), StandardCharsets.UTF_8));

                        parser.nextToken();
                        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                            switch (parser.getEventType()) {
                                case XmlPullParser.START_TAG:
                                    String tag = parser.getName();

                                    parser.next();
                                    if(parser.getEventType() == XmlPullParser.TEXT) {
                                        String value = parser.getText();
                                        Timber.i("#####################: " +  tag + " : " + value);
                                    }
                                    break;
                                default:
                            }

                            parser.next();
                        }

                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }
        }


    }

    /*
     * Get the surveys that have been references
     */
    private HashMap<String, LinkedSurvey> getLinkedSurveys() {
        HashMap<String, LinkedSurvey> surveys = new HashMap<> ();

        LinkedSurvey ls = new LinkedSurvey();
        ls.name = "s1496_20066";
        surveys.put(ls.name, ls);

        return surveys;
    }

    /*
     * Get the array of linked surveys from the database
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
                        Timber.i("xxxxxxxxxxxxxxxxxxx: " + li.instanceFilePath);
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
}
