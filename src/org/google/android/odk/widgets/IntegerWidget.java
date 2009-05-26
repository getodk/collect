/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.google.android.odk.widgets;

import org.google.android.odk.PromptElement;

import android.content.Context;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;


/**
 * Widget that restricts values to integers.
 * 
 * @author Carl Hartung
 */
public class IntegerWidget extends StringWidget {

    public IntegerWidget(Context context) {
        super(context);
    }


    public IntegerWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void buildView(PromptElement prompt) {
        super.buildView(prompt);

        // only allows numbers and no periods
        setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        setFilters(fa);
    }
}
