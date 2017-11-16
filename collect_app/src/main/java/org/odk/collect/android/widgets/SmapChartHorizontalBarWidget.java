/*
 * Copyright (C) 2011 University of Washington
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
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.ArrayList;
import java.util.List;

/**
 * The Label Widget does not return an answer. The purpose of this widget is to be the top entry in
 * a field-list with a bunch of list widgets below. This widget provides the labels, so that the
 * list widgets can hide their labels and reduce the screen clutter. This class is essentially
 * ListWidget with all the answer generating code removed.
 *
 * @author Jeff Beorse
 */
@SuppressLint("ViewConstructor")
public class SmapChartHorizontalBarWidget extends SmapChartWidget {

    boolean readOnly = true;

    public SmapChartHorizontalBarWidget(Context context, FormEntryPrompt prompt, String appearance) {
        super(context, prompt, appearance);

        String dString = prompt.getAnswerText();
        BarData data;
        if(isStacked()) {
            data = getStackedBarData(dString);
        } else {
            data = getGroupedBarData(dString);
        }

        // Add a Horizontal Bar Widget
        HorizontalBarChart chart = new HorizontalBarChart(context);
        addChart(chart);

        // Add data
        if(data != null) {
            chart.setData(data);
        }
        chart.invalidate();

        //addAnswerView(answerText);
    }

    private BarData getStackedBarData(String sData) {
        BarData data = null;

        if(sData != null && sData.trim().length() > 0) {

            String [] barData = sData.split("::");
            ArrayList<BarEntry> entries = new ArrayList<> ();
            for(int i = 0; i < barData.length; i++) {
                entries.add(getStackedBarEntry((float) i, barData[i]));
            }
            data = new BarData(new BarDataSet(entries, "A Label"));
        }

        return data;
    }

    private BarData getGroupedBarData(String s) {
        BarData data = null;

        return data;
    }

    private BarEntry getStackedBarEntry(float idx, String barData) {
        BarEntry entry = null;
        String [] items = barData.split(" ");
        float [] values = new float [items.length];

        for(int i = 0; i < items.length; i++) {
            float f = 0f;
            try {
                f = Float.parseFloat(items[i]);
            } catch (Exception e) {
                // ignore errors
            }
            values[i] = f;
        }

        entry = new BarEntry(idx, values);
        return entry;
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
        return "";
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

}
