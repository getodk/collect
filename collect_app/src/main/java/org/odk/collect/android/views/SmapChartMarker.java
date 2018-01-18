package org.odk.collect.android.views;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import org.odk.collect.android.R;

/**
 * Created by neilpenman on 18/01/2018.
 */

public class SmapChartMarker extends MarkerView {

    private TextView textContent;

    public SmapChartMarker (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        textContent = (TextView) findViewById(R.id.smapMarkerView);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        textContent.setText("" + e.toString()); // set the entry-value as the display text
    }


}
