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
import android.content.Intent;
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
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.widgets.GeoPointMapWidget.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.GeoPointMapWidget.DEFAULT_LOCATION_ACCURACY;
import static org.odk.collect.android.widgets.GeoPointMapWidget.LOCATION;

@SuppressLint("ViewConstructor")
public class GeoPointWidget extends QuestionWidget implements BinaryDataReceiver {

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final double accuracyThreshold;

    GeoWidgetAnswerBinding binding;

    private boolean readOnly;
    private String stringAnswer;

    public GeoPointWidget(Context context, QuestionDetails questionDetails, QuestionDef questionDef, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;

        // Determine the accuracy threshold to use.
        String acc = questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        accuracyThreshold = acc != null && !acc.isEmpty() ? Double.parseDouble(acc) : DEFAULT_LOCATION_ACCURACY;

        stringAnswer = getFormEntryPrompt().getAnswerText();
        boolean dataAvailable = false;
        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            dataAvailable = true;
            setBinaryData(stringAnswer);
        }
        updateButtonLabelsAndVisibility(dataAvailable);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        readOnly = prompt.isReadOnly();
        if (readOnly) {
            binding.simpleButton.setVisibility(GONE);
        } else {
            binding.simpleButton.setText(getDefaultButtonLabel());
            binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.simpleButton.setOnClickListener(v -> onButtonClick());
        }
        return answerView;
    }

    @Override
    public IAnswerData getAnswer() {
        if (stringAnswer == null || stringAnswer.isEmpty()) {
            return null;
        } else {
            return new GeoPointData(GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
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
    public void setBinaryData(Object answer) {
        stringAnswer = (String) answer;
        binding.geoAnswerText.setText(getAnswerToDisplay(stringAnswer));

        if (binding.geoAnswerText.getText().toString().equals("")) {
            stringAnswer = "";
        }

        updateButtonLabelsAndVisibility(stringAnswer != null);
        widgetValueChanged();
    }

    private String getAnswerToDisplay(String answer) {
        try {
            if (answer != null && !answer.isEmpty()) {
                String[] parts = answer.split(" ");
                return getContext().getString(
                        R.string.gps_result,
                        GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(getContext(), Double.parseDouble(parts[0]), "lat"),
                        GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(getContext(), Double.parseDouble(parts[1]), "lon"),
                        GeoWidgetUtils.truncateDouble(parts[2]),
                        GeoWidgetUtils.truncateDouble(parts[3])
                );
            }
        } catch (NumberFormatException e) {
            return "";
        }
        return "";
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (!readOnly) {
            binding.simpleButton.setText(
                    dataAvailable ? R.string.change_location : R.string.get_point);
        }
    }

    private void onButtonClick() {
        if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
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
    }

    private void startGeoActivity() {
        Context context = getContext();
        Intent intent = new Intent(context, GeoPointActivity.class);

        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            intent.putExtra(LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
        intent.putExtra(ACCURACY_THRESHOLD, accuracyThreshold);

        ((Activity) context).startActivityForResult(intent, RequestCodes.LOCATION_CAPTURE);
    }

    private String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_location);
    }
}