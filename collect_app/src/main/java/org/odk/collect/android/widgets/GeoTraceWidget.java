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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoTraceGoogleMapActivity;
import org.odk.collect.android.activities.GeoTraceOsmMapActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.PlayServicesUtil;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * GeoShapeTrace is the widget that allows the user to get Collect multiple GPS points based on the
 * locations.
 *
 * Date
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

public class GeoTraceWidget extends QuestionWidget implements IBinaryWidget {

    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    private final boolean readOnly;
    public static final String TRACE_LOCATION = "gp";
    private Button createTraceButton;
    private Button viewShapeButton;
    public static final String GOOGLE_MAP_KEY = "google_maps";
    public SharedPreferences sharedPreferences;
    public String mapSDK;

    private TextView stringAnswer;
    private TextView answerDisplay;

    public GeoTraceWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
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

        createTraceButton = new Button(getContext());
        createTraceButton.setId(QuestionWidget.newUniqueId());
        createTraceButton.setText(getContext().getString(R.string.get_trace));
        createTraceButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        createTraceButton.setPadding(20, 20, 20, 20);
        createTraceButton.setLayoutParams(params);

        createTraceButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Collect.getInstance().getFormController().setIndexWaitingForData(formEntryPrompt.getIndex());
                startGeoTraceActivity();

            }
        });

        if (prompt.isReadOnly()) {
            createTraceButton.setEnabled(false);
        }

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(createTraceButton);
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

    private void startGeoTraceActivity() {
        Intent i;
        if (mapSDK.equals(GOOGLE_MAP_KEY)) {
            if (PlayServicesUtil.isGooglePlayServicesAvailable(getContext())) {
                i = new Intent(getContext(), GeoTraceGoogleMapActivity.class);
            } else {
                PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(getContext());
                return;
            }
        } else {
            i = new Intent(getContext(), GeoTraceOsmMapActivity.class);
        }
        String s = stringAnswer.getText().toString();
        if (s.length() != 0) {
            i.putExtra(TRACE_LOCATION, s);
        }
        ((Activity) getContext()).startActivityForResult(i, FormEntryActivity.GEOTRACE_CAPTURE);
    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        if (dataAvailable) {
            createTraceButton.setText(
                    getContext().getString(R.string.geotrace_view_change_location));
        } else {
            createTraceButton.setText(getContext().getString(R.string.get_trace));
        }
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = answer.toString();
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
        // TODO Auto-generated method stub
        Boolean test = formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());

    }

    @Override
    public IAnswerData getAnswer() {
        ArrayList<double[]> list = new ArrayList<double[]>();
        String s = stringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                String[] sa = s.split(";");
                for (int i = 0; i < sa.length; i++) {
                    String[] sp = sa[i].trim().split(" ");
                    double[] gp = new double[4];
                    gp[0] = Double.valueOf(sp[0]);
                    gp[1] = Double.valueOf(sp[1]);
                    gp[2] = Double.valueOf(sp[2]);
                    gp[3] = Double.valueOf(sp[3]);
                }
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
        createTraceButton.setOnLongClickListener(l);
        stringAnswer.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

}