package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.RuntimeEnvironment;

/**
 * @author James Knight
 */
public class BearingWidgetTest extends BinaryWidgetTest<BearingWidget, StringData> {

    private String barcodeData;

    @NonNull
    @Override
    public BearingWidget createWidget() {
        return new BearingWidget(RuntimeEnvironment.application, formEntryPrompt);
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
