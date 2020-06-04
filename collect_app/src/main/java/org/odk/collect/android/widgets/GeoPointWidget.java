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

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.hasAppearance;

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoPointWidget extends BaseGeoWidget {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";

    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;
    private final MapConfigurator mapConfigurator;
    private boolean useMap;
    private double accuracyThreshold;
    private boolean draggable = true;
    private String stringAnswer;

    public GeoPointWidget(Context context, QuestionDetails questionDetails, QuestionDef questionDef, MapConfigurator mapConfigurator) {
        super(context, questionDetails);
        this.mapConfigurator = mapConfigurator;

        stringAnswer = answerDisplay.getText().toString();
        determineMapProperties(questionDef);
        updateButtonLabelsAndVisibility(stringAnswer != null || !stringAnswer.isEmpty());
    }

    private void determineMapProperties(QuestionDef questionDef) {
        // Determine the accuracy threshold to use.
        String acc = questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        accuracyThreshold = acc != null && !acc.isEmpty() ? Double.parseDouble(acc) : DEFAULT_LOCATION_ACCURACY;

        // Determine whether to use the map and whether the point should be draggable.
        if (mapConfigurator.isAvailable(getContext())) {
            if (hasAppearance(getFormEntryPrompt(), PLACEMENT_MAP)) {
                draggable = true;
                useMap = true;
            } else if (hasAppearance(getFormEntryPrompt(), MAPS)) {
                draggable = false;
                useMap = true;
            }
        }
    }

    public void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (useMap) {
            if (readOnly) {
                startGeoButton.setText(R.string.geopoint_view_read_only);
            } else {
                startGeoButton.setText(
                        dataAvailable ? R.string.view_change_location : R.string.get_point);
            }
        } else {
            if (!readOnly) {
                startGeoButton.setText(
                    dataAvailable ? R.string.change_location : R.string.get_point);
            }
        }
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
        super.clearAnswer();
    }

    @Override
    public void setBinaryData(Object answer) {
        stringAnswer = (String) answer;
        answerDisplay.setText(getAnswerToDisplay(stringAnswer));

        if (answerDisplay.getText().toString().equals("")) {
            stringAnswer = null;
        }

        updateButtonLabelsAndVisibility(stringAnswer != null);
        widgetValueChanged();
    }

    @Override
    public String getAnswerToDisplay(String answer) {
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

    @Override
    public String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_location);
    }

    public void startGeoActivity() {
        Context context = getContext();
        Intent intent = new Intent(
            context, useMap ? GeoPointMapActivity.class : GeoPointActivity.class);

        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            intent.putExtra(LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
        intent.putExtra(READ_ONLY, readOnly);
        intent.putExtra(DRAGGABLE_ONLY, draggable);
        intent.putExtra(ACCURACY_THRESHOLD, accuracyThreshold);

        ((Activity) context).startActivityForResult(intent, RequestCodes.LOCATION_CAPTURE);
    }
}
