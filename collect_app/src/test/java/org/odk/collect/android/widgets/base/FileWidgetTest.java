package org.odk.collect.android.widgets.base;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class FileWidgetTest<W extends FileWidget> extends BinaryWidgetTest<W, StringData> {

    @Mock
    public File instancePath;

    @NonNull
    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(answerData.getDisplayText());
        when(file.getAbsolutePath()).thenReturn(answerData.getDisplayText());
        return file;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        when(formController.getInstanceFile()).thenReturn(instancePath);
        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        super.settingANewAnswerShouldRemoveTheOldAnswer();

        W widget = getSpyWidget();
        verify(widget).deleteFile();
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer();

        W widget = getSpyWidget();
        verify(widget).deleteFile();
    }
}