package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.widgets.base.GeneralStringWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */
public class StringWidgetTest extends GeneralStringWidgetTest<StringWidget, StringData> {

    @NonNull
    @Override
    public StringWidget createWidget() {
        return new StringWidget(RuntimeEnvironment.application, formEntryPrompt, false);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }
}
