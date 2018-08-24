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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoShapeGoogleMapActivity;
import org.odk.collect.android.activities.GeoShapeOsmMapActivity;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.requestLocationPermissions;

/**
 * GeoShapeWidget is the widget that allows the user to get Collect multiple GPS points.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoShapeWidget extends QuestionWidget implements BinaryWidget {

    public static final String SHAPE_LOCATION = "gp";
    public static final String GOOGLE_MAP_KEY = "google_maps";
    public SharedPreferences sharedPreferences;
    public String mapSDK;
    private final Button createShapeButton;
    private final TextView answerDisplay;

    public GeoShapeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        // assemble the widget...

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mapSDK = sharedPreferences.getString(PreferenceKeys.KEY_MAP_SDK, GOOGLE_MAP_KEY);

        answerDisplay = getCenteredAnswerTextView();

        createShapeButton = getSimpleButton(getContext().getString(R.string.get_shape));

        if (prompt.isReadOnly()) {
            createShapeButton.setEnabled(false);
        }

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(createShapeButton);
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

    private void startGeoShapeActivity() {
        Intent i;
        if (mapSDK.equals(GOOGLE_MAP_KEY)) {
            if (PlayServicesUtil.isGooglePlayServicesAvailable(getContext())) {
                i = new Intent(getContext(), GeoShapeGoogleMapActivity.class);
            } else {
                PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(getContext());
                return;
            }
        } else {
            i = new Intent(getContext(), GeoShapeOsmMapActivity.class);
        }
        String s = answerDisplay.getText().toString();
        if (s.length() != 0) {
            i.putExtra(SHAPE_LOCATION, s);
        }
        ((Activity) getContext()).startActivityForResult(i, RequestCodes.GEOSHAPE_CAPTURE);
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (dataAvailable) {
            createShapeButton.setText(
                    getContext().getString(R.string.geoshape_view_change_location));
        } else {
            createShapeButton.setText(getContext().getString(R.string.get_shape));
        }
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = answer.toString();
        answerDisplay.setText(s);
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();

        return !s.isEmpty()
                ? new StringData(s)
                : null;
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        createShapeButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void onButtonClick(int buttonId) {
        requestLocationPermissions((FormEntryActivity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitForData();
                startGeoShapeActivity();
            }

            @Override
            public void denied() {
            }
        });
    }
}