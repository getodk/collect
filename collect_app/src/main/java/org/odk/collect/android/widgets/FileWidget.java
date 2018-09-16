package org.odk.collect.android.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.io.File;

import timber.log.Timber;

/**
 * @author James Knight
 */
public abstract class FileWidget extends QuestionWidget implements BinaryWidget {

    public FileWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    public abstract void deleteFile();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ApplicationConstants.RequestCodes.AUDIO_CAPTURE:
            case ApplicationConstants.RequestCodes.VIDEO_CAPTURE:
                Uri mediaUri = data.getData();
                setBinaryData(mediaUri);

                String filePath = MediaUtils.getDataColumn(getContext(), mediaUri, null, null);
                if (filePath != null) {
                    new File(filePath).delete();
                }
                try {
                    getContext().getContentResolver().delete(mediaUri, null, null);
                } catch (Exception e) {
                    Timber.e(e);
                }
                break;

            case ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER:
            case ApplicationConstants.RequestCodes.AUDIO_CHOOSER:
            case ApplicationConstants.RequestCodes.VIDEO_CHOOSER:
            case ApplicationConstants.RequestCodes.IMAGE_CHOOSER:
                getWidgetAnswerListener().saveChosenFile(this, data.getData());
                break;
            case ApplicationConstants.RequestCodes.DRAW_IMAGE:
            case ApplicationConstants.RequestCodes.ANNOTATE_IMAGE:
            case ApplicationConstants.RequestCodes.SIGNATURE_CAPTURE:
            case ApplicationConstants.RequestCodes.IMAGE_CAPTURE:
                saveCapturedImage();
                break;
        }
    }


    /*
     * We saved the image to the tempfile_path, but we really want it to
     * be in: /sdcard/odk/instances/[current instnace]/something.jpg so
     * we move it there before inserting it into the content provider.
     * Once the android image capture bug gets fixed, (read, we move on
     * from Android 1.6) we want to handle images the audio and video
     */
    protected void saveCapturedImage() {
        // The intent is empty, but we know we saved the image to the temp file
        ImageConverter.execute(Collect.TMPFILE_PATH, this, getContext());
        File fi = new File(Collect.TMPFILE_PATH);

        // Revoke permissions granted to this file due its possible usage in the camera app
        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", fi);
        FileUtils.revokeFileReadWritePermission(getContext(), uri);
        saveImageToInstanceFolder(fi);
    }

    protected void saveImageToInstanceFolder(File imageFile) {
        String instanceFolder = getFormController().getInstanceFile().getParent();
        String s = instanceFolder + File.separator + System.currentTimeMillis() + ".jpg";

        File nf = new File(s);
        if (!imageFile.renameTo(nf)) {
            Timber.e("Failed to rename %s", imageFile.getAbsolutePath());
        } else {
            Timber.i("Renamed %s to %s", imageFile.getAbsolutePath(), nf.getAbsolutePath());
        }

        setBinaryData(nf);
    }
}
