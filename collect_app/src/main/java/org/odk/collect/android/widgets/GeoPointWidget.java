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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;

@SuppressLint("ViewConstructor")
public class GeoPointWidget extends QuestionWidget implements WidgetDataReceiver {
    GeoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final double accuracyThreshold;

    private String stringAnswer;

    public GeoPointWidget(Context context, QuestionDetails questionDetails, QuestionDef questionDef, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        accuracyThreshold = GeoWidgetUtils.getAccuracyThreshold(questionDef);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.simpleButton.setVisibility(GONE);
        } else {
            binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.simpleButton.setText(getDefaultButtonLabel());

            binding.simpleButton.setOnClickListener(v -> {
                Bundle bundle = GeoWidgetUtils.getGeoPointBundle(stringAnswer, accuracyThreshold, null, null);
                GeoWidgetUtils.onButtonClick(context, prompt, getPermissionUtils(), null,
                        waitingForDataRegistry, GeoPointActivity.class, bundle, LOCATION_CAPTURE);
            });
        }
        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        String stringAnswer = prompt.getAnswerText();
        boolean dataAvailable = false;
        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            dataAvailable = true;
            setData(stringAnswer);
        }
        updateButtonLabelsAndVisibility(dataAvailable);

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        return GeoWidgetUtils.getAnswer(stringAnswer);
    }

    @Override
    public void clearAnswer() {
        stringAnswer = null;
        binding.geoAnswerText.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.simpleButton.setOnLongClickListener(l);
        binding.geoAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.simpleButton.cancelLongPress();
        binding.geoAnswerText.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        stringAnswer = (String) answer;
        binding.geoAnswerText.setText(GeoWidgetUtils.getAnswerToDisplay(getContext(), stringAnswer));

        if (binding.geoAnswerText.getText().toString().equals("")) {
            stringAnswer = "";
        }

        updateButtonLabelsAndVisibility(stringAnswer != null);
        widgetValueChanged();
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (!getFormEntryPrompt().isReadOnly()) {
            binding.simpleButton.setText(
                    dataAvailable ? R.string.change_location : R.string.get_point);
        }
    }

    private String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_location);
    }
}