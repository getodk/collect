/*
 * Copyright (C) 2013 Nafundi
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
@SuppressLint("ViewConstructor")
public class BearingWidget extends QuestionWidget implements BinaryWidget {
    private final Button getBearingButton;
    private final boolean isSensorAvailable;
    private final EditText answer;
    private final Drawable textBackground;

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        isSensorAvailable = checkForRequiredSensors();

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        answer = getEditText();
        textBackground = answer.getBackground();
        answer.setBackground(null);

        getBearingButton = getSimpleButton(getContext().getString(R.string.get_bearing));
        getBearingButton.setEnabled(!prompt.isReadOnly());
        if (prompt.isReadOnly()) {
            getBearingButton.setVisibility(View.GONE);
        }

        answerLayout.addView(getBearingButton);
        answerLayout.addView(answer);

        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            getBearingButton.setText(getContext().getString(R.string.replace_bearing));
            if (!isSensorAvailable && answer != null) {
                answer.setText(s);
            }
            setBinaryData(s);
        }
        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        answer.setText(null);
        getBearingButton.setText(getContext().getString(R.string.get_bearing));
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answer.getText().toString();

        if (s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    @Override
    public void setBinaryData(Object answer) {
        this.answer.setText((String) answer);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (isSensorAvailable) {
            getBearingButton.setOnLongClickListener(l);
        }
        answer.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        if (isSensorAvailable) {
            getBearingButton.cancelLongPress();
        }
        answer.cancelLongPress();
    }

    private boolean checkForRequiredSensors() {

        boolean isAccelerometerSensorAvailable = false;
        boolean isMagneticFieldSensorAvailable = false;

        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            isAccelerometerSensorAvailable = true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            isMagneticFieldSensorAvailable = true;
        }

        return isAccelerometerSensorAvailable && isMagneticFieldSensorAvailable;
    }

    private EditText getEditText() {
        final EditText manualData = new EditText(getContext());
        manualData.setPadding(20, 20, 20, 20);
        manualData.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        manualData.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        manualData.setKeyListener(new DigitsKeyListener(true, true));
        manualData.setFocusable(false);
        manualData.setFocusableInTouchMode(false);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        manualData.setLayoutParams(params);

        return manualData;
    }

    @Override
    public void onButtonClick(int buttonId) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "recordBearing", "click",
                        getFormEntryPrompt().getIndex());

        if (isSensorAvailable) {
            Intent i = new Intent(getContext(), BearingActivity.class);
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.BEARING_CAPTURE);
        } else {
            getBearingButton.setEnabled(false);
            ToastUtils.showLongToast(R.string.bearing_lack_of_sensors);
            answer.setBackground(textBackground);
            answer.setFocusable(true);
            answer.setFocusableInTouchMode(true);
            answer.requestFocus();
        }
    }
}
