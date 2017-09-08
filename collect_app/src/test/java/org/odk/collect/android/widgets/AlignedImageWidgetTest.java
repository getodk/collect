package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.mockito.Mockito.when;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AlignedImageWidgetTest extends BinaryNameWidgetTest<AlignedImageWidget> {

    @Mock
    File file;

    public AlignedImageWidgetTest() {
        super(AlignedImageWidget.class);
    }

    @NonNull
    @Override
    public AlignedImageWidget createWidget() {
        return new AlignedImageWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    Object createBinaryData(StringData answerData) {
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(answerData.getDisplayText());

        return file;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(formEntryPrompt.getAppearanceHint()).thenReturn("0");
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
    }
}