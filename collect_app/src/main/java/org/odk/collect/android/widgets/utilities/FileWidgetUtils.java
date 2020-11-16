package org.odk.collect.android.widgets.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.MultiClickSafeButton;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

public class FileWidgetUtils {

    private FileWidgetUtils() {
    }

    public static void hideButtonsIfNeeded(FormEntryPrompt prompt, MultiClickSafeButton button) {
        if (prompt.getAppearanceHint() != null
                && prompt.getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(WidgetAppearanceUtils.NEW)) {
            button.setVisibility(View.GONE);
        }
    }

    public static File getFile(Context context, Object object, String instanceFolder) {
        File file = null;
        if (object instanceof Uri) {
            String sourcePath = getSourcePathFromUri(context, (Uri) object, MediaStore.MediaColumns.DATA);
            String destinationPath = FileWidgetUtils.getDestinationPathFromSourcePath(sourcePath, instanceFolder);
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

    public static String getSourcePathFromUri(Context context, @NonNull Uri uri, String dataType) {
        return MediaUtil.getPathFromUri(context, uri, dataType);
    }

    public static String updateWidgetAnswer(Context context, Object object, String questionIndex, String instanceFolder,
                                            String binaryName, Uri uri, boolean isImageType) {
        File newFile = null;
        if (isImageType) {
            newFile = (File) object;
        } else {
            newFile = FileWidgetUtils.getFile(context, object, instanceFolder);
            if (newFile == null) {
                return binaryName;
            }
        }

        if (newFile.exists()) {
            MediaManager.INSTANCE.replaceRecentFileForQuestion(questionIndex, newFile.getAbsolutePath());

            updateContentValues(context, newFile, uri, isImageType);
            binaryName = deleteOriginalAnswer(newFile, binaryName, questionIndex, instanceFolder);
        } else {
            Timber.e("No media file found at : %s", newFile.getAbsolutePath());
        }
        return binaryName;
    }

    public static String deleteOriginalAnswer(File newFile, String originalAnswer, String questionIndex, String instanceFolder) {
        if (originalAnswer != null && !originalAnswer.equals(newFile.getName())) {
            MediaManager.INSTANCE.markOriginalFileOrDelete(questionIndex,
                    instanceFolder + File.separator + originalAnswer);
        }
        return newFile.getName();
    }

    private static void updateContentValues(Context context, File file, Uri uri, boolean isImageType) {
        // Add the copy to the content provider
        ContentValues values = new ContentValues(6);
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        if (isImageType) {
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        }

        Uri newFileUri = context.getContentResolver().insert(uri, values);
        if (newFileUri != null) {
            Timber.i("Inserting media returned uri = %s", newFileUri.toString());
        }
    }

    public static String getDestinationPathFromSourcePath(@NonNull String sourcePath, String instanceFolder) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return instanceFolder + File.separator + FileUtil.getRandomFilename() + extension;
    }
}
