package org.odk.collect.android.widgets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ApplicationConstants;
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
                getWidgetAnswerListener().saveAnswersForCurrentScreen(false);

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
                getWidgetAnswerListener().saveChosenFile(data.getData());
                break;
        }
    }
}
