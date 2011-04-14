/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.database.StorageDatabase;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;
import org.odk.collect.android.utilities.FilterUtils.FilterCriteria;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * ContentProvider for form definitions. This supports 3 URIs. CONTENT_URI_INFO_DATASET
 * (content://org.opendatakit.storage.forms/info and .../info/#) Access to the metadata about a
 * form. This includes all the KEY_* values defined here, with the exception of the
 * KEY_JRCACHE_FILE_PATH, which is explicitly prevented from leaving the confines of the
 * ContentProvider. KEY_FORM_FILE_PATH should be similarly restricted, but there is still some
 * restructuring to be done for it. Supports: query, delete. Partial support: insert -- must specify
 * KEY_FORM_FILE_PATH. update -- no parameters -- forces re-scan of /sdcard/odk/forms for new forms
 * CONTENT_URI_FORM_FILE_DATASET (content://org.opendatakit.storage.forms/form and .../form/#)
 * Access the form file itself. Supports: openFile("r") Partial support: insert -- must specify the
 * KEY_FORM_FILE_PATH. update -- no parameters -- forces re-scan of /sdcard/odk/forms for new forms
 * CONTENT_URI_JRCACHE_FILE_DATASET (content://org.opendatakit.storage.forms/jrcache) Access the
 * saved FormDef of the corresponding form. Supports: openFile("r"), openFile("w") Notes: insert -
 * the form is considered 'managed' by ODK Collect and can be deleted through the content provider.
 * Note that any forms opened are automatically inserted into the content provider.
 * 
 * @author mitchellsundt@gmail.com
 */
public class FormsStorage extends ContentProvider {

    private static final String t = "FormsStorage";

    /** URI of the Forms content provider */
    public static final Uri CONTENT_URI;
    public static final Uri CONTENT_URI_INFO_DATASET;
    public static final Uri CONTENT_URI_FORM_FILE_DATASET;
    public static final Uri CONTENT_URI_JRCACHE_FILE_DATASET;

    public static final String INFO_DATASET = "info";
    public static final String FORM_FILE_DATASET = "form";
    public static final String JRCACHE_FILE_DATASET = "jrcache";

    // status for forms
    public static final String STATUS_PARTIAL = "partial"; // partially downloaded (working...)
    public static final String STATUS_AVAILABLE = "available";

    // these values are read-only through content provider...
    public static final String KEY_ID = "_id"; // required for Android
    public static final String KEY_DISPLAY_NAME = "displayName"; // (form name)
    public static final String KEY_DISPLAY_SUBTEXT = "displaySubtext";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_FORM_ID = "formId";
    public static final String KEY_MODEL_VERSION = "modelVersion";
    public static final String KEY_UI_VERSION = "uiVersion";
    public static final String KEY_MD5_HASH = "md5Hash";
    public static final String KEY_DISCOVERY_DATE = "date";
    public static final String KEY_FORM_MEDIA_PATH = "formMediaPath";

    public static final String KEY_FORM_FILE_PATH = "formFilePath"; // hidden
    public static final String KEY_JRCACHE_FILE_PATH = "jrcacheFilePath"; // hidden

    private static final int INFO_ALLROWS = 1;
    private static final int INFO_SINGLE_ROW = 2;
    private static final int FORM_FILE_ALLROWS = 4;
    private static final int FORM_FILE_SINGLE_ROW = 5;
    private static final int JRCACHE_FILE_SINGLE_ROW = 6;

    private static final UriMatcher uriMatcher;

    static {
        // Collect.getInstance() is null at this point!!!
        String formsAuthority = "org.opendatakit.storage.forms";
        CONTENT_URI = Uri.parse("content://" + formsAuthority);
        CONTENT_URI_INFO_DATASET = Uri.withAppendedPath(CONTENT_URI, INFO_DATASET);
        CONTENT_URI_FORM_FILE_DATASET = Uri.withAppendedPath(CONTENT_URI, FORM_FILE_DATASET);
        CONTENT_URI_JRCACHE_FILE_DATASET = Uri.withAppendedPath(CONTENT_URI, JRCACHE_FILE_DATASET);

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(formsAuthority, INFO_DATASET, INFO_ALLROWS);
        uriMatcher.addURI(formsAuthority, INFO_DATASET + "/#", INFO_SINGLE_ROW);
        uriMatcher.addURI(formsAuthority, FORM_FILE_DATASET, FORM_FILE_ALLROWS);
        uriMatcher.addURI(formsAuthority, FORM_FILE_DATASET + "/#", FORM_FILE_SINGLE_ROW);
        uriMatcher.addURI(formsAuthority, JRCACHE_FILE_DATASET + "/#", JRCACHE_FILE_SINGLE_ROW);
    }


    private static final int matchOnly(Uri uri, int... values) {
        int value = uriMatcher.match(uri);
        for (int i = 0; i < values.length; ++i) {
            if (value == values[i])
                return value;
        }
        throw new IllegalArgumentException("Invalid URI for this operation: " + uri.toString());
    }


    private static final void isExposableProjection(String[] projection) {
        for (String s : projection) {
            if (KEY_FORM_FILE_PATH.equalsIgnoreCase(s)) {
                Log.w(t, "Exposing KEY_FORM_FILE_PATH -- consider restructuring to hide this!");
                // throw new IllegalArgumentException("Unrecognized element");
            }
            if (KEY_JRCACHE_FILE_PATH.equalsIgnoreCase(s)) {
                throw new IllegalArgumentException("Unrecognized element");
            }
        }
    }

    private static final String FORMS_TABLE = "forms";


    private static final String getDisplaySubtext(Date date) {
        String ts = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(date);
        return "added on " + ts;

    }

    /**
     * Database helper. Adding or altering the database structure should result in a new class than
     * handles transforming from version n-1 to version n. When onUpgrade is called, it can then
     * invoke the onUpgrade of the earlier versions until it has the version at n-1, then it does
     * its own processing.
     * 
     * @author mitchellsundt@gmail.com
     */
    private static class DatabaseHelper1 extends ODKSQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;

        private static final String FORMS_TABLE_CREATE =
            "create table " + FormsStorage.FORMS_TABLE + " (" + FormsStorage.KEY_ID
                    + " integer primary key, " + FormsStorage.KEY_DISPLAY_NAME + " text not null, "
                    + FormsStorage.KEY_DISPLAY_SUBTEXT + " text not null, "
                    + FormsStorage.KEY_DESCRIPTION + " text not null, " + FormsStorage.KEY_FORM_ID
                    + " text not null, " + FormsStorage.KEY_MODEL_VERSION + " integer null, "
                    + FormsStorage.KEY_UI_VERSION + " integer null, " + FormsStorage.KEY_MD5_HASH
                    + " text not null, " + FormsStorage.KEY_DISCOVERY_DATE
                    + " integer not null, " // milliseconds
                    + FormsStorage.KEY_FORM_MEDIA_PATH + " text not null, "

                    + FormsStorage.KEY_FORM_FILE_PATH + " text not null, "
                    + FormsStorage.KEY_JRCACHE_FILE_PATH + " text not null );";


        DatabaseHelper1(String databaseName) {
            super(FileUtils.getDatabasePath(), databaseName, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FORMS_TABLE_CREATE);
        }


        @Override
        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + FormsStorage.FORMS_TABLE);
            onCreate(db);
        }

    }


    public static final ODKSQLiteOpenHelper getOpenHelper(String databaseName) {
        return new DatabaseHelper1(databaseName);
    }

    private StorageDatabase guardedStorageDb = null;


    private synchronized StorageDatabase getStorageDb() {
        if (guardedStorageDb != null)
            return guardedStorageDb;

        Collect app = Collect.getInstance();
        if (app == null)
            throw new IllegalStateException("Collect application not yet initialized");

        guardedStorageDb = app.getStorageDb(Collect.StorageType.FORMS);
        sync();

        return guardedStorageDb;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    private static class FormDetails {
        String formId;
        Integer modelVersion;
        Integer uiVersion;
        String formName;
    }


    private FormDetails retrieveDetails(File formPath) {
        final FormDetails f = new FormDetails();

        InputStream is;
        try {
            is = new FileInputStream(formPath);
        } catch (FileNotFoundException e1) {
            throw new IllegalStateException(e1);
        }

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.w(t, "UTF 8 encoding unavailable, trying default encoding");
            isr = new InputStreamReader(is);
        }

        if (isr != null) {

            Document doc;
            try {
                doc = XFormParser.getXMLDocument(isr);
            } finally {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.w(t, "Error closing form reader");
                    e.printStackTrace();
                }
            }

            String xforms = "http://www.w3.org/2002/xforms";
            String html = doc.getRootElement().getNamespace();

            Element cur = doc.getRootElement().getElement(html, "head");
            Element title = cur.getElement(html, "title");
            if (title != null) {
                f.formName = XFormParser.getXMLText(title, true);
            } else {
                String name = formPath.getName();
                // strip off file extension
                name = name.substring(0, name.lastIndexOf("."));
                f.formName = name;
            }
            cur = cur.getElement(xforms, "model");
            cur = cur.getElement(xforms, "instance");
            int idx = cur.getChildCount();
            int i;
            for (i = 0; i < idx; ++i) {
                if (cur.isText(i))
                    continue;
                if (cur.getType(i) == Node.ELEMENT) {
                    break;
                }
            }

            if (i < idx) {
                cur = cur.getElement(i); // this is the first data element
                String id = cur.getAttributeValue(null, "id");
                String xmlns = cur.getNamespace();
                String modelVersion = cur.getAttributeValue(null, "version");
                String uiVersion = cur.getAttributeValue(null, "uiVersion");

                f.formId = (id == null) ? xmlns : id;
                f.modelVersion = (modelVersion == null) ? null : Integer.valueOf(modelVersion);
                f.uiVersion = (uiVersion == null) ? null : Integer.valueOf(uiVersion);
            } else {
                throw new IllegalStateException("Form could not be parsed");
            }
        }

        return f;
    }

    private class SyncWithFilesystem extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... unused) {
            boolean repeat = true;
            while (repeat) {
                int changeCount = 0;
                // get the available cached forms
                ArrayList<String> cachedForms = new ArrayList<String>();
                if (FileUtils.createFolder(FileUtils.CACHE_PATH)) {
                    cachedForms = FileUtils.getValidFormsAsArrayList(FileUtils.CACHE_PATH);
                }

                // get the available xml forms
                ArrayList<String> xmlForms = new ArrayList<String>();
                if (FileUtils.createFolder(FileUtils.FORMS_PATH)) {
                    xmlForms = FileUtils.getValidFormsAsArrayList(FileUtils.FORMS_PATH);
                }

                List<Integer> toDelete = new ArrayList<Integer>();

                String[] params = new String[] {
                        KEY_ID, KEY_FORM_FILE_PATH, KEY_JRCACHE_FILE_PATH, KEY_MD5_HASH
                };

                Cursor c = null;
                try {
                    c = getStorageDb().query(FORMS_TABLE, params, null, null, null);
                    while (c.moveToNext()) {
                        String path = c.getString(c.getColumnIndex(KEY_FORM_FILE_PATH));
                        String jrcachePath = c.getString(c.getColumnIndex(KEY_JRCACHE_FILE_PATH));

                        if (xmlForms.remove(path)) {
                            if (jrcachePath != null) {
                                File formBin = new File(jrcachePath);
                                cachedForms.remove(formBin.getAbsolutePath());
                            }
                            continue; // found in db and on disk
                        }

                        // otherwise, xml file for form is no longer present...

                        // delete jrcache version of form...
                        if (jrcachePath != null) {
                            File formBin = new File(jrcachePath);
                            if (cachedForms.remove(formBin.getAbsolutePath())) {
                                try {
                                    formBin.delete();
                                } catch (Exception e) {
                                    Log.w(t, "Unable to delete orphan cached form: "
                                            + formBin.getAbsolutePath() + " reason: "
                                            + e.getMessage());
                                }
                            }
                        }

                        toDelete.add(new Integer(c.getInt(c.getColumnIndex(KEY_ID))));
                    }
                } finally {
                    try {
                        if (c != null) {
                            c.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c = null;
                }

                // delete the dead forms...
                for (Integer idx : toDelete) {
                    FilterUtils.FilterCriteria fc = FilterUtils.buildSelectionClause(KEY_ID, idx);
                    getStorageDb().delete(FORMS_TABLE, fc.selection, fc.selectionArgs);
                    changeCount++;
                }

                // and add the newly found forms...
                for (String xmlFormPath : xmlForms) {
                    File formXml = new File(xmlFormPath);
                    ContentValues v = new ContentValues();
                    v.put(KEY_FORM_FILE_PATH, formXml.getAbsolutePath());
                    FormsStorage.this.insert(CONTENT_URI_FORM_FILE_DATASET, v);
                    changeCount++;
                }

                // now loop through all the forms (some of which are newly added above)
                // and remove their jrcache files from the cachedForms list.
                try {
                    c = getStorageDb().query(FORMS_TABLE, params, null, null, null);
                    while (c.moveToNext()) {
                        String path = c.getString(c.getColumnIndex(KEY_JRCACHE_FILE_PATH));
                        File jrcacheFile = new File(path);
                        cachedForms.remove(jrcacheFile.getAbsolutePath());
                    }
                } finally {
                    try {
                        if (c != null) {
                            c.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c = null;
                }

                // and now remove any cached forms that don't have the xml files any more...
                for (String cached : cachedForms) {
                    File formDef = new File(cached);
                    formDef.delete();
                    changeCount++;
                }

                Log.i(t, "Number of changes to the known forms list: "
                        + Integer.toString(changeCount));
                if (changeCount != 0) {
                    getContext().getContentResolver().notifyChange(FormsStorage.CONTENT_URI, null);
                }
                repeat = FormsStorage.this.removeGuardedSync();
            }
            return null;
        }
    }

    private int requestCount = 0;


    private synchronized boolean removeGuardedSync() {
        --requestCount;
        return (requestCount == 0);
    }


    private synchronized void sync() {
        ++requestCount;
        if (requestCount == 1) {
            // no requests were outstanding.
            new SyncWithFilesystem().execute((Void[]) null);
        }
    }

    private static class FormFilesetInfo {
        String id;
        File formPath;
        File cachePath;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        FilterCriteria criteria = null;
        boolean deleteOnlyJRCacheFile = false;
        switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW, JRCACHE_FILE_SINGLE_ROW)) {
            case INFO_ALLROWS:
                criteria = FilterUtils.buildAsGiven(selection, selectionArgs);
                break;
            case INFO_SINGLE_ROW:
                criteria = FilterUtils.buildAsGivenWithUri(selection, selectionArgs, KEY_ID, uri);
                break;
            case JRCACHE_FILE_SINGLE_ROW:
                deleteOnlyJRCacheFile = true;
                criteria = FilterUtils.buildAsGivenWithUri(selection, selectionArgs, KEY_ID, uri);
                break;
        }
        String[] projection = new String[] {
                KEY_ID, KEY_FORM_FILE_PATH, KEY_JRCACHE_FILE_PATH
        };

        List<FormFilesetInfo> toDelete = new ArrayList<FormFilesetInfo>();

        Cursor c = null;
        try {
            c =
                getStorageDb().query(FORMS_TABLE, projection, criteria.selection,
                    criteria.selectionArgs, null);
            int idxId = c.getColumnIndex(KEY_ID);
            int idxFormPath = c.getColumnIndex(KEY_FORM_FILE_PATH);
            int idxCachePath = c.getColumnIndex(KEY_JRCACHE_FILE_PATH);

            while (c.moveToNext()) {
                FormFilesetInfo f = new FormFilesetInfo();
                f.id = c.getString(idxId);
                f.formPath = new File(c.getString(idxFormPath));
                f.cachePath = new File(c.getString(idxCachePath));
                toDelete.add(f);
            }
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c = null;
        }

        int deleteCount = 0;
        for (FormFilesetInfo f : toDelete) {
            boolean cacheDeleted = false;
            try {
                if (f.cachePath.exists()) {
                    cacheDeleted = f.cachePath.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(t, "Unable to delete jrcache file: " + f.cachePath.getAbsolutePath());
            }

            if (deleteOnlyJRCacheFile) {
                if (cacheDeleted)
                    ++deleteCount;
            } else {
                FilterUtils.FilterCriteria fc = FilterUtils.buildSelectionClause(KEY_ID, f.id);
                int found = getStorageDb().delete(FORMS_TABLE, fc.selection, fc.selectionArgs);
                try {
                    if (f.formPath.exists()) {
                        f.formPath.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(t, "Unable to delete form file: " + f.formPath.getAbsolutePath());
                }
                if (found != 1) {
                    Log.w(t, "Unexpected found count(" + Integer.toString(found)
                            + ") returned from delete on forms table record: "
                            + f.formPath.getAbsolutePath());
                }
                deleteCount += found;
            }
        }

        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(FormsStorage.CONTENT_URI, null);
        }
        return deleteCount;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        String s = null;
        switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW)) {
            case INFO_ALLROWS:
                s = Collect.getInstance().getString(R.string.mime_type_forms_list);
                break;
            case INFO_SINGLE_ROW:
                s = Collect.getInstance().getString(R.string.mime_type_forms_item);
                break;
        }
        return s;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // any insert into FORM_FILE or INFO dataset is equivalent
        int type = matchOnly(uri, FORM_FILE_ALLROWS, INFO_ALLROWS);

        String xmlFormPath = values.getAsString(KEY_FORM_FILE_PATH);
        if (xmlFormPath == null) {
            throw new IllegalArgumentException("insertions must specify " + KEY_FORM_FILE_PATH);
        }
        File formXml = new File(xmlFormPath);
        // double-check that the form file does not already exist...
        Cursor c = null;
        try {
            FilterUtils.FilterCriteria fd =
                FilterUtils.buildSelectionClause(KEY_FORM_FILE_PATH, formXml.getAbsolutePath());
            c = getStorageDb().query(FORMS_TABLE, new String[] {
                KEY_ID
            }, fd.selection, fd.selectionArgs, null);
            if (c.moveToNext()) {
                // the file already exists in database -- return the link...
                long keyId = c.getLong(c.getColumnIndex(KEY_ID));
                if (type == INFO_ALLROWS) {
                    return ContentUris.withAppendedId(CONTENT_URI_INFO_DATASET, keyId);
                } else if (type == FORM_FILE_ALLROWS) {
                    return ContentUris.withAppendedId(CONTENT_URI_FORM_FILE_DATASET, keyId);
                } else {
                    throw new IllegalStateException("missing case");
                }
            }
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c = null;
        }

        // doesn't exist --- insert it.
        String formHash = FileUtils.getMd5Hash(formXml);
        FormDetails d = retrieveDetails(formXml);
        Date now = new Date();
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        g.setTime(now);

        // build up actual inserted content
        ContentValues v = new ContentValues();
        v.put(KEY_FORM_FILE_PATH, formXml.getAbsolutePath());
        File formMedia = new File(FileUtils.getFormMediaPath(formXml.getAbsolutePath()));
        v.put(KEY_FORM_MEDIA_PATH, formMedia.getAbsolutePath());
        v.put(KEY_JRCACHE_FILE_PATH, FileUtils.CACHE_PATH + formHash + ".formdef");
        v.put(KEY_MD5_HASH, formHash);
        if (values.containsKey(KEY_DISPLAY_NAME)) {
            v.put(KEY_DISPLAY_NAME, values.getAsString(KEY_DISPLAY_NAME));
        } else {
            v.put(KEY_DISPLAY_NAME, d.formName);
        }
        v.put(KEY_DISPLAY_SUBTEXT, getDisplaySubtext(now));
        if (values.containsKey(KEY_DESCRIPTION)) {
            v.put(KEY_DESCRIPTION, values.getAsString(KEY_DESCRIPTION));
        } else {
            v.put(KEY_DESCRIPTION, d.formName);
        }
        v.put(KEY_FORM_ID, d.formId);
        v.put(KEY_MODEL_VERSION, d.modelVersion);
        v.put(KEY_UI_VERSION, d.uiVersion);
        v.put(KEY_DISCOVERY_DATE, now.getTime());

        // insert
        long keyId = getStorageDb().insert(FORMS_TABLE, v);
        getContext().getContentResolver().notifyChange(FormsStorage.CONTENT_URI, null);

        // and return the appropriate Uri (to file or metadata)
        if (type == INFO_ALLROWS) {
            return ContentUris.withAppendedId(CONTENT_URI_INFO_DATASET, keyId);
        } else if (type == FORM_FILE_ALLROWS) {
            return ContentUris.withAppendedId(CONTENT_URI_FORM_FILE_DATASET, keyId);
        } else {
            throw new IllegalStateException("missing case");
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#openFile(android.net.Uri, java.lang.String)
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

        FilterCriteria criteria = null;
        String[] projection = null;
        int uriType = matchOnly(uri, FORM_FILE_SINGLE_ROW, JRCACHE_FILE_SINGLE_ROW);
        switch (uriType) {
            case FORM_FILE_SINGLE_ROW:
                criteria = FilterUtils.buildAsGivenWithUri(null, null, KEY_ID, uri);
                projection = new String[] {
                        KEY_FORM_FILE_PATH, KEY_DISPLAY_NAME
                };
                break;
            case JRCACHE_FILE_SINGLE_ROW:
                criteria = FilterUtils.buildAsGivenWithUri(null, null, KEY_ID, uri);
                projection = new String[] {
                    KEY_JRCACHE_FILE_PATH
                };
                break;
        }
        int idxFilePath;
        String filepath;

        Cursor c = null;
        try {
            c =
                getStorageDb().query(FORMS_TABLE, projection, criteria.selection,
                    criteria.selectionArgs, null);
            if (!c.moveToFirst()) {
                throw new FileNotFoundException("Unable to locate indicated record: "
                        + uri.toString());
            }

            idxFilePath = c.getColumnIndex(projection[0]);
            filepath = c.isNull(idxFilePath) ? null : c.getString(c.getColumnIndex(projection[0]));

            if (c.moveToNext()) {
                throw new IllegalStateException(
                        "Criteria did not result in identifying a unique file");
            }
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c = null;
        }

        if (filepath == null || filepath.length() == 0) {
            throw new FileNotFoundException("No file path found for uri: " + uri.toString());
        }

        File file = new File(filepath);
        int modeInt = ParcelFileDescriptor.MODE_READ_ONLY;
        if (mode.compareToIgnoreCase("r") == 0) {
            modeInt = ParcelFileDescriptor.MODE_READ_ONLY;
            if (!file.exists()) {
                throw new FileNotFoundException("Unable to locate file for uri: " + uri.toString());
            }
        } else if (mode.compareToIgnoreCase("w") == 0) {
            if (uriType == FORM_FILE_SINGLE_ROW) {
                throw new FileNotFoundException("File cannot be opened for write access: "
                        + uri.toString());
            }
            modeInt =
                ParcelFileDescriptor.MODE_WRITE_ONLY
                        | (file.exists() ? ParcelFileDescriptor.MODE_TRUNCATE
                                : ParcelFileDescriptor.MODE_CREATE);
        } else {
            throw new FileNotFoundException(
                    "Only read ('r') and write ('w') supported for open mode");
        }

        return ParcelFileDescriptor.open(file, modeInt);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[],
     * java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        isExposableProjection(projection);
        FilterCriteria c = null;
        switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW)) {
            case INFO_ALLROWS:
                c = FilterUtils.buildAsGiven(selection, selectionArgs);
                break;
            case INFO_SINGLE_ROW:
                c = FilterUtils.buildAsGivenWithUri(selection, selectionArgs, KEY_ID, uri);
                break;
        }
        Cursor cursor =
            getStorageDb().query(FORMS_TABLE, projection, c.selection, c.selectionArgs, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        matchOnly(uri, INFO_ALLROWS, FORM_FILE_ALLROWS);
        if (selection != null || (selectionArgs != null && selectionArgs.length != 0)) {
            throw new IllegalArgumentException("update does not take any arguments");
        }
        sync();
        return 0;
    }

}
