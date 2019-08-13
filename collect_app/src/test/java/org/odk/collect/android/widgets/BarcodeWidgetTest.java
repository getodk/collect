package org.odk.collect.android.widgets;

import android.content.Intent;
import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;

/**
 * @author James Knight
 */

public class BarcodeWidgetTest extends BinaryWidgetTest<BarcodeWidget, StringData> {

    private String barcodeData;

    @NonNull
    @Override
    public BarcodeWidget createWidget() {
        return new BarcodeWidget(activity, formEntryPrompt);
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
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, ScannerWithFlashlightActivity.class, intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.simple_button));
    }
}
