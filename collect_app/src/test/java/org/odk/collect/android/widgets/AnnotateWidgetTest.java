package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Button;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.AlignedImageWidget.DIMENSIONS_EXTRA;
import static org.odk.collect.android.widgets.AlignedImageWidget.FILE_PATH_EXTRA;
import static org.odk.collect.android.widgets.AlignedImageWidget.ODK_CAMERA_INTENT_PACKAGE;
import static org.odk.collect.android.widgets.AlignedImageWidget.ODK_CAMERA_TAKE_PICTURE_INTENT_COMPONENT;
import static org.odk.collect.android.widgets.AlignedImageWidget.RETAKE_OPTION_EXTRA;

/**
 * @author James Knight
 */
public class AnnotateWidgetTest extends FileWidgetTest<AnnotateWidget> {

    @Mock
    File file;

    @NonNull
    @Override
    public AnnotateWidget createWidget() {
        return new AnnotateWidget(activity, formEntryPrompt);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(answerData.getDisplayText());

        return file;
    }

    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;

        switch (clickedButton.getId()) {
            case R.id.capture_image:
                if (permissionGranted) {
                    intent = new Intent();
                    intent.setComponent(new ComponentName(ODK_CAMERA_INTENT_PACKAGE, ODK_CAMERA_TAKE_PICTURE_INTENT_COMPONENT));
                    intent.putExtra(FILE_PATH_EXTRA, Collect.CACHE_PATH);
                    intent.putExtra(DIMENSIONS_EXTRA, new int[6]);
                    intent.putExtra(RETAKE_OPTION_EXTRA, false);
                }
                break;
            case R.id.choose_image:

                /* We aren't checking for storage permissions as without that permission
                 * FormEntryActivity cannot be started */

                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                break;
        }
        return intent;
    }
}