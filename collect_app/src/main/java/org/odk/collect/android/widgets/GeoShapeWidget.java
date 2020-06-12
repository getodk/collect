/*
 * Copyright (C) 2014 GeoODK
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * GeoShapeWidget is the widget that allows the user to get Collect multiple GPS points.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoShapeWidget extends QuestionWidget implements BinaryDataReceiver {
    private final WaitingForDataRegistry waitingForDataRegistry;

    protected Button startGeoButton;
    protected TextView answerDisplay;

    public GeoShapeWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    public void startGeoActivity() {
        Intent intent = new Intent(getContext(), GeoPolyActivity.class)
            .putExtra(GeoPolyActivity.ANSWER_KEY, answerDisplay.getText().toString())
            .putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
        ((Activity) getContext()).startActivityForResult(intent, RequestCodes.GEOSHAPE_CAPTURE);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        ViewGroup answerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.base_geo_widget_layout, null);

        answerDisplay = answerView.findViewById(R.id.geo_answer_text);
        answerDisplay.setTextColor(new ThemeUtils(context).getColorOnSurface());
        answerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        startGeoButton = answerView.findViewById(R.id.simple_button);

        if (prompt.isReadOnly()) {
            startGeoButton.setVisibility(GONE);
        } else {
            startGeoButton.setText(getDefaultButtonLabel());
            startGeoButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            startGeoButton.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
                    this.onButtonClick();
                }
            });
        }
        String answerText = prompt.getAnswerText();
        boolean dataAvailable = false;

        if (answerText != null && !answerText.isEmpty()) {
            dataAvailable = true;
            setBinaryData(answerText);
        }

        updateButtonLabelsAndVisibility(dataAvailable);
        return answerView;
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        startGeoButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        startGeoButton.cancelLongPress();
        answerDisplay.cancelLongPress();
    }

    @Override
    public void setBinaryData(Object answer) {
        answerDisplay.setText(answer.toString());
        updateButtonLabelsAndVisibility(!answer.toString().isEmpty());
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        return !s.isEmpty()
                ? new StringData(s)
                : null;
    }

    private void onButtonClick() {
        getPermissionUtils().requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
                startGeoActivity();
            }

            @Override
            public void denied() {
            }
        });
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        startGeoButton.setText(dataAvailable ? R.string.geoshape_view_change_location : R.string.get_shape);
    }

    private String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_shape);
    }
}
