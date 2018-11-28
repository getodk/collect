package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Button;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.junit.Assert.assertNull;
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
                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                }
                break;

            /* We aren't checking for storage permissions as without that permission
             * FormEntryActivity cannot be started */
            case R.id.choose_image:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                break;
            case R.id.markup_image:
                intent = new Intent(activity, DrawActivity.class);
                intent.putExtra(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE);
                intent.putExtra(DrawActivity.EXTRA_OUTPUT, Uri.fromFile(new File(Collect.TMPFILE_PATH)));
                intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0);
                break;
        }
        return intent;
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_image);
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_image);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);

        intent = getIntentLaunchedByClick(R.id.markup_image);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertNull(getIntentLaunchedByClick(R.id.capture_image));
    }
}