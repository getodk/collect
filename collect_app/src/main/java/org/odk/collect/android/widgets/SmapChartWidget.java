/*
 * Copyright Smap Consulting 2017
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
import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.SmapChartMarker;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 *
 *
 */
@SuppressLint("ViewConstructor")
public abstract class SmapChartWidget extends QuestionWidget {

    private static final String CHART_DATA = "chart_data";
    public static final int[] DEFAULT_COLORS = {    // green, red, blue, yellow, purple, orange, light blue
            rgb("#037c1e"), rgb("#e2180d"), rgb("#0744ed"), rgb("#f9f10c"), rgb("#6c33e8"),
            rgb("#ed5d04"), rgb("#07ceed")
    };

    String appearance;
    boolean stacked = false;
    boolean normalised = false;

    protected List<String> xLabels = null;
    protected List<String> dsLabels = null;
    IAxisValueFormatter formatter = null;
    SmapChartMarker mv = null;

    public SmapChartWidget(Context context, FormEntryPrompt prompt, String appearance) {
        super(context, prompt);
        this.appearance = appearance;

        String stackString = prompt.getQuestion().getAdditionalAttribute(null, "stacked");
        if(stackString != null && (stackString.equals("yes") || stackString.equals("true"))) {
            stacked = true;
        }

        String normalisedString = prompt.getQuestion().getAdditionalAttribute(null, "normalised");
        if(normalisedString != null && (normalisedString.equals("yes") || normalisedString.equals("true"))) {
            normalised = true;
        }

        // the labels that should be drawn on the XAxis - These labels are added by the SmapChartWidget
        getLabels(prompt.getAnswerText());

        formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                List<String> labels = xLabels;
                if(isStacked()) {
                    labels = dsLabels;
                }
                int idx = (int) value;
                if(labels != null && idx < labels.size() && labels.size() > 0) {
                    return labels.get(idx);
                } else {
                    return "";
                }
            }
        };
    }

    void addChart(Chart chart) {
        chart.setMinimumHeight(800);

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        chart.setPadding(10, 10, 10, 10);
        chart.setExtraLeftOffset(10);
        chart.setExtraRightOffset(10);
        answerLayout.addView(chart);
        addAnswerView(answerLayout);
    }

    boolean isStacked() {
       return stacked;
    }

    protected boolean isNormalised() {
        return normalised;
    }

    @Override
    public void clearAnswer() {

    }


    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        String s = getAnswerText();
        return !s.equals("") ? new StringData(s) : null;
    }

    @NonNull
    public String getAnswerText() {
        return getFormEntryPrompt().getAnswerText();
    }

    @Override
    public void setFocus(Context context) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

    }

    private void getLabels(String sInput) {

        xLabels = new ArrayList<>();
        dsLabels = new ArrayList<>();

        String labelsString = null;
        String xLabelString = null;
        String dsLabelString = null;
        if(sInput != null && sInput.trim().length() > 0) {

            String[] components = sInput.split("==");
            if (components.length == 1) {
                // No Labels
            } else if (components.length >= 2) {
                labelsString = components[0];
                components = labelsString.split("::");
                if(components.length == 1) {
                    xLabelString = components[0];
                } else if (components.length >= 2) {
                    xLabelString = components[0];
                    dsLabelString = components[1];
                }
            }
            if(xLabelString != null) {
                String [] vArray = xLabelString.split(":");
                for(int i = 0; i < vArray.length; i++) {
                    xLabels.add(vArray[i]);
                }
            }
            if(dsLabelString != null) {
                String [] vArray = dsLabelString.split(":");
                for(int i = 0; i < vArray.length; i++) {
                    dsLabels.add(vArray[i]);
                }
            }
        }
    }

    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

}
