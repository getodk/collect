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
import org.jetbrains.annotations.Contract;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.getCenteredAnswerTextView;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class BarcodeWidget extends QuestionWidget implements BinaryWidget {
    final Button getBarcodeButton;
    final TextView stringAnswer;

    public BarcodeWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        getBarcodeButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.get_barcode), getAnswerFontSize(), this);

        stringAnswer = getCenteredAnswerTextView(getContext(), getAnswerFontSize());

        String s = questionDetails.getPrompt().getAnswerText();
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
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    public void clearAnswer() {
        stringAnswer.setText(null);
        getBarcodeButton.setText(getContext().getString(R.string.get_barcode));

        widgetValueChanged();
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
        stringAnswer.setText(stripInvalidCharacters(response));

        widgetValueChanged();
    }

    // Remove control characters, invisible characters and unused code points.
    @Contract("null -> null; !null -> !null")
    protected static String stripInvalidCharacters(String data) {
        if (data == null) {
            return null;
        }
        return data.replaceAll("\\p{C}", "");
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
        getPermissionUtils().requestCameraPermission((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitForData();

                IntentIntegrator intent = new IntentIntegrator((Activity) getContext())
                        .setCaptureActivity(ScannerWithFlashlightActivity.class);

                setCameraIdIfNeeded(intent);
                intent.initiateScan();
            }

            @Override
            public void denied() {
            }
        });
    }

    private void setCameraIdIfNeeded(IntentIntegrator intent) {
        String appearance = getFormEntryPrompt().getAppearanceHint();
        if (appearance != null && appearance.equalsIgnoreCase(WidgetAppearanceUtils.FRONT)) {
            if (CameraUtils.isFrontCameraAvailable()) {
                intent.addExtra(WidgetAppearanceUtils.FRONT, true);
            } else {
                ToastUtils.showLongToast(R.string.error_front_camera_unavailable);
            }
        }
    }
}
