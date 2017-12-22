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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
public class SmapChartHorizontalBarWidget extends SmapChartWidget {

    private boolean readOnly = true;
    private BarData data = null;
    private HorizontalBarChart chart = null;
    String dString = null;

    public SmapChartHorizontalBarWidget(Context context, FormEntryPrompt prompt, String appearance) {
        super(context, prompt, appearance);

        if(dString == null) {
            dString = getFormEntryPrompt().getAnswerText();
        }
        if(isStacked()) {
            data = getStackedBarData(dString);
        } else {
            data = getGroupedBarData(dString);
        }

        // Add a Horizontal Bar Widget
        chart = new HorizontalBarChart(context);
        chart.setScaleEnabled(false);
        addChart(chart);

        // Add data
        if(data != null) {
            float groupSpace = 0.06f;
            float barSpace = 0.02f; // x2 dataset
            float barWidth = 0.45f; // x2 dataset
            data.setBarWidth(barWidth); // set the width of each bar
            //chart.setData(data);
            data.setValueTextSize((float) 12.0);

            if(!isStacked()) {
                chart.groupBars(0f, groupSpace, barSpace); // perform the "explicit" grouping
            }
        }

        // the labels that should be drawn on the XAxis
        final String[] dsLabels = getDataSetLabels(dString);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int idx = (int) value;
                if(idx < dsLabels.length && dsLabels.length > 0) {
                    return dsLabels[idx];
                } else {
                    return "";
                }
            }
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        chart.setData(data);
        chart.invalidate();
    }

    private String [] getDataSetLabels(String sInput) {

        String [] dsLabels = {""};
        if(sInput != null && sInput.trim().length() > 0) {

            String[] components = sInput.split("==");
            if (components.length == 1) {
                //No labels must just be data
            } else if (components.length >= 2) {
                String sLabels = components[0];  // labels are first

                String [] labelComponents = sLabels.split("::");
                if(labelComponents.length > 1) {
                    dsLabels = labelComponents[0].split(":");
                }
            }
        }

        return dsLabels;
    }
    private BarData getStackedBarData(String sInput) {
        BarData data = null;
        int [] colors = ColorTemplate.PASTEL_COLORS;
        String sData = "";
        String sLabels = "";

        String [] entryLabels = {""};

        if(sInput != null && sInput.trim().length() > 0) {

            String [] components = sInput.split("==");
            if(components.length == 1) {
                sData = components[0];
            } else if(components.length >= 2) {
                sLabels = components[0];
                sData = components[1];
            }

            // Get the labels
            String [] labelComponents = sLabels.split("::");
            if(labelComponents.length == 1) {
                entryLabels = labelComponents[0].split(":");
            } else   if(labelComponents.length > 1) {
                // data set labels at position 0
                entryLabels = labelComponents[1].split(":");
            }

            // Get the data sets
            String [] barData = sData.split("::");
            ArrayList<BarEntry> entries = new ArrayList<> ();
            for(int i = 0; i < barData.length; i++) {
                entries.add(getStackedBarEntry((float) i, barData[i]));
            }

            ArrayList<Integer> finalColors = new ArrayList<Integer> ();
            for(int i = 0; i < numberOfEntries(sInput); i++) {
                finalColors.add(colors[i % colors.length]);
            }


            BarDataSet bds = new BarDataSet(entries, "");
            bds.setColors(finalColors);
            bds.setStackLabels(entryLabels);
            data = new BarData(bds);
        }

        return data;
    }

    private BarData getGroupedBarData(String sData) {
        BarData data = null;
        List <BarDataSet> dataSets = new ArrayList<BarDataSet> ();
        int [] colors = ColorTemplate.PASTEL_COLORS;

        if(sData != null && sData.trim().length() > 0) {

            String [] barData = sData.split("::");

            for(int i = 0; i < barData.length; i++) {
                ArrayList<BarEntry> entries = getGroupedBarEntries((float) i, barData[i]);

                BarDataSet bds = new BarDataSet(entries, "A Label" + i);
                bds.setColor(colors[i]);

                dataSets.add(bds);
            }
            data = new BarData((List) dataSets);
        }

        return data;
    }

    private BarEntry getStackedBarEntry(float idx, String barData) {
        BarEntry entry;
        String [] items = barData.split(" ");
        float [] values = new float [items.length];

        float total = 0;
        for(int i = 0; i < items.length; i++) {
            float f = 0f;
            try {
                f = Float.parseFloat(items[i]);
            } catch (Exception e) {
                // ignore errors
            }
            if(isNormalised()) {
                total += f;
            }
            values[i] = f;
        }

        if(isNormalised() && total > 0) {
            for(int i = 0; i < values.length; i++) {
                values[i] = values[i] * 100 / total;
            }
        }

        entry = new BarEntry(idx, values);
        return entry;
    }

    private int numberOfEntries(String sInput) {

        int length = 0;
        if(sInput != null && sInput.trim().length() > 0) {

            String[] components = sInput.split("==");
            String sData = "";
            if (components.length == 1) {
                sData = components[0];
            } else if (components.length >= 2) {
                sData = components[1];
            }

            String [] dataComponents = sData.split("::");
            if(dataComponents.length >= 1) {
                for(int i = 0; i < dataComponents.length; i++) {
                    String [] entries = dataComponents[0].split(" ");
                    if(entries.length > length) {
                        length = entries.length;
                    }
                }
            }
        }

        return length;
    }

    private ArrayList<BarEntry> getGroupedBarEntries(float idx, String barData) {

        ArrayList<BarEntry> entries = new ArrayList<> ();
        String [] items = barData.split(" ");
        float [] values = new float [items.length];

        for(int i = 0; i < items.length; i++) {
            float f = 0f;
            try {
                f = Float.parseFloat(items[i]);
            } catch (Exception e) {
                // ignore errors
            }
            entries.add(new BarEntry((float) i, f));
        }

        return entries;
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
