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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * GeoTraceWidget allows the user to collect a trace of GPS points as the
 * device moves along a path.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoTraceWidget extends BaseGeoWidget {

    public GeoTraceWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails, waitingForDataRegistry);
    }

    public void startGeoActivity() {
        Context context = getContext();
        if (MapProvider.getConfigurator().isAvailable(context)) {
            Intent intent = new Intent(context, GeoPolyActivity.class)
                .putExtra(GeoPolyActivity.ANSWER_KEY, answerDisplay.getText().toString())
                .putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.GEOTRACE_CAPTURE);
        } else {
            MapProvider.getConfigurator().showUnavailableMessage(context);
        }
    }

    public void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        startGeoButton.setText(dataAvailable ? R.string.geotrace_view_change_location : R.string.get_trace);
    }

    @Override
    public String getAnswerToDisplay(String answer) {
        return answer;
    }

    @Override
    public String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_trace);
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        return !s.equals("")
                ? new StringData(s)
                : null;
    }
}
