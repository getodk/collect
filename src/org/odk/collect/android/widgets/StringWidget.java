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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.AbstractFolioView;

import android.R;
import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * The most basic widget that allows for entry of any text.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class StringWidget extends AbstractQuestionWidget {
    
    protected EditText mStringAnswer;

    protected StringWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
    }

    /**
     * Override this as needed for derived classes
     * 
     * @return the prompt's Answer value as a string
     */
    protected String accessPromptAnswerAsString() {
    	return prompt.getAnswerText();
    }

    @Override
	public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

	/**
     * Common widget-building code.  This is pulled out because some variables, 
     * such as mReadOnly, are initialized here.  Derived classes must call this
     * from within their buildView(...) method.
     * 
     * @param listener
     * @param initialValue
     */
    protected void commonBuildView(int inputType, InputFilter[] filters) {

    	mStringAnswer = new EditText(getContext(), null, R.attr.editTextStyle);
    	// monitor focus change events...
    	mStringAnswer.setOnFocusChangeListener(this);
        // font size
    	mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PX, AbstractFolioView.APPLICATION_FONTSIZE);

        // needed to make long read only text scroll
    	mStringAnswer.setHorizontallyScrolling(false);
    	mStringAnswer.setSingleLine(false);

    	mStringAnswer.setInputType(inputType);
    	if ( filters != null ) {
    		mStringAnswer.setFilters(filters);
    	}
    	
    	addView(mStringAnswer);
    }

    @Override
    protected void buildViewBodyImpl() {

    	// restrict field to text with sentence capitalization...
    	commonBuildView(InputType.TYPE_CLASS_TEXT |
	 			   InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, null);
    }

    protected void updateViewAfterAnswer() {
		String s = accessPromptAnswerAsString();
		mStringAnswer.setText(s);
    }

    @Override
	public void setFocus(Context context) {
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // Put focus on text input field and display soft keyboard if appropriate.
        if (!prompt.isReadOnly()) {
        	mStringAnswer.requestFocus();
            inputManager.showSoftInput(mStringAnswer, 0);
        }
        else {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    public void setEnabled(boolean isEnabled) {
    	mStringAnswer.setEnabled(isEnabled && !prompt.isReadOnly());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isAltPressed() == true) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
