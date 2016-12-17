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

import org.odk.collect.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class TwoItemMultipleChoiceView extends RelativeLayout implements Checkable {

    public TwoItemMultipleChoiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public TwoItemMultipleChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public TwoItemMultipleChoiceView(Context context) {
        super(context);
    }


    @Override
    public boolean isChecked() {
        CheckBox c = (CheckBox) findViewById(R.id.checkbox);
        return c.isChecked();
    }


    @Override
    public void setChecked(boolean checked) {
        CheckBox c = (CheckBox) findViewById(R.id.checkbox);
        c.setChecked(checked);
    }


    @Override
    public void toggle() {
        CheckBox c = (CheckBox) findViewById(R.id.checkbox);
        c.setChecked(!c.isChecked());
    }

}
