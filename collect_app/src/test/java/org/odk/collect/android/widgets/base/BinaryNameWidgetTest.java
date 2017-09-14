package org.odk.collect.android.widgets.base;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.widgets.IBinaryNameWidget;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;


@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public abstract class BinaryNameWidgetTest<W extends IBinaryNameWidget> extends BinaryWidgetTest<W, StringData> {

    public BinaryNameWidgetTest(Class<W> clazz) {
        super(clazz);
    }

    @NonNull
    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        super.settingANewAnswerShouldRemoveTheOldAnswer();

        W widget = getWidget();
        verify(widget).deleteMedia();
    }

    @Test
    public void callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer();

        W widget = getWidget();
        verify(widget).deleteMedia();
    }
}