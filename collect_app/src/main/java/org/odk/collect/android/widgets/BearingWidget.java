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
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

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
    private Button getBearingButton;
    private TextView answerDisplay;
    private boolean isSensorAvailable = false;
    private EditText manualDataEntry;
    private LinearLayout answerLayout = new LinearLayout(getContext());

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        isSensorAvailable = checkForRequiredSensors();
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerDisplay = getCenteredAnswerTextView();

        getBearingButton = getSimpleButton(getContext().getString(R.string.get_bearing));
        getBearingButton.setEnabled(!prompt.isReadOnly());
        if (prompt.isReadOnly()) {
            getBearingButton.setVisibility(View.GONE);
        }
        // when you press the button
        getBearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordBearing", "click",
                                getFormEntryPrompt().getIndex());

                if (isSensorAvailable) {
                    Intent i;
                    i = new Intent(getContext(), BearingActivity.class);

                    waitForData();
                    ((Activity) getContext()).startActivityForResult(i,
                            RequestCodes.BEARING_CAPTURE);
                } else {
                    getBearingButton.setEnabled(false);
                    ToastUtils.showLongToast(R.string.bearing_lack_of_sensors);
                    manualDataEntry = getEditText();
                    manualDataEntry.setText(answerDisplay.getText().toString());
                    answerLayout.addView(manualDataEntry);
                }

            }
        });


        answerLayout.addView(getBearingButton);
        answerLayout.addView(answerDisplay);
        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {

            getBearingButton.setText(getContext().getString(R.string.replace_bearing));
            if (!isSensorAvailable && manualDataEntry != null) {
                    manualDataEntry.setText(s);
            }
            setBinaryData(s);
        }

        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        if (isSensorAvailable) {
            getBearingButton.setText(getContext().getString(R.string.get_bearing));
        } else if (manualDataEntry != null) {
            manualDataEntry.setText(null);
        }

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
        if (isSensorAvailable) {
            getBearingButton.setOnLongClickListener(l);
        }
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        if (isSensorAvailable) {
            getBearingButton.cancelLongPress();
        }
        answerDisplay.cancelLongPress();
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

        if (!isAccelerometerSensorAvailable || !isMagneticFieldSensorAvailable) {
            return false;
        }

        return true;
    }

    private EditText getEditText() {
        final EditText manualData = new EditText(getContext());
        manualData.setPadding(20, 20, 20, 20);
        manualData.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        manualData.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        manualData.setKeyListener(new DigitsKeyListener(true, true));

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        final InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(7);
        manualData.setFilters(fa);
        manualData.setLayoutParams(params);

        manualData.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                   String data = manualData.getText().toString();
                    if (isValidInput(Double.valueOf(data))) {
                        answerDisplay.setText(manualData.getText().toString());
                        ToastUtils.showShortToast("Data entered :" + data);
                        setFocus(getContext());
                        return true;
                    } else {
                        ToastUtils.showShortToast(R.string.enter_correct_data);
                        return false;
                    }
                }
                return false;
            }
        });
        return  manualData;
     }

    private boolean isValidInput(double input) {
    if (input >= 0 && input <= 360.0) {
        return true;
    }
    return false;
    }

}
