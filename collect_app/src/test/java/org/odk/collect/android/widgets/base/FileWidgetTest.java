package org.odk.collect.android.widgets.base;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;

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
    public void setUp() throws Exception {
        super.setUp();

        when(formController.getInstanceFile()).thenReturn(instancePath);
        when(instancePath.getParent()).thenReturn("");
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        prepareAnswerFile();

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

    @Override
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        prepareAnswerFile();
        super.getAnswerShouldReturnCorrectAnswerAfterBeingSet();
    }

    @Override
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        prepareAnswerFile();
        super.settingANewAnswerShouldRemoveTheOldAnswer();
    }

    /**
     * Override this to provide additional set-up prior to testing any set answer methods.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void prepareAnswerFile() {
        // Default implementation does nothing.
    }
}