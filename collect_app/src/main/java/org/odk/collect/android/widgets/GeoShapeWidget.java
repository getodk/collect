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

/**
 * Widget for geoshape data type
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.GeoShapeData.GeoShape;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoShapeGoogleMapActivity;
import org.odk.collect.android.activities.GeoShapeOsmMapActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.PlayServicesUtil;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * GeoShapeWidget is the widget that allows the user to get Collect multiple GPS points.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

public class GeoShapeWidget extends QuestionWidget implements IBinaryWidget {
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    private final boolean readOnly;
    public static final String SHAPE_LOCATION = "gp";
    public static final String GOOGLE_MAP_KEY = "google_maps";
    private Button createShapeButton;
    private Button viewShapeButton;
    public SharedPreferences sharedPreferences;
    public String mapSDK;

    private TextView stringAnswer;
    private TextView answerDisplay;

    public GeoShapeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        // assemble the widget...

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mapSDK = sharedPreferences.getString(PreferenceKeys.KEY_MAP_SDK, GOOGLE_MAP_KEY);
        readOnly = prompt.isReadOnly();

        stringAnswer = new TextView(getContext());
        stringAnswer.setId(QuestionWidget.newUniqueId());

        answerDisplay = new TextView(getContext());
        answerDisplay.setId(QuestionWidget.newUniqueId());
        answerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        answerDisplay.setGravity(Gravity.CENTER);
        answerDisplay.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));

        createShapeButton = new Button(getContext());
        createShapeButton.setId(QuestionWidget.newUniqueId());
        createShapeButton.setText(getContext().getString(R.string.get_shape));
        createShapeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        createShapeButton.setPadding(20, 20, 20, 20);
        createShapeButton.setLayoutParams(params);

        createShapeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Collect.getInstance().getFormController().setIndexWaitingForData(formEntryPrompt.getIndex());
                startGeoShapeActivity();
            }
        });

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
        String s = stringAnswer.getText().toString();
        if (s.length() != 0) {
            i.putExtra(SHAPE_LOCATION, s);
        }
        ((Activity) getContext()).startActivityForResult(i, FormEntryActivity.GEOSHAPE_CAPTURE);
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
        // TODO Auto-generated method stub
        String s =  answer.toString();
        stringAnswer.setText(s);
        answerDisplay.setText(s);
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void cancelWaitingForBinaryData() {
        // TODO Auto-generated method stub
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        Boolean test = formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());

        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public IAnswerData getAnswer() {
        // TODO Auto-generated method stub

        GeoShapeData data = new GeoShapeData();
        ArrayList<double[]> list = new ArrayList<double[]>();
        String s = stringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                for (String sa :  s.split(";")) {
                    String[] sp = sa.trim().split(" ");
                    double[] gp = new double[4];
                    gp[0] = Double.valueOf(sp[0]);
                    gp[1] = Double.valueOf(sp[1]);
                    gp[2] = Double.valueOf(sp[2]);
                    gp[3] = Double.valueOf(sp[3]);
                    list.add(gp);
                }
                GeoShape shape = new GeoShape(list);
                return new StringData(s);
            } catch (NumberFormatException e) {
                Timber.e(e);
                return null;
            }
        }
    }

    @Override
    public void clearAnswer() {
        // TODO Auto-generated method stub
        stringAnswer.setText(null);
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
    }

    @Override
    public void setFocus(Context context) {
        // TODO Auto-generated method stub
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        createShapeButton.setOnLongClickListener(l);
        stringAnswer.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

}