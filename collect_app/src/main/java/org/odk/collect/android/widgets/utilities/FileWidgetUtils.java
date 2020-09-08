package org.odk.collect.android.widgets.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.MediaWidgetDataRequester;

import java.io.File;

import timber.log.Timber;

public class FileWidgetUtils implements MediaWidgetDataRequester {

    @Override
    public String getUpdatedWidgetAnswer(Context context, QuestionMediaManager questionMediaManager, Object object,
                                         String questionIndex, String binaryName, Uri uri, boolean isImageType) {
        File newFile;
        if (isImageType) {
            newFile = (File) object;
        } else {
            newFile = FileWidgetUtils.getFile(context, object);
            if (newFile == null) {
                return binaryName;
            }
        }
        if (newFile.exists()) {
            questionMediaManager.replaceRecentFileForQuestion(questionIndex, newFile.getAbsolutePath());

            Uri newFileUri = context.getContentResolver().insert(uri, getContentValues(newFile, isImageType));
            if (newFileUri != null) {
                Timber.i("Inserting media returned uri = %s", newFileUri.toString());
            }

            if (binaryName != null && !binaryName.equals(newFile.getName())) {
                questionMediaManager.markOriginalFileOrDelete(questionIndex,
                        getInstanceFolder() + File.separator + binaryName);
            }

            binaryName = newFile.getName();
        } else {
            Timber.e("No media file found at : %s", newFile.getAbsolutePath());
        }
        return binaryName;
    }

    public static File getFile(Context context, Object object) {
        File file = null;
        if (object instanceof Uri) {
            String sourcePath = getSourcePathFromUri(context, (Uri) object, MediaStore.MediaColumns.DATA);
            String destinationPath = getDestinationPathFromSourcePath(sourcePath);
            File source = FileUtil.getFileAtPath(sourcePath);
            file = FileUtil.getFileAtPath(destinationPath);
            FileUtil.copyFile(source, file);

        } else if (object instanceof File) {
            file = (File) object;
        } else {
            Timber.w("Widget's setBinaryData must receive a File or Uri object.");
        }
        return file;
    }

    public static String getInstanceFolder() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return null;
        }

        return formController.getInstanceFile().getParent();
    }

    public static String getSourcePathFromUri(Context context, @NonNull Uri uri, String dataType) {
        return MediaUtil.getPathFromUri(context, uri, dataType);
    }

    public static String getDestinationPathFromSourcePath(@NonNull String sourcePath) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return getInstanceFolder() + File.separator + FileUtil.getRandomFilename() + extension;
    }

    private static ContentValues getContentValues(File file, boolean isImageType) {
        // Add the copy to the content provider
        ContentValues values = new ContentValues(6);
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        if (isImageType) {
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        }
        return values;
    }
}
