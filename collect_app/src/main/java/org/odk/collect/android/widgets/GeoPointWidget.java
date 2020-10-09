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
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.GeoButtonClickListener;
import org.odk.collect.android.widgets.utilities.GeoDataRequester;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;

@SuppressLint("ViewConstructor")
public class GeoPointWidget extends QuestionWidget implements WidgetDataReceiver {
    GeoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final GeoButtonClickListener geoButtonClickListener;
    private final double accuracyThreshold;

    public GeoPointWidget(Context context, QuestionDetails questionDetails, QuestionDef questionDef,
                          WaitingForDataRegistry waitingForDataRegistry, GeoButtonClickListener geoButtonClickListener) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.geoButtonClickListener = geoButtonClickListener;
        accuracyThreshold = GeoDataRequester.getAccuracyThreshold(questionDef);

        String stringAnswer = getFormEntryPrompt().getAnswerText();

        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            binding.geoAnswerText.setText(GeoDataRequester.getAnswerToDisplay(getContext(), stringAnswer));
            binding.simpleButton.setText(R.string.change_location);
        } else {
            binding.simpleButton.setText(R.string.get_point);
        }
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        if (prompt.isReadOnly()) {
            binding.simpleButton.setVisibility(GONE);
        } else {
            binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.simpleButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                String stringAnswer = prompt.getAnswerText();
                if (stringAnswer != null && !stringAnswer.isEmpty()) {
                    bundle.putDoubleArray(GeoDataRequester.LOCATION, GeoDataRequester.getLocationParamsFromStringAnswer(stringAnswer));
                }
                bundle.putDouble(GeoDataRequester.ACCURACY_THRESHOLD, accuracyThreshold);

                geoButtonClickListener.requestGeoIntent(context, prompt.getIndex(), getPermissionUtils(), waitingForDataRegistry,
                        GeoPointActivity.class, bundle, LOCATION_CAPTURE);
            });
        }

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        return binding.geoAnswerText.getText().equals("")
                ? null
                : new GeoPointData(GeoDataRequester.getLocationParamsFromStringAnswer(getFormEntryPrompt().getAnswerText()));
    }

    @Override
    public void clearAnswer() {
        binding.geoAnswerText.setText(null);
        binding.simpleButton.setText(R.string.get_point);
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
        binding.geoAnswerText.setText(GeoDataRequester.getAnswerToDisplay(getContext(), (String) answer));
        binding.simpleButton.setText(answer != null ? R.string.change_location : R.string.get_point);
        widgetValueChanged();
    }
}