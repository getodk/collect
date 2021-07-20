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

package org.odk.collect.android.external;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.database.forms.DatabaseFormsRepository;
import org.odk.collect.android.formmanagement.FormDeleter;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.utilities.Clock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import static org.odk.collect.android.database.forms.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.database.forms.DatabaseFormColumns.SUBMISSION_URI;

public class FormsProvider extends ContentProvider {

    private static final int FORMS = 1;
    private static final int FORM_ID = 2;
    // Forms unique by ID, keeping only the latest one downloaded
    private static final int NEWEST_FORMS_BY_FORM_ID = 3;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

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

    @Inject
    ProjectsRepository projectsRepository;

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

        String projectId = getProjectId(uri);

        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                cursor = databaseQuery(projectId, projection, selection, selectionArgs, sortOrder, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), FormsContract.getUri(projectId));
                break;

            case NEWEST_FORMS_BY_FORM_ID:
                Set<String> maxDateColumns = new HashSet<>();
                maxDateColumns.add(_ID);
                maxDateColumns.add(DISPLAY_NAME);
                maxDateColumns.add(DESCRIPTION);
                maxDateColumns.add(JR_FORM_ID);
                maxDateColumns.add(JR_VERSION);
                maxDateColumns.add(SUBMISSION_URI);
                maxDateColumns.add(BASE64_RSA_PUBLIC_KEY);
                maxDateColumns.add(MD5_HASH);
                maxDateColumns.add(FORM_MEDIA_PATH);
                maxDateColumns.add(FORM_FILE_PATH);
                maxDateColumns.add(JRCACHE_FILE_PATH);
                maxDateColumns.add(LANGUAGE);
                maxDateColumns.add(AUTO_DELETE);
                maxDateColumns.add(AUTO_SEND);
                maxDateColumns.add(GEOMETRY_XPATH);
                maxDateColumns.add(DELETED_DATE);
                maxDateColumns.add("MAX(date)");

                Map<String, String> maxDateProjectionMap = new HashMap<>();
                for (String column : maxDateColumns) {
                    if (column.equals("MAX(date)")) {
                        maxDateProjectionMap.put("MAX(date)", "MAX(date) AS " + DATE);
                    } else {
                        maxDateProjectionMap.put(column, column);
                    }
                }

                cursor = databaseQuery(projectId, maxDateColumns.toArray(new String[0]), selection, selectionArgs, sortOrder, JR_FORM_ID, maxDateProjectionMap);
                cursor.setNotificationUri(getContext().getContentResolver(), FormsContract.getUri(projectId));
                break;

            case FORM_ID:
                String formId = String.valueOf(ContentUriHelper.getIdFromUri(uri));
                cursor = databaseQuery(projectId, null, _ID + "=?", new String[]{formId}, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;

            // Only include the latest form that was downloaded with each form_id

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case FORMS:
            case NEWEST_FORMS_BY_FORM_ID:
                return FormsContract.CONTENT_TYPE;

            case FORM_ID:
                return FormsContract.CONTENT_ITEM_TYPE;

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

        String projectId = getProjectId(uri);
        String formsPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId);
        String cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId);
        Form form = getFormsRepository(projectId).save(getFormFromValues(initialValues, formsPath, cachePath));
        return FormsContract.getUri(projectId, form.getDbId());
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

        String projectId = getProjectId(uri);
        FormDeleter formDeleter = new FormDeleter(getFormsRepository(projectId), instancesRepositoryProvider.get(projectId));

        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                try (Cursor cursor = databaseQuery(projectId, null, where, whereArgs, null, null, null)) {
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
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        deferDaggerInit();

        String projectId = getProjectId(uri);
        FormsRepository formsRepository = getFormsRepository(projectId);
        String formsPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId);
        String cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case FORMS:
                try (Cursor cursor = databaseQuery(projectId, null, where, whereArgs, null, null, null)) {
                    while (cursor.moveToNext()) {
                        Form form = getFormFromCurrentCursorPosition(cursor, formsPath, cachePath);
                        ContentValues existingValues = getValuesFromForm(form, formsPath);
                        existingValues.putAll(values);

                        formsRepository.save(getFormFromValues(existingValues, formsPath, cachePath));
                    }

                    count = cursor.getCount();
                }
                break;

            case FORM_ID:
                Form form = formsRepository.get(ContentUriHelper.getIdFromUri(uri));
                if (form != null) {
                    ContentValues existingValues = getValuesFromForm(form, formsPath);
                    existingValues.putAll(values);

                    formsRepository.save(getFormFromValues(existingValues, formsPath, cachePath));
                    count = 1;
                } else {
                    count = 0;
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @NotNull
    private FormsRepository getFormsRepository(String projectId) {
        return formsRepositoryProvider.get(projectId);
    }

    private String getProjectId(@NonNull Uri uri) {
        String queryParam = uri.getQueryParameter("projectId");

        if (queryParam != null) {
            return queryParam;
        } else {
            return projectsRepository.getAll().get(0).getUuid();
        }
    }

    private Cursor databaseQuery(String projectId, String[] projection, String selection, String[] selectionArgs, String sortOrder, String groupBy, Map<String, String> projectionMap) {
        return ((DatabaseFormsRepository) getFormsRepository(projectId)).rawQuery(projectionMap, projection, selection, selectionArgs, sortOrder, groupBy);
    }

    static {
        URI_MATCHER.addURI(FormsContract.AUTHORITY, "forms", FORMS);
        URI_MATCHER.addURI(FormsContract.AUTHORITY, "forms/#", FORM_ID);
        // Only available for query and type
        URI_MATCHER.addURI(FormsContract.AUTHORITY, "newest_forms_by_form_id", NEWEST_FORMS_BY_FORM_ID);
    }
}
