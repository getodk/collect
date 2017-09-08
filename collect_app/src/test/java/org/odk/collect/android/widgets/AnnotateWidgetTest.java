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

/**
 * @author James Knight
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class AnnotateWidgetTest extends BinaryNameWidgetTest<AnnotateWidget> {
    @Mock
    File file;

    public AnnotateWidgetTest() {
        super(AnnotateWidget.class);
    }

    @NonNull
    @Override
    public AnnotateWidget createWidget() {
        return new AnnotateWidget(RuntimeEnvironment.application, formEntryPrompt);
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
        when(formEntryPrompt.isReadOnly()).thenReturn(false);
    }
}