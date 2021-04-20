/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.forms.DatabaseFormsRepository;
import org.odk.collect.android.formmanagement.FormDeleter;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.utilities.Clock;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseObjectMapper.getFormFromCurrentCursorPosition;
import static org.odk.collect.android.database.DatabaseObjectMapper.getFormFromValues;
import static org.odk.collect.android.database.DatabaseObjectMapper.getValuesFromForm;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_DELETE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.AUTO_SEND;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DESCRIPTION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.LANGUAGE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MAX_DATE;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.SUBMISSION_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI;
import static org.odk.collect.android.provider.FormsProviderAPI.CONTENT_URI;

public class FormsProvider extends ContentProvider {

    private static HashMap<String, String> sFormsProjectionMap;

    private static final int FORMS = 1;
    private static final int FORM_ID = 2;
    // Forms unique by ID, keeping only the latest one downloaded
    private static final int NEWEST_FORMS_BY_FORM_ID = 3;

    private static final UriMatcher URI_MATCHER;

    @Inject
    Clock clock;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    FastExternalItemsetsRepository fastExternalItemsetsRepository;

    @Inject
    StoragePathProvider storagePathProvider;

    public static void notifyChange() {
        // Make sure content observers (CursorLoaders for instance) are notified of change
        Collect.getInstance().getContentResolver().notifyChange(CONTENT_URI, null);
        Collect.getInstance().getContentResolver().notifyChange(CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);
    }

    // Do not call it in onCreate() https://stackoverflow.com/questions/23521083/inject-database-in-a-contentprovider-with-dagger
    private void deferDaggerInit() {
        DaggerUtils.getComponent(getContext()).inject(this);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        deferDaggerInit();

        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                cursor = databaseQuery(projection, selection, selectionArgs, sortOrder, null);
                break;

            case FORM_ID:
                String formId = String.valueOf(ContentUriHelper.getIdFromUri(uri));
                cursor = databaseQuery(null, _ID + "=?", new String[]{formId}, null, null);
                break;

            // Only include the latest form that was downloaded with each form_id
            case NEWEST_FORMS_BY_FORM_ID:
                Map<String, String> filteredProjectionMap = new HashMap<>(sFormsProjectionMap);
                filteredProjectionMap.put(DATE, MAX_DATE);
                cursor = databaseQuery(filteredProjectionMap.values().toArray(new String[0]), selection, selectionArgs, sortOrder, JR_FORM_ID);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
            case NEWEST_FORMS_BY_FORM_ID:
                return FormsProviderAPI.CONTENT_TYPE;

            case FORM_ID:
                return FormsProviderAPI.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        deferDaggerInit();

        // Validate the requested uri
        if (URI_MATCHER.match(uri) != FORMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Form form = formsRepositoryProvider.get().save(getFormFromValues(initialValues, storagePathProvider));
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(form.getDbId()));
    }

    /**
     * This method removes the entry from the content provider, and also removes
     * any associated files. files: form.xml, [formmd5].formdef, formname-media
     * {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        deferDaggerInit();

        int count;

        FormDeleter formDeleter = new FormDeleter(formsRepositoryProvider.get(), instancesRepositoryProvider.get(), fastExternalItemsetsRepository);

        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                try (Cursor cursor = databaseQuery(null, where, whereArgs, null, null)) {
                    while (cursor.moveToNext()) {
                        formDeleter.delete(cursor.getLong(cursor.getColumnIndex(_ID)));
                    }

                    count = cursor.getCount();
                }
                break;

            case FORM_ID:
                formDeleter.delete(ContentUriHelper.getIdFromUri(uri));
                count = 1;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        deferDaggerInit();
        FormsRepository formsRepository = formsRepositoryProvider.get();

        int count;
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                try (Cursor cursor = databaseQuery(null, where, whereArgs, null, null)) {
                    while (cursor.moveToNext()) {
                        Form form = getFormFromCurrentCursorPosition(cursor, storagePathProvider);

                        ContentValues existingValues = getValuesFromForm(form, storagePathProvider);
                        existingValues.putAll(values);

                        formsRepository.save(getFormFromValues(existingValues, storagePathProvider));
                    }

                    count = cursor.getCount();
                }
                break;

            case FORM_ID:
                Form form = formsRepository.get(ContentUriHelper.getIdFromUri(uri));
                if (form != null) {
                    ContentValues existingValues = getValuesFromForm(form, storagePathProvider);
                    existingValues.putAll(values);

                    formsRepository.save(getFormFromValues(existingValues, storagePathProvider));
                    count = 1;
                } else {
                    count = 0;
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI, null);

        return count;
    }

    private Cursor databaseQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder, String o) {
        return ((DatabaseFormsRepository) formsRepositoryProvider.get()).rawQuery(projection, selection, selectionArgs, sortOrder, o);
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, FormsProviderAPI.CONTENT_URI.getPath(), FORMS);
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, FormsProviderAPI.CONTENT_URI.getPath() + "/#", FORM_ID);
        // Only available for query and type
        URI_MATCHER.addURI(FormsProviderAPI.AUTHORITY, FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI.getPath(), NEWEST_FORMS_BY_FORM_ID);

        sFormsProjectionMap = new HashMap<>();
        sFormsProjectionMap.put(_ID, _ID);
        sFormsProjectionMap.put(DISPLAY_NAME, DISPLAY_NAME);
        sFormsProjectionMap.put(DESCRIPTION, DESCRIPTION);
        sFormsProjectionMap.put(JR_FORM_ID, JR_FORM_ID);
        sFormsProjectionMap.put(JR_VERSION, JR_VERSION);
        sFormsProjectionMap.put(SUBMISSION_URI, SUBMISSION_URI);
        sFormsProjectionMap.put(BASE64_RSA_PUBLIC_KEY, BASE64_RSA_PUBLIC_KEY);
        sFormsProjectionMap.put(MD5_HASH, MD5_HASH);
        sFormsProjectionMap.put(DATE, DATE);
        sFormsProjectionMap.put(FORM_MEDIA_PATH, FORM_MEDIA_PATH);
        sFormsProjectionMap.put(FORM_FILE_PATH, FORM_FILE_PATH);
        sFormsProjectionMap.put(JRCACHE_FILE_PATH, JRCACHE_FILE_PATH);
        sFormsProjectionMap.put(LANGUAGE, LANGUAGE);
        sFormsProjectionMap.put(AUTO_DELETE, AUTO_DELETE);
        sFormsProjectionMap.put(AUTO_SEND, AUTO_SEND);
        sFormsProjectionMap.put(GEOMETRY_XPATH, GEOMETRY_XPATH);
        sFormsProjectionMap.put(DELETED_DATE, DELETED_DATE);
    }
}
