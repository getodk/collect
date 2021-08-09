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
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.analytics.AnalyticsUtils;
import org.odk.collect.android.dao.CursorLoaderFactory;
import org.odk.collect.android.database.instances.DatabaseInstancesRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.projects.ProjectsRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromCurrentCursorPosition;
import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromValues;
import static org.odk.collect.android.database.DatabaseObjectMapper.getValuesFromInstance;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns._ID;
import static org.odk.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.external.InstancesContract.CONTENT_TYPE;
import static org.odk.collect.android.external.InstancesContract.getUri;

public class InstanceProvider extends ContentProvider {

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);

        // We only want to log external calls to the content provider
        if (uri.getQueryParameter(CursorLoaderFactory.INTERNAL_QUERY_PARAM) == null) {
            logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_QUERY);
        }

        Cursor c;
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                c = dbQuery(projectId, projection, selection, selectionArgs, sortOrder);
                break;

            case INSTANCE_ID:
                String id = String.valueOf(ContentUriHelper.getIdFromUri(uri));
                c = dbQuery(projectId, projection, _ID + "=?", new String[]{id}, null);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private Cursor dbQuery(String projectId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ((DatabaseInstancesRepository) instancesRepositoryProvider.get(projectId)).rawQuery(projection, selection, selectionArgs, sortOrder, null);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                return CONTENT_TYPE;

            case INSTANCE_ID:
                return CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_INSERT);

        // Validate the requested uri
        if (URI_MATCHER.match(uri) != INSTANCES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Instance newInstance = instancesRepositoryProvider.get(projectId).save(getInstanceFromValues(initialValues));
        return getUri(projectId, newInstance.getDbId());
    }

    public static String getDisplaySubtext(Context context, String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        context.getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return "";
        }
    }

    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_DELETE);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(_ID));
                        new InstanceDeleter(instancesRepositoryProvider.get(projectId), formsRepositoryProvider.get(projectId)).delete(id);
                    }

                    count = cursor.getCount();
                }

                break;

            case INSTANCE_ID:
                long id = ContentUriHelper.getIdFromUri(uri);

                if (where == null) {
                    new InstanceDeleter(instancesRepositoryProvider.get(projectId), formsRepositoryProvider.get(projectId)).delete(id);
                } else {
                    try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndex(_ID)) == id) {
                                new InstanceDeleter(instancesRepositoryProvider.get(), formsRepositoryProvider.get()).delete(id);
                                break;
                            }
                        }
                    }
                }

                count = 1;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_UPDATE);

        InstancesRepository instancesRepository = instancesRepositoryProvider.get(projectId);
        String instancesPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                try (Cursor cursor = dbQuery(projectId, null, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        Instance instance = getInstanceFromCurrentCursorPosition(cursor, instancesPath);
                        ContentValues existingValues = getValuesFromInstance(instance, instancesPath);

                        existingValues.putAll(values);
                        instancesRepository.save(getInstanceFromValues(existingValues));
                    }

                    count = cursor.getCount();
                }

                break;

            case INSTANCE_ID:
                long instanceId = ContentUriHelper.getIdFromUri(uri);
                if (whereArgs == null || whereArgs.length == 0) {
                    Instance instance = instancesRepository.get(instanceId);
                    ContentValues existingValues = getValuesFromInstance(instance, instancesPath);

                    existingValues.putAll(values);
                    instancesRepository.save(getInstanceFromValues(existingValues));
                    count = 1;
                } else {
                    try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndex(_ID)) == instanceId) {
                                Instance instance = getInstanceFromCurrentCursorPosition(cursor, instancesPath);
                                ContentValues existingValues = getValuesFromInstance(instance, instancesPath);

                                existingValues.putAll(values);
                                instancesRepository.save(getInstanceFromValues(existingValues));
                                break;
                            }
                        }
                    }

                    count = 1;
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private String getProjectId(@NonNull Uri uri) {
        String queryParam = uri.getQueryParameter("projectId");

        if (queryParam != null) {
            return queryParam;
        } else {
            return projectsRepository.getAll().get(0).getUuid();
        }
    }

    private void logServerEvent(String projectId, String event) {
        AnalyticsUtils.logServerEvent(event, settingsProvider.getGeneralSettings(projectId));
    }

    static {
        URI_MATCHER.addURI(InstancesContract.AUTHORITY, "instances", INSTANCES);
        URI_MATCHER.addURI(InstancesContract.AUTHORITY, "instances/#", INSTANCE_ID);
    }
}
