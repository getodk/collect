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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.external.ExternalAppsUtils;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.DependencyProvider;
import org.odk.collect.android.utilities.ObjectUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.util.Map;

import timber.log.Timber;

import static android.content.Intent.ACTION_SENDTO;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

@SuppressLint("ViewConstructor")
public class ExStringWidget extends BaseStringWidget implements BinaryWidget {
    // If an extra with this key is specified, it will be parsed as a URI and used as intent data
    private static final String URI_KEY = "uri_data";

    private boolean hasExApp = true;
    private final Button launchIntentButton;
    private final Drawable textBackground;

    private ActivityAvailability activityAvailability;

    public ExStringWidget(Context context, FormEntryPrompt prompt) {

        super(context, prompt);

        textBackground = answerText.getBackground();
        answerText.setBackground(null);
        answerText.setTextColor(themeUtils.getPrimaryTextColor());

        // capitalize nothing
        answerText.setKeyListener(new TextKeyListener(Capitalize.NONE, false));

        String s = prompt.getAnswerText();
        if (s != null) {
            answerText.setText(s);
        }

        if (getFormEntryPrompt().isReadOnly() || hasExApp) {
            answerText.setFocusable(false);
            answerText.setEnabled(false);
        }

        String v = getFormEntryPrompt().getSpecialFormQuestionText("buttonText");
        String buttonText = (v != null) ? v : context.getString(R.string.launch_app);

        launchIntentButton = getSimpleButton(buttonText);

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(launchIntentButton);
        answerLayout.addView(answerText);
        addAnswerView(answerLayout);

        Collect.getInstance().getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("WidgetType")
                        .setAction("ExternalApp")
                        .setLabel(Collect.getCurrentFormIdentifierHash())
                        .build());

    }

    protected void fireActivity(Intent i) throws ActivityNotFoundException {
        i.putExtra("value", getFormEntryPrompt().getAnswerText());
        try {
            ((Activity) getContext()).startActivityForResult(i, RequestCodes.EX_STRING_CAPTURE);
        } catch (SecurityException e) {
            Timber.i(e);
            ToastUtils.showLongToast(R.string.not_granted_permission);
        }
    }

    /**
     * Allows answer to be set externally in {@link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        StringData stringData = ExternalAppsUtils.asStringData(answer);
        answerText.setText(stringData == null ? null : stringData.getValue().toString());
        widgetValueChanged();
    }

    @Override
    public void setFocus(Context context) {
        if (hasExApp) {
            SoftKeyboardUtils.hideSoftKeyboard(answerText);
            // focus on launch button
            launchIntentButton.requestFocus();
        } else {
            if (!getFormEntryPrompt().isReadOnly()) {
                SoftKeyboardUtils.showSoftKeyboard(answerText);
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
                SoftKeyboardUtils.hideSoftKeyboard(answerText);
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        answerText.setOnLongClickListener(l);
        launchIntentButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        answerText.cancelLongPress();
        launchIntentButton.cancelLongPress();
    }

    @Override
    protected void injectDependencies(DependencyProvider dependencyProvider) {
        DependencyProvider<ActivityAvailability> activityUtilProvider =
                ObjectUtils.uncheckedCast(dependencyProvider);

        if (activityUtilProvider == null) {
            Timber.e("DependencyProvider doesn't provide ActivityAvailability.");
            return;
        }

        activityAvailability = activityUtilProvider.provide();
    }

    @Override
    public void onButtonClick(int buttonId) {
        String exSpec = getFormEntryPrompt().getAppearanceHint().replaceFirst("^ex[:]", "");
        final String intentName = ExternalAppsUtils.extractIntentName(exSpec);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(exSpec);
        final String errorString;
        String v = getFormEntryPrompt().getSpecialFormQuestionText("noAppErrorString");
        errorString = (v != null) ? v : getContext().getString(R.string.no_app);

        Intent i = new Intent(intentName);

        // Use special "uri_data" key to set intent data. This must be done before checking if an
        // activity is available to handle implicit intents.
        if (exParams.containsKey(URI_KEY)) {
            try {
                String uriValue = (String) ExternalAppsUtils.getValueRepresentedBy(exParams.get(URI_KEY),
                            getFormEntryPrompt().getIndex().getReference());
                i.setData(Uri.parse(uriValue));
                exParams.remove(URI_KEY);
            } catch (XPathSyntaxException e) {
                Timber.d(e);
                onException(e.getMessage());
            }
        }

        if (activityAvailability.isActivityAvailable(i)) {
            try {
                ExternalAppsUtils.populateParameters(i, exParams, getFormEntryPrompt().getIndex().getReference());

                waitForData();
                // ACTION_SENDTO used for sending text messages or emails doesn't require any results
                if (ACTION_SENDTO.equals(i.getAction())) {
                    getContext().startActivity(i);
                } else {
                    fireActivity(i);
                }
            } catch (ExternalParamsException | ActivityNotFoundException e) {
                Timber.d(e);
                onException(e.getMessage());
            }
        } else {
            onException(errorString);
        }
    }

    private void focusAnswer() {
        SoftKeyboardUtils.showSoftKeyboard(answerText);
    }

    private void onException(String toastText) {
        hasExApp = false;
        if (!getFormEntryPrompt().isReadOnly()) {
            answerText.setBackground(textBackground);
            answerText.setFocusable(true);
            answerText.setFocusableInTouchMode(true);
            answerText.setEnabled(true);
        }
        launchIntentButton.setEnabled(false);
        launchIntentButton.setFocusable(false);
        cancelWaitingForData();

        Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
        Timber.d(toastText);
        focusAnswer();
    }
}
