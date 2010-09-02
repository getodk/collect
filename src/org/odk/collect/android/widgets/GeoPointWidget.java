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

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.views.AbstractFolioView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class GeoPointWidget extends AbstractQuestionWidget implements IBinaryWidget {

    private Button mActionButton;
    private TextView mStringAnswer;
    private TextView mAnswerDisplay;


    public GeoPointWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
    }

    @Override
	public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                // segment lat and lon
                String[] sa = s.split(" ");
                double gp[] = new double[4];
                gp[0] = Double.valueOf(sa[0]).doubleValue();
                gp[1] = Double.valueOf(sa[1]).doubleValue();
                gp[2] = Double.valueOf(sa[2]).doubleValue();
                gp[3] = Double.valueOf(sa[3]).doubleValue();

                return new GeoPointData(gp);
            } catch (Exception NumberFormatException) {
                return null;
            }
        }
    }

    @Override
    protected void buildViewBodyImpl() {
        
        mActionButton = new Button(getContext());
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setText(getContext().getString(R.string.get_location));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, AbstractFolioView.APPLICATION_FONTSIZE);
        mActionButton.setEnabled(!prompt.isReadOnly());

        mStringAnswer = new TextView(getContext());

        mAnswerDisplay = new TextView(getContext());
        mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            AbstractFolioView.APPLICATION_FONTSIZE - 1);
        mAnswerDisplay.setGravity(Gravity.CENTER);

        // when you press the button
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	// touches are not focus change events 
            	signalDescendant(true);
                Intent i = new Intent(getContext(), GeoPointActivity.class);
                ((Activity) getContext()).startActivityForResult(i,
                    FormEntryActivity.LOCATION_CAPTURE);

            }
        });

        // finish complex layout
        addView(mActionButton);
        addView(mAnswerDisplay);
    }

    protected void updateViewAfterAnswer() {
        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            mActionButton.setText(getContext().getString(R.string.replace_location));
	        mStringAnswer.setText(s);
            String[] sa = s.split(" ");
            mAnswerDisplay.setText(getContext().getString(R.string.latitude) + ": "
                    + formatGps(Double.parseDouble(sa[0]), "lat") + "\n"
                    + getContext().getString(R.string.longitude) + ": "
                    + formatGps(Double.parseDouble(sa[1]), "lon") + "\n"
                    + getContext().getString(R.string.altitude) + ": " + sa[2] + "\n"
                    + getContext().getString(R.string.accuracy) + ": " + sa[3] + "m");
        } else {
	        mActionButton.setText(getContext().getString(R.string.get_location));
	        mStringAnswer.setText(null);
	        mAnswerDisplay.setText(null);
        }
    }

    private String formatGps(double coordinates, String type) {
        String location = Double.toString(coordinates);
        String degreeSign = "\u00B0";
        String degree = location.substring(0, location.indexOf(".")) + degreeSign;
        location = "0." + location.substring(location.indexOf(".") + 1);
        double temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String mins = location.substring(0, location.indexOf(".")) + "'";

        location = "0." + location.substring(location.indexOf(".") + 1);
        temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String secs = location.substring(0, location.indexOf(".")) + '"';
        if (type.equalsIgnoreCase("lon")) {
            if (degree.startsWith("-")) {
                degree = "W " + degree.replace("-", "") + mins + secs;
            } else
                degree = "E " + degree.replace("-", "") + mins + secs;
        } else {
            if (degree.startsWith("-")) {
                degree = "S " + degree.replace("-", "") + mins + secs;
            } else
                degree = "N " + degree.replace("-", "") + mins + secs;
        }
        return degree;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    	mAnswerDisplay.setEnabled(isEnabled);
    	mActionButton.setEnabled(isEnabled && !prompt.isReadOnly());
    }

    @Override
	public void setBinaryData(Object answer) {
        String s = (String) answer;
        mStringAnswer.setText(s);
        saveAnswer(true); // and evaluate constraints and trigger UI update...
    }
}
