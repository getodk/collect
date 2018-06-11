package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author James Knight
 */
@RunWith(RobolectricTestRunner.class)
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

    @Test
    public void whenSensorUnavailable() {
        BearingWidget bearingWidget = getWidget();
        Button recordBearing = bearingWidget.getGetBearingButton();
        when(bearingWidget.checkForRequiredSensors()).thenReturn(false);
        recordBearing.performClick();
        EditText answer = bearingWidget.getEditTextAnswer();
        assertEquals(recordBearing.isEnabled(), false);
        assertEquals(answer.isFocusable(), true);
        assertEquals(answer.isFocused(), true);

    }

}
