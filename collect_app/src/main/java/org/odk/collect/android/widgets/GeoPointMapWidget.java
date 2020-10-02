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
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.hasAppearance;

@SuppressLint("ViewConstructor")
public class GeoPointMapWidget extends QuestionWidget implements BinaryDataReceiver {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";
    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

    private final WaitingForDataRegistry waitingForDataRegistry;

    GeoWidgetAnswerBinding binding;

    private boolean readOnly;
    private boolean draggable = true;
    private double accuracyThreshold;
    private String stringAnswer;

    public GeoPointMapWidget(Context context, QuestionDetails questionDetails,
                             QuestionDef questionDef, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        determineMapProperties(questionDef);

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

        readOnly = prompt.isReadOnly();
        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.simpleButton.setOnClickListener(v -> onButtonClick());

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

    private void determineMapProperties(QuestionDef questionDef) {
        // Determine the accuracy threshold to use.
        String acc = questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        accuracyThreshold = acc != null && !acc.isEmpty() ? Double.parseDouble(acc) : DEFAULT_LOCATION_ACCURACY;

        // Determine whether the point should be draggable.
        if (hasAppearance(getFormEntryPrompt(), PLACEMENT_MAP)) {
            draggable = true;
        } else if (hasAppearance(getFormEntryPrompt(), MAPS)) {
            draggable = false;
        }
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
        if (readOnly) {
            if (dataAvailable) {
                binding.simpleButton.setText(R.string.geopoint_view_read_only);
            } else {
                binding.simpleButton.setVisibility(GONE);
            }
        } else {
            binding.simpleButton.setText(
                    dataAvailable ? R.string.view_change_location : R.string.get_point);
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
        Intent intent = new Intent(context, GeoPointMapActivity.class);

        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            intent.putExtra(LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
        intent.putExtra(READ_ONLY, readOnly);
        intent.putExtra(DRAGGABLE_ONLY, draggable);
        intent.putExtra(ACCURACY_THRESHOLD, accuracyThreshold);

        ((Activity) context).startActivityForResult(intent, RequestCodes.LOCATION_CAPTURE);
    }
}