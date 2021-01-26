/*
 * Copyright (C) 2018 Shobhit Agarwal
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

package org.odk.collect.android.dao.helpers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public final class ContentResolverHelper {

    private ContentResolverHelper() {

    }

    private static ContentResolver getContentResolver() {
        return Collect.getInstance().getContentResolver();
    }

    public static FormInfo getFormDetails(Uri uri) {
        FormInfo formInfo = null;

        try (Cursor instanceCursor = getContentResolver().query(uri, null, null, null, null)) {
            if (instanceCursor != null && instanceCursor.getCount() > 0) {
                instanceCursor.moveToFirst();
                String instancePath = new StoragePathProvider().getAbsoluteInstanceFilePath(instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH)));

                String jrFormId = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceColumns.JR_FORM_ID));
                int idxJrVersion = instanceCursor
                        .getColumnIndex(InstanceColumns.JR_VERSION);

                String jrVersion = instanceCursor.isNull(idxJrVersion) ? null
                        : instanceCursor
                        .getString(idxJrVersion);
                formInfo = new FormInfo(instancePath, jrFormId, jrVersion);
            }
        }
        return formInfo;
    }

    public static String getFormPath(Uri uri) {
        String formPath = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                formPath = getAbsoluteFilePath(new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS), c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
            }
        }
        return formPath;
    }

    public static String getFileExtensionFromUri(Uri fileUri) {
        String mimeType = getContentResolver().getType(fileUri);

        String extension = fileUri.getScheme() != null && fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                : MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

        if (extension == null || extension.isEmpty()) {
            try (Cursor cursor = getContentResolver().query(fileUri, null, null, null, null)) {
                String name = null;
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                }
                extension = name != null ? name.substring(name.lastIndexOf('.') + 1) : "";
            }
        }

        if (extension.isEmpty() && mimeType != null && mimeType.contains("/")) {
            extension = mimeType.substring(mimeType.lastIndexOf('/') + 1);
        }

        return extension;
    }

    public static String getMimeType(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        String mimeType = extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;

        if (mimeType == null || mimeType.isEmpty()) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            mimeType = fileNameMap.getContentTypeFor(file.getAbsolutePath());
        }

        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = URLConnection.guessContentTypeFromName(file.getName());
        }

        return mimeType;
    }
}
