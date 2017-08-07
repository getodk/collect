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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.external.ExternalAppsUtils;

import java.util.Map;

import timber.log.Timber;

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
 */
public class ExStringWidget extends QuestionWidget implements IBinaryWidget {


    private boolean hasExApp = true;
    private Button launchIntentButton;
    private Drawable textBackground;

    protected EditText answer;

    public ExStringWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        // set text formatting
        answer = new EditText(context);
        answer.setId(QuestionWidget.newUniqueId());
        answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        answer.setLayoutParams(params);
        textBackground = answer.getBackground();
        answer.setBackground(null);
        answer.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));

        // capitalize nothing
        answer.setKeyListener(new TextKeyListener(Capitalize.NONE, false));

        // needed to make long read only text scroll
        answer.setHorizontallyScrolling(false);
        answer.setSingleLine(false);

        String s = prompt.getAnswerText();
        if (s != null) {
            answer.setText(s);
        }

        if (formEntryPrompt.isReadOnly() || hasExApp) {
            answer.setFocusable(false);
            answer.setEnabled(false);
        }

        String exSpec = prompt.getAppearanceHint().replaceFirst("^ex[:]", "");
        final String intentName = ExternalAppsUtils.extractIntentName(exSpec);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(exSpec);
        final String buttonText;
        final String errorString;
        String v = formEntryPrompt.getSpecialFormQuestionText("buttonText");
        buttonText = (v != null) ? v : context.getString(R.string.launch_app);
        v = formEntryPrompt.getSpecialFormQuestionText("noAppErrorString");
        errorString = (v != null) ? v : context.getString(R.string.no_app);

        // set button formatting
        launchIntentButton = new Button(getContext());
        launchIntentButton.setId(QuestionWidget.newUniqueId());
        launchIntentButton.setText(buttonText);
        launchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        launchIntentButton.setPadding(20, 20, 20, 20);
        launchIntentButton.setEnabled(!formEntryPrompt.isReadOnly());
        launchIntentButton.setLayoutParams(params);

        launchIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(intentName);
                if (isActivityAvailable(i)) {
                    try {
                        ExternalAppsUtils.populateParameters(i, exParams,
                                formEntryPrompt.getIndex().getReference());

                        Collect.getInstance().getFormController().setIndexWaitingForData(
                                formEntryPrompt.getIndex());
                        fireActivity(i);
                    } catch (ExternalParamsException e) {
                        Timber.e(e);
                        onException(e.getMessage());
                    }
                } else {
                    onException(errorString);
                }
            }

            private void onException(String toastText) {
                hasExApp = false;
                if (!formEntryPrompt.isReadOnly()) {
                    answer.setBackground(textBackground);
                    answer.setFocusable(true);
                    answer.setFocusableInTouchMode(true);
                    answer.setEnabled(true);
                }
                launchIntentButton.setEnabled(false);
                launchIntentButton.setFocusable(false);
                Collect.getInstance().getFormController().setIndexWaitingForData(null);
                Toast.makeText(getContext(),
                        toastText, Toast.LENGTH_SHORT)
                        .show();
                ExStringWidget.this.answer.requestFocus();
                Timber.e(toastText);
            }
        });

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(launchIntentButton);
        answerLayout.addView(answer);
        addAnswerView(answerLayout);
    }

    protected void fireActivity(Intent i) throws ActivityNotFoundException {
        i.putExtra("value", formEntryPrompt.getAnswerText());
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchIntent",
                i.getAction(), formEntryPrompt.getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                FormEntryActivity.EX_STRING_CAPTURE);
    }

    @Override
    public void clearAnswer() {
        answer.setText(null);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = answer.getText().toString();
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
        this.answer.setText(stringData == null ? null : stringData.getValue().toString());
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setFocus(Context context) {
        // Put focus on text input field and display soft keyboard if appropriate.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasExApp) {
            // hide keyboard
            inputManager.hideSoftInputFromWindow(answer.getWindowToken(), 0);
            // focus on launch button
            launchIntentButton.requestFocus();
        } else {
            if (!formEntryPrompt.isReadOnly()) {
                answer.requestFocus();
                inputManager.showSoftInput(answer, 0);
            /*
             * If you do a multi-question screen after a "add another group" dialog, this won't
             * automatically pop up. It's an Android issue.
             *
             * That is, if I have an edit text in an activity, and pop a dialog, and in that
             * dialog's button's OnClick() I call edittext.requestFocus() and
             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the
             * edittext
             * is focused before the dialog pops up, everything works fine. great.
             */
            } else {
                inputManager.hideSoftInputFromWindow(answer.getWindowToken(), 0);
            }
        }
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController().getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        answer.setOnLongClickListener(l);
        launchIntentButton.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        answer.cancelLongPress();
        launchIntentButton.cancelLongPress();
    }

    private boolean isActivityAvailable(Intent intent) {
        return getContext()
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }
}
