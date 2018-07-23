/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.utilities.PermissionUtils.requestCameraPermission;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class BarcodeWidget extends QuestionWidget implements BinaryWidget {
    private final Button getBarcodeButton;
    private final TextView stringAnswer;

    public BarcodeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        getBarcodeButton = getSimpleButton(getContext().getString(R.string.get_barcode));
        getBarcodeButton.setEnabled(!prompt.isReadOnly());

        stringAnswer = getCenteredAnswerTextView();

        String s = prompt.getAnswerText();
        if (s != null) {
            getBarcodeButton.setText(getContext().getString(
                    R.string.replace_barcode));
            stringAnswer.setText(s);
        }
        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(getBarcodeButton);
        answerLayout.addView(stringAnswer);
        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        stringAnswer.setText(null);
        getBarcodeButton.setText(getContext().getString(R.string.get_barcode));
    }

    @Override
    public IAnswerData getAnswer() {
        String s = stringAnswer.getText().toString();
        if (s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    /**
     * Allows answer to be set externally in {@link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        String response = (String) answer;
        if (response != null) {      // It looks like the answer is not set to null even if no barcode captured, however it seems prudent to check
            response = response.replaceAll("\\p{C}", "");
        }
        stringAnswer.setText(response);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        stringAnswer.setOnLongClickListener(l);
        getBarcodeButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        getBarcodeButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        requestCameraPermission((FormEntryActivity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordBarcode", "click",
                                getFormEntryPrompt().getIndex());

                waitForData();

                new IntentIntegrator((Activity) getContext())
                        .setCaptureActivity(ScannerWithFlashlightActivity.class)
                        .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        .setOrientationLocked(false)
                        .setPrompt(getContext().getString(R.string.barcode_scanner_prompt))
                        .initiateScan();
            }

            @Override
            public void denied() {
            }
        });
    }
}
