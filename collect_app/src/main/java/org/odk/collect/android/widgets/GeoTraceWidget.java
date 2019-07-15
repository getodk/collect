/*
 * Copyright (C) 2015 GeoODK
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.map.MapConfigurator;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * GeoTraceWidget allows the user to collect a trace of GPS points as the
 * device moves along a path.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class GeoTraceWidget extends QuestionWidget implements BinaryWidget {

    private final Button createTraceButton;
    private final TextView answerDisplay;

    public GeoTraceWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        answerDisplay = getCenteredAnswerTextView();

        createTraceButton = getSimpleButton(getContext().getString(R.string.get_trace));

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(createTraceButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout);

        boolean dataAvailable = false;
        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            dataAvailable = true;
            setBinaryData(s);
        }

        updateButtonLabelsAndVisibility(dataAvailable);
    }

    private void startGeoTraceActivity() {
        Context context = getContext();
        MapConfigurator.Option current = MapConfigurator.getCurrent(context);
        if (MapConfigurator.getCurrent(context).source.isAvailable(context)) {
            Intent intent = new Intent(context, GeoPolyActivity.class)
                .putExtra(GeoPolyActivity.ANSWER_KEY, answerDisplay.getText().toString())
                .putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.GEOTRACE_CAPTURE);
        } else {

        }
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (dataAvailable) {
            createTraceButton.setText(R.string.geotrace_view_change_location);
        } else {
            createTraceButton.setText(R.string.get_trace);
        }
    }

    @Override
    public void setBinaryData(Object answer) {
        String answerText = answer.toString();
        answerDisplay.setText(answerText);
        updateButtonLabelsAndVisibility(!answerText.isEmpty());
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        return !s.equals("")
                ? new StringData(s)
                : null;
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        createTraceButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void onButtonClick(int buttonId) {
        getPermissionUtils().requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitForData();
                startGeoTraceActivity();
            }

            @Override
            public void denied() {
            }
        });
    }
}
