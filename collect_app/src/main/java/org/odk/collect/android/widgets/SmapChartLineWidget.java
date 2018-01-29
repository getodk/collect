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
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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
public class SmapChartLineWidget extends SmapChartWidget {

    boolean readOnly = true;
    LineChart chart = null;
    List<List<Entry>> datasets = null;


    public SmapChartLineWidget(Context context, FormEntryPrompt prompt, String appearance) {
        super(context, prompt, appearance);

        String s = getFormEntryPrompt().getAnswerText();
        datasets = getLineEntries(s);

        // programmatically create a LineChart
        chart = new LineChart(context);
        addChart(chart);

        String label = "";
        if(datasets.size() > 0) {
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            int idx = 0;
            for(List<Entry> ds : datasets) {
                if(dsLabels.size() > idx) {
                    label = dsLabels.get(idx);
                }
                LineDataSet dataSet = new LineDataSet(ds, label);
                dataSets.add(dataSet);
                dataSet.setColors(DEFAULT_COLORS[idx]);
                dataSet.setLineWidth(4f);
                idx++;
            }

            LineData lineData = new LineData(dataSets);

            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);

            chart.setData(lineData);
        }
        chart.invalidate();

        //addAnswerView(answerText);
    }

    private List<List<Entry>> getLineEntries(String sInput) {

        List<List<Entry>> dataSets = new ArrayList<List<Entry>> ();

        String sData = "";

        if(sInput != null && sInput.trim().length() > 0) {

            String [] components = sInput.split("==");
            if(components.length == 1) {
                sData = components[0];
            } else if(components.length >= 2) {
                sData = components[1];
            }

            // Get the data sets
            String [] dsArray = sData.split("::");
            for(int i = 0; i < dsArray.length; i++) {
                List<Entry> entries = new ArrayList<Entry>();

                String [] vArray = dsArray[i].split(":");
                for(int j = 0; j < vArray.length; j++) {
                    String [] point = vArray[j].split("#");
                    try {
                        int x = j;
                        float y;
                        if(point.length > 1) {
                            x = Integer.parseInt(point[0]);
                            y = Float.parseFloat(point[1]);
                        } else {
                            y = Float.parseFloat(point[0]);
                        }

                        entries.add(new Entry(x, y));
                    } catch (Exception e) {

                    }
                }
                dataSets.add(entries);
            }


        }

        return dataSets;
    }

    @Override
    public void clearAnswer() {

    }


    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        String s = getAnswerText();
        return (s != null && !s.equals("")) ? new StringData(s) : null;
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
