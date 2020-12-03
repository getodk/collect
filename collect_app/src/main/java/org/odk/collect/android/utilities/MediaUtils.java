/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.utilities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.exception.GDriveConnectionException;
import org.odk.collect.android.network.NetworkStateProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

import static org.odk.collect.android.utilities.FileUtils.deleteAndReport;

/**
 * Consolidate all interactions with media providers here.
 * <p>
 * The functionality of getPath() was provided by paulburke as described here:
 * See
 * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android
 * -kitkat-new-storage-access-framework for details
 *
 * @author mitchellsundt@gmail.com
 * @author paulburke
 */
public class MediaUtils {
    private static Uri filePathUri;

    public void deleteMediaFile(String imageFile) {
        deleteAndReport(new File(imageFile));
    }

    public void openFile(Context context, File file) {
        Uri fileUri = Uri.fromFile(file);
        Uri contentUri = ContentUriProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, getMimeType(getPath(context, fileUri)));
        FileUtils.grantFileReadPermissions(intent, contentUri, context);

        if (new ActivityAvailability(context).isActivityAvailable(intent)) {
            context.startActivity(intent);
        } else {
            String message = context.getString(R.string.activity_not_found, context.getString(R.string.open_file));
            ToastUtils.showLongToast(message);
            Timber.w(message);
        }
    }

    public String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;
    }

    public String getDestinationPathFromSourcePath(@NonNull String sourcePath, String instanceFolder) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return instanceFolder + File.separator + new FileUtil().getRandomFilename() + extension;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @see #isLocal(String)
     * @see #getFile(Context, Uri)
     * @author paulburke
     */
    public String getPath(final Context context, final Uri uri) {
        filePathUri = uri;

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type) || !new File("/storage/" + type).exists()) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // To support paths like /storage/4A50-B543/
                return "/storage/" + type + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider

                String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                if (id.startsWith("msf:")) {
                    id = id.replaceFirst("msf:", "");
                }

                String[] contentUriPrefixesToTry = {
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.parseLong(id));
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception e) {
                        Timber.i(e);
                    }
                }

                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                // https://github.com/coltoscosmin/FileUtils/blob/master/FileUtils.java
                String fileName = getFileNameFromUri(context, uri);
                File cacheDir = getDocumentCacheDir(context);
                File file = generateFileName(fileName, cacheDir);
                String destinationPath = null;
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }

                return destinationPath;
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = {split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            return uri.getPath();
        }

        return null;
    }

    public File getFileFromUri(final Context context, final Uri uri, NetworkStateProvider connectivityProvider) throws GDriveConnectionException {
        File file = null;
        String filePath = getPath(context, uri);
        if (filePath != null) {
            file = new File(filePath);
        } else if (isGoogleDriveDocument(uri)) {
            file = getGoogleDriveFile(context, uri, connectivityProvider);
        }

        return file;
    }

    private File getGoogleDriveFile(Context context, Uri uri, NetworkStateProvider connectivityProvider) throws GDriveConnectionException {
        if (!connectivityProvider.isDeviceOnline()) {
            throw new GDriveConnectionException();
        }
        if (uri == null) {
            return null;
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        String filePath = new File(context.getCacheDir(), "tmp").getAbsolutePath();
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor == null) {
                return null;
            }
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fileDescriptor);
            outputStream = new FileOutputStream(filePath);
            int read;
            byte[] bytes = new byte[4096];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            return new File(filePath);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check
     * @return Whether the Uri authority is Google Drive.
     */
    private boolean isGoogleDriveDocument(Uri uri) {
        return uri.getAuthority().startsWith("com.google.android.apps.docs.storage")
                || uri.getAuthority().startsWith("com.google.android.apps.photos.content");
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e) {
            File file = new File(context.getCacheDir(), "tmp");
            FileUtils.saveAnswerFileFromUri(filePathUri, file, context);
            return file.getAbsolutePath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public String getFileNameFromUri(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String fileName = null;

        if (mimeType == null) {
            String path = getPath(context, uri);
            if (path == null) {
                fileName = getName(uri.toString());
            } else {
                File file = new File(path);
                fileName = file.getName();
            }
        } else {
            try (Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = returnCursor.getString(nameIndex);
                    }
                }
            }
        }

        return fileName;
    }

    private String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }

    private File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), "documents");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @Nullable
    private File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            Timber.w(e);
            return null;
        }

        return file;
    }

    private void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            Timber.w(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                Timber.w(e);
            }
        }
    }
}
