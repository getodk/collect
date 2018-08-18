package org.odk.collect.android.widgets;

import android.content.Context;
import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */
public class BearingWidgetTest extends BinaryWidgetTest<BearingWidget, StringData> {

    private String barcodeData;

    public class MockBearingWidget extends BearingWidget {

        public MockBearingWidget(Context context, FormEntryPrompt prompt) {
            super(context, prompt);
        }

        @Override
        public boolean checkForRequiredSensors() {
            return false;
        }

        @Override
        public void setAnswer(String s) {
            if (answer != null) {
                answer.setText(s);
            }
        }
    }

    @NonNull
    @Override
    public BearingWidget createWidget() {
        return new MockBearingWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return barcodeData;
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(barcodeData);
    }

    @Override
    public StringData getInitialAnswer() {
        return new StringData(RandomString.make());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        barcodeData = RandomString.make();
    }
}
