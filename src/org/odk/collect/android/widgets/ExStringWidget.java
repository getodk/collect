/*
 * Copyright (C) 2012 University of Washington
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

import java.util.Map;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.external.ExternalAppsUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;


/**
 * <p>Launch an external app to supply a string value. If the app
 * does not launch, enable the text area for regular data entry.</p>
 *
 * <p>The default button text is "Launch"
 *
 * <p>You may override the button text and the error text that is
 * displayed when the app is missing by using jr:itext() values.
 *
 * <p>To use this widget, define an appearance on the &lt;input/&gt;
 * tag that begins "ex:" and then contains the intent action to lauch.
 *
 * <p>e.g.,
 *
 * <pre>
 * &lt;input appearance="ex:change.uw.android.TEXTANSWER" ref="/form/passPhrase" &gt;
 * </pre>
 * <p>or, to customize the button text and error strings with itext:
 * <pre>
 *      ...
 *      &lt;bind nodeset="/form/passPhrase" type="string" /&gt;
 *      ...
 *      &lt;itext&gt;
 *        &lt;translation lang="English"&gt;
 *          &lt;text id="textAnswer"&gt;
 *            &lt;value form="short"&gt;Text question&lt;/value&gt;
 *            &lt;value form="long"&gt;Enter your pass phrase&lt;/value&gt;
 *            &lt;value form="buttonText"&gt;Get Pass Phrase&lt;/value&gt;
 *            &lt;value form="noAppErrorString"&gt;Pass Phrase Tool is not installed!
 *             Please proceed to manually enter pass phrase.&lt;/value&gt;
 *          &lt;/text&gt;
 *        &lt;/translation&gt;
 *      &lt;/itext&gt;
 *    ...
 *    &lt;input appearance="ex:change.uw.android.TEXTANSWER" ref="/form/passPhrase"&gt;
 *      &lt;label ref="jr:itext('textAnswer')"/&gt;
 *    &lt;/input&gt;
 * </pre>
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ExStringWidget extends QuestionWidget implements IBinaryWidget {

    private final String t = getClass().getName();

    private boolean mHasExApp = true;
    private Button mLaunchIntentButton;
    private Drawable mTextBackground;

    protected EditText mAnswer;

    public ExStringWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        // set text formatting
        mAnswer = new EditText(context);
        mAnswer.setId(QuestionWidget.newUniqueId());
        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setLayoutParams(params);
        mTextBackground = mAnswer.getBackground();
        mAnswer.setBackgroundDrawable(null);

        // capitalize nothing
        mAnswer.setKeyListener(new TextKeyListener(Capitalize.NONE, false));

        // needed to make long read only text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        String s = prompt.getAnswerText();
        if (s != null) {
            mAnswer.setText(s);
        }

        if (mPrompt.isReadOnly()) {
        	mAnswer.setBackgroundDrawable(null);
        }

        if (mPrompt.isReadOnly() || mHasExApp) {
            mAnswer.setFocusable(false);
            mAnswer.setClickable(false);
        }

        String appearance = prompt.getAppearanceHint();
        String[] attrs = appearance.split(":");
        final String intentName = ExternalAppsUtils.extractIntentName(attrs[1]);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(attrs[1]);
        final String buttonText;
        final String errorString;
    	String v = mPrompt.getSpecialFormQuestionText("buttonText");
    	buttonText = (v != null) ? v : context.getString(R.string.launch_app);
    	v = mPrompt.getSpecialFormQuestionText("noAppErrorString");
    	errorString = (v != null) ? v : context.getString(R.string.no_app);

        // set button formatting
        mLaunchIntentButton = new Button(getContext());
        mLaunchIntentButton.setId(QuestionWidget.newUniqueId());
        mLaunchIntentButton.setText(buttonText);
        mLaunchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mLaunchIntentButton.setPadding(20, 20, 20, 20);
        mLaunchIntentButton.setEnabled(!mPrompt.isReadOnly());
        mLaunchIntentButton.setLayoutParams(params);

        mLaunchIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(intentName);
                try {
                    ExternalAppsUtils.populateParameters(i, exParams, mPrompt.getIndex().getReference());

                    Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
                	fireActivity(i);
                } catch (ExternalParamsException e) {
                    Log.e(t, e.getMessage(), e);
                    onException(e.getMessage());
                } catch (ActivityNotFoundException e) {
                    Log.e(t, e.getMessage(), e);
                    onException(errorString);
                }
            }

            private void onException(String toastText) {
                mHasExApp = false;
                if ( !mPrompt.isReadOnly() ) {
                    mAnswer.setBackgroundDrawable(mTextBackground);
                    mAnswer.setFocusable(true);
                    mAnswer.setFocusableInTouchMode(true);
                    mAnswer.setClickable(true);
                }
                mLaunchIntentButton.setEnabled(false);
                mLaunchIntentButton.setFocusable(false);
                Collect.getInstance().getFormController().setIndexWaitingForData(null);
                Toast.makeText(getContext(),
                        toastText, Toast.LENGTH_SHORT)
                        .show();
                ExStringWidget.this.mAnswer.requestFocus();
            }
        });

        // finish complex layout
        addView(mLaunchIntentButton);
        addView(mAnswer);
    }

    protected void fireActivity(Intent i) throws ActivityNotFoundException {
    	i.putExtra("value", mPrompt.getAnswerText());
       	Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchIntent",
    			i.getAction(), mPrompt.getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                FormEntryActivity.EX_STRING_CAPTURE);
    }

    @Override
    public void clearAnswer() {
    	mAnswer.setText(null);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = mAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    /**
     * Allows answer to be set externally in {@Link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        StringData stringData = ExternalAppsUtils.asStringData(answer);
        mAnswer.setText(stringData == null ? null : stringData.getValue().toString());
    	Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setFocus(Context context) {
        // Put focus on text input field and display soft keyboard if appropriate.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	if ( mHasExApp ) {
    		// hide keyboard
            inputManager.hideSoftInputFromWindow(mAnswer.getWindowToken(), 0);
            // focus on launch button
            mLaunchIntentButton.requestFocus();
    	} else {
            if (!mPrompt.isReadOnly()) {
	            mAnswer.requestFocus();
	            inputManager.showSoftInput(mAnswer, 0);
	            /*
	             * If you do a multi-question screen after a "add another group" dialog, this won't
	             * automatically pop up. It's an Android issue.
	             *
	             * That is, if I have an edit text in an activity, and pop a dialog, and in that
	             * dialog's button's OnClick() I call edittext.requestFocus() and
	             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the edittext
	             * is focused before the dialog pops up, everything works fine. great.
	             */
	        } else {
	            inputManager.hideSoftInputFromWindow(mAnswer.getWindowToken(), 0);
	        }
    	}
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return mPrompt.getIndex().equals(Collect.getInstance().getFormController().getIndexWaitingForData());
    }

	@Override
	public void cancelWaitingForBinaryData() {
    	Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isAltPressed() == true) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    	mAnswer.setOnLongClickListener(l);
        mLaunchIntentButton.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mAnswer.cancelLongPress();
        mLaunchIntentButton.cancelLongPress();
    }


}
