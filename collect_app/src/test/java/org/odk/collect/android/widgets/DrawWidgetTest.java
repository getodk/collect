package org.odk.collect.android.widgets;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Button;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
@RunWith(RobolectricTestRunner.class)
public class DrawWidgetTest extends FileWidgetTest<DrawWidget> {

    @Mock
    File file;

    private String fileName;

    @NonNull
    @Override
    public DrawWidget createWidget() {
        return new DrawWidget(activity, formEntryPrompt);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(fileName);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return file;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        fileName = RandomString.make();
    }

    @Override
    protected void prepareForSetAnswer() {

        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(fileName);
    }

    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;
        switch (clickedButton.getId()) {
            case R.id.simple_button:
                intent = new Intent(activity, DrawActivity.class);
                intent.putExtra(DrawActivity.OPTION, DrawActivity.OPTION_DRAW);
                intent.putExtra(DrawActivity.EXTRA_OUTPUT, Uri.fromFile(new File(Collect.TMPFILE_PATH)));
                break;
        }
        return intent;
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent);
    }
}