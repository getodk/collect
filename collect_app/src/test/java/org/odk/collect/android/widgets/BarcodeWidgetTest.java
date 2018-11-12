package org.odk.collect.android.widgets;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
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

    @Override
    protected Intent getExpectedIntent(Button clickedButton, boolean permissionGranted) {
        Intent intent = null;
        switch (clickedButton.getId()) {
            case R.id.simple_button:
                if (permissionGranted) {
                    intent = new IntentIntegrator(activity)
                            .setCaptureActivity(ScannerWithFlashlightActivity.class)
                            .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                            .setOrientationLocked(false)
                            .setPrompt(activity.getString(R.string.barcode_scanner_prompt))
                            .createScanIntent();
                }
                break;
        }
        return intent;
    }
}
