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

package org.odk.collect.android.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.TextUtils;

public class HierarchyElementView extends RelativeLayout {

    private TextView primaryTextView;
    private TextView secondaryTextView;
    private ImageView icon;
    private int iconVisibility;

    public HierarchyElementView(Context context) {
        super(context);
    }

    public HierarchyElementView(Context context, HierarchyElement it) {
        super(context);

        setColor(it.getColor());

        View view = View.inflate(context, R.layout.hierarchy_item_element, null);
        icon = (ImageView) view.findViewById(R.id.icon);
        setIconVisibility(it.getDisplayIcon() == 1 ? View.VISIBLE : View.GONE);

        primaryTextView = (TextView) view.findViewById(R.id.primaryText);
        setPrimaryText(it.getPrimaryText());

        secondaryTextView = (TextView) view.findViewById(R.id.secondaryText);
        setSecondaryText(it.getSecondaryText());
        addView(view);
    }

    public void setPrimaryText(String text) {
        primaryTextView.setText(TextUtils.textToHtml(text));
    }


    public void setSecondaryText(String text) {
        secondaryTextView.setText(TextUtils.textToHtml(text));
    }

    public void setColor(int color) {
        setBackgroundColor(color);
    }

    public void showSecondary(boolean bool) {
        if (bool) {
            secondaryTextView.setVisibility(VISIBLE);
            setMinimumHeight(dipToPx(64));

        } else {
            secondaryTextView.setVisibility(GONE);
            setMinimumHeight(dipToPx(32));

        }
    }

    public int dipToPx(int dip) {
        return (int) (dip * getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setIconVisibility(int visibility) {
        icon.setVisibility(visibility);
    }
}
