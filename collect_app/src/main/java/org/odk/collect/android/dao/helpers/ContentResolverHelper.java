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
import org.odk.collect.android.database.DatabaseInstancesRepository;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.logic.FormInfo;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

public final class ContentResolverHelper {

    private ContentResolverHelper() {

    }

    public static FormInfo getFormDetails(Uri uri) {
        Instance instance = new DatabaseInstancesRepository().get(Long.parseLong(uri.getPathSegments().get(1)));
        if (instance != null) {
            String instanceFilePath = instance.getInstanceFilePath();
            return new FormInfo(instanceFilePath, instance.getJrFormId(), instance.getJrVersion());
        } else {
            return null;
        }
    }

    public static String getFileExtensionFromUri(Uri fileUri) {
        String mimeType = Collect.getInstance().getContentResolver().getType(fileUri);

        String extension = fileUri.getScheme() != null && fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                : MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

        if (extension == null || extension.isEmpty()) {
            try (Cursor cursor = Collect.getInstance().getContentResolver().query(fileUri, null, null, null, null)) {
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
