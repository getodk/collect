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
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;

/**
 * GeoTraceWidget allows the user to collect a trace of GPS points as the
 * device moves along a path.
 */
@SuppressLint("ViewConstructor")
public class GeoTraceWidget extends QuestionWidget implements BinaryDataReceiver {
    GeoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final MapConfigurator mapConfigurator;

    public GeoTraceWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry,
                          MapConfigurator mapConfigurator) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.mapConfigurator = mapConfigurator;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.simpleButton.setVisibility(GONE);
        } else {
            binding.simpleButton.setText(getDefaultButtonLabel());
            binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.simpleButton.setOnClickListener(v -> {
                Bundle bundle = GeoWidgetUtils.getGeoPolyActivityBundle(binding.geoAnswerText.getText().toString(),
                        GeoPolyActivity.OutputMode.GEOTRACE);
                GeoWidgetUtils.onButtonClick(context, prompt, getPermissionUtils(), mapConfigurator,
                        waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOTRACE_CAPTURE);
            });
        }
        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        String answerText = prompt.getAnswerText();
        boolean dataAvailable = false;

        if (answerText != null && !answerText.isEmpty()) {
            dataAvailable = true;
            setBinaryData(answerText);
        }
        updateButtonLabelsAndVisibility(dataAvailable);

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = binding.geoAnswerText.getText().toString();
        return !s.equals("")
                ? new StringData(s)
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.simpleButton.setOnLongClickListener(l);
        binding.geoAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void clearAnswer() {
        binding.geoAnswerText.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setBinaryData(Object answer) {
        binding.geoAnswerText.setText(answer.toString());
        updateButtonLabelsAndVisibility(!answer.toString().isEmpty());
        widgetValueChanged();
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        binding.simpleButton.setText(dataAvailable ? R.string.geotrace_view_change_location : R.string.get_trace);
    }

    private String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_trace);
    }
}
