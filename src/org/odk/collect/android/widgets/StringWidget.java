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

package org.odk.collect.android.widgets;

import android.R;
import android.content.Context;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.SharedConstants;

/**
 * The most basic widget that allows for entry of any text.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class StringWidget extends EditText implements IQuestionWidget {


    public StringWidget(Context context) {
        this(context, null);
    }


    public StringWidget(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }


    public StringWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void clearAnswer() {
        setText(null);
    }


    public IAnswerData getAnswer() {
        String s = getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    public void buildView(PromptElement prompt) {
        
        // font size
        setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        
        // capitalize the first letter of the sentence
        setKeyListener(new TextKeyListener(Capitalize.SENTENCES, false));

        // needed to make long read only text scroll
        setHorizontallyScrolling(false);
        setSingleLine(false);

        String s = (String) prompt.getAnswerObject();
        if (s != null) {
            setText(s);
        }
        if (prompt.isReadonly()) {
            setBackgroundDrawable(null);
            setFocusable(false);
            setClickable(false);
        }


    }

}
