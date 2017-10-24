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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
@SuppressLint("ViewConstructor")
public class BearingWidget extends QuestionWidget implements BinaryWidget {
    private Button getBearingButton;
    private TextView answerDisplay;

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        getBearingButton = getSimpleButton(getContext().getString(R.string.get_bearing));
        getBearingButton.setEnabled(!prompt.isReadOnly());
        if (prompt.isReadOnly()) {
            getBearingButton.setVisibility(View.GONE);
        }

        answerDisplay = getCenteredAnswerTextView();

        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            getBearingButton.setText(getContext().getString(
                    R.string.replace_bearing));
            setBinaryData(s);
        }

        // when you press the button
        getBearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordBearing", "click",
                                formEntryPrompt.getIndex());
                Intent i;
                i = new Intent(getContext(), BearingActivity.class);

                waitForData();
                ((Activity) getContext()).startActivityForResult(i,
                        RequestCodes.BEARING_CAPTURE);
            }
        });

        checkForRequiredSensors();

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(getBearingButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        getBearingButton.setText(getContext()
                .getString(R.string.get_bearing));
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        if (s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setBinaryData(Object answer) {
        answerDisplay.setText((String) answer);
        cancelWaitingForData();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        getBearingButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        getBearingButton.cancelLongPress();
        answerDisplay.cancelLongPress();
    }

    private void checkForRequiredSensors() {
        boolean isAccelerometerSensorAvailable = false;
        boolean isMagneticFieldSensorAvailable = false;

        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            isAccelerometerSensorAvailable = true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            isMagneticFieldSensorAvailable = true;
        }

        if (!isAccelerometerSensorAvailable || ! isMagneticFieldSensorAvailable) {
            getBearingButton.setEnabled(false);
            ToastUtils.showLongToast(R.string.bearing_lack_of_sensors);
        }
    }
}
