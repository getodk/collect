package org.odk.collect.android.widgets;

import android.content.Intent;
import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class SignatureWidgetTest extends FileWidgetTest<SignatureWidget> {

    @Mock
    File file;

    private String fileName;

    @NonNull
    @Override
    public SignatureWidget createWidget() {
        return new SignatureWidget(activity, formEntryPrompt);
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

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent);
    }
}