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

package org.odk.collect.android.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.external.ExternalAppsUtils;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.WidgetFactory;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * This class is
 *
 * @author carlhartung
 */
@SuppressLint("ViewConstructor")
public class ODKView extends ScrollView implements OnLongClickListener {

    private final LinearLayout view;
    private final LinearLayout.LayoutParams layout;
    private final ArrayList<QuestionWidget> widgets;

    public static final String FIELD_LIST = "field-list";

    public ODKView(Context context, final FormEntryPrompt[] questionPrompts,
            FormEntryCaption[] groups, boolean advancingPage) {
        super(context);

        widgets = new ArrayList<>();

        view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.TOP);
        view.setPadding(0, 7, 0, 0);

        layout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setMargins(10, 0, 10, 0);

        // display which group you are in as well as the question

        addGroupText(groups);

        // when the grouped fields are populated by an external app, this will get true.
        boolean readOnlyOverride = false;

        // get the group we are showing -- it will be the last of the groups in the groups list
        if (groups != null && groups.length > 0) {
            final FormEntryCaption c = groups[groups.length - 1];
            final String intentString = c.getFormElement().getAdditionalAttribute(null, "intent");
            if (intentString != null && intentString.length() != 0) {

                readOnlyOverride = true;

                final String buttonText;
                final String errorString;
                String v = c.getSpecialFormQuestionText("buttonText");
                buttonText = (v != null) ? v : context.getString(R.string.launch_app);
                v = c.getSpecialFormQuestionText("noAppErrorString");
                errorString = (v != null) ? v : context.getString(R.string.no_app);

                TableLayout.LayoutParams params = new TableLayout.LayoutParams();
                params.setMargins(7, 5, 7, 5);

                // set button formatting
                Button launchIntentButton = new Button(getContext());
                launchIntentButton.setId(ViewIds.generateViewId());
                launchIntentButton.setText(buttonText);
                launchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                        Collect.getQuestionFontsize() + 2);
                launchIntentButton.setPadding(20, 20, 20, 20);
                launchIntentButton.setLayoutParams(params);

                launchIntentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String intentName = ExternalAppsUtils.extractIntentName(intentString);
                        Map<String, String> parameters = ExternalAppsUtils.extractParameters(
                                intentString);

                        Intent i = new Intent(intentName);
                        try {
                            ExternalAppsUtils.populateParameters(i, parameters,
                                    c.getIndex().getReference());

                            for (FormEntryPrompt p : questionPrompts) {
                                IFormElement formElement = p.getFormElement();
                                if (formElement instanceof QuestionDef) {
                                    TreeReference reference =
                                            (TreeReference) formElement.getBind().getReference();
                                    IAnswerData answerValue = p.getAnswerValue();
                                    Object value =
                                            answerValue == null ? null : answerValue.getValue();
                                    switch (p.getDataType()) {
                                        case Constants.DATATYPE_TEXT:
                                        case Constants.DATATYPE_INTEGER:
                                        case Constants.DATATYPE_DECIMAL:
                                            i.putExtra(reference.getNameLast(),
                                                    (Serializable) value);
                                            break;
                                    }
                                }
                            }

                            ((Activity) getContext()).startActivityForResult(i, RequestCodes.EX_GROUP_CAPTURE);
                        } catch (ExternalParamsException e) {
                            Timber.e(e, "ExternalParamsException");

                            ToastUtils.showShortToast(e.getMessage());
                        } catch (ActivityNotFoundException e) {
                            Timber.d(e, "ActivityNotFoundExcept");

                            ToastUtils.showShortToast(errorString);
                        }
                    }
                });

                View divider = new View(getContext());
                divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
                divider.setMinimumHeight(3);
                view.addView(divider);

                view.addView(launchIntentButton, layout);
            }
        }

        boolean first = true;
        for (FormEntryPrompt p : questionPrompts) {
            if (!first) {
                View divider = new View(getContext());
                divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
                divider.setMinimumHeight(3);
                view.addView(divider);
            } else {
                first = false;
            }

            // if question or answer type is not supported, use text widget
            QuestionWidget qw =
                    WidgetFactory.createWidgetFromPrompt(p, getContext(), readOnlyOverride);
            qw.setLongClickable(true);
            qw.setOnLongClickListener(this);
            qw.setId(ViewIds.generateViewId());

            widgets.add(qw);
            view.addView(qw, layout);
        }

        addView(view);

        // see if there is an autoplay option.
        // Only execute it during forward swipes through the form
        if (advancingPage && widgets.size() == 1) {
            final String playOption = widgets.get(
                    0).getFormEntryPrompt().getFormElement().getAdditionalAttribute(null, "autoplay");
            if (playOption != null) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (playOption.equalsIgnoreCase("audio")) {
                            widgets.get(0).playAudio();
                        } else if (playOption.equalsIgnoreCase("video")) {
                            widgets.get(0).playVideo();
                        }
                    }
                }, 150);
            }
        }
    }

    public Bundle getState() {
        Bundle state = new Bundle();
        for (QuestionWidget qw : getWidgets()) {
            state.putAll(qw.getCurrentState());
        }

        return state;
    }

    /**
     * http://code.google.com/p/android/issues/detail?id=8488
     */
    public void recycleDrawables() {
        this.destroyDrawingCache();
        view.destroyDrawingCache();
        for (QuestionWidget q : widgets) {
            q.recycleDrawables();
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Collect.getInstance().getActivityLogger().logScrollAction(this, t - oldt);
    }

    /**
     * @return a HashMap of answers entered by the user for this set of widgets
     */
    public HashMap<FormIndex, IAnswerData> getAnswers() {
        HashMap<FormIndex, IAnswerData> answers = new LinkedHashMap<>();
        for (QuestionWidget q : widgets) {
            /*
             * The FormEntryPrompt has the FormIndex, which is where the answer gets stored. The
             * QuestionWidget has the answer the user has entered.
             */
            FormEntryPrompt p = q.getFormEntryPrompt();
            answers.put(p.getIndex(), q.getAnswer());
        }

        return answers;
    }


    /**
     * // * Add a TextView containing the hierarchy of groups to which the question belongs. //
     */
    private void addGroupText(FormEntryCaption[] groups) {
        String path = getGroupsPath(groups);

        // build view
        if (!path.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText(path);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize() - 4);
            tv.setPadding(0, 0, 0, 5);
            view.addView(tv, layout);
        }
    }

    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups) {
        StringBuilder path = new StringBuilder("");
        if (groups != null) {
            String longText;
            int multiplicity;
            int index = 1;
            // list all groups in one string
            for (FormEntryCaption group : groups) {
                multiplicity = group.getMultiplicity() + 1;
                longText = group.getLongText();
                if (longText != null) {
                    path.append(longText);
                    if (group.repeats() && multiplicity > 0) {
                        path
                                .append(" (")
                                .append(multiplicity)
                                .append(")\u200E");
                    }
                    if (index < groups.length) {
                        path.append(" > ");
                    }
                    index++;
                }
            }
        }

        return path.toString();
    }

    public void setFocus(Context context) {
        if (!widgets.isEmpty()) {
            widgets.get(0).setFocus(context);
        }
    }


    /**
     * Called when another activity returns information to answer this question.
     */
    public void setBinaryData(Object answer) {
        boolean set = false;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                BinaryWidget binaryWidget = (BinaryWidget) q;
                if (binaryWidget.isWaitingForData()) {
                    try {
                        binaryWidget.setBinaryData(answer);
                        binaryWidget.cancelWaitingForData();
                    } catch (Exception e) {
                        Timber.e(e);
                        ToastUtils.showLongToast(getContext().getString(R.string.error_attaching_binary_file,
                                        e.getMessage()));
                    }
                    set = true;
                    break;
                }
            }
        }

        if (!set) {
            Timber.w("Attempting to return data to a widget or set of widgets not looking for data");
        }
    }

    public void setDataForFields(Bundle bundle) throws JavaRosaException {
        if (bundle == null) {
            return;
        }
        FormController formController = Collect.getInstance().getFormController();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            for (QuestionWidget questionWidget : widgets) {
                FormEntryPrompt prompt = questionWidget.getFormEntryPrompt();
                TreeReference treeReference =
                        (TreeReference) prompt.getFormElement().getBind().getReference();

                if (treeReference.getNameLast().equals(key)) {

                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_TEXT:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asStringData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_INTEGER:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asIntegerData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asDecimalData(bundle.get(key)));
                            break;
                        default:
                            throw new RuntimeException(
                                    getContext().getString(R.string.ext_assign_value_error,
                                            treeReference.toString(false)));
                    }

                    break;
                }
            }
        }
    }

    public void cancelWaitingForBinaryData() {
        int count = 0;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                if (q.isWaitingForData()) {
                    q.cancelWaitingForData();
                    ++count;
                }
            }
        }

        if (count != 1) {
            Timber.w("Attempting to cancel waiting for binary data to a widget or set of widgets "
                            + "not looking for data");
        }
    }

    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        for (QuestionWidget q : widgets) {
            if (q.suppressFlingGesture(e1, e2, velocityX, velocityY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the answer was cleared, false otherwise.
     */
    public boolean clearAnswer() {
        // If there's only one widget, clear the answer.
        // If there are more, then force a long-press to clear the answer.
        if (widgets.size() == 1 && !widgets.get(0).getFormEntryPrompt().isReadOnly()) {
            widgets.get(0).clearAnswer();
            return true;
        } else {
            return false;
        }
    }


    public ArrayList<QuestionWidget> getWidgets() {
        return widgets;
    }


    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        for (int i = 0; i < widgets.size(); i++) {
            QuestionWidget qw = widgets.get(i);
            qw.setOnFocusChangeListener(l);
        }
    }


    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (QuestionWidget qw : widgets) {
            qw.cancelLongPress();
        }
    }

    public void stopAudio() {
        widgets.get(0).stopAudio();
    }

    /**
     * Releases widget resources, such as {@link android.media.MediaPlayer}s
     */
    public void releaseWidgetResources() {
        for (QuestionWidget w : widgets) {
            w.release();
        }
    }

    public void highlightWidget(FormIndex formIndex) {
        QuestionWidget qw = getQuestionWidget(formIndex);

        if (qw != null) {
            // postDelayed is needed because otherwise scrolling may not work as expected in case when
            // answers are validated during form finalization.
            new Handler().postDelayed(() -> {
                scrollTo(0, qw.getTop());

                ValueAnimator va = new ValueAnimator();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    va.setIntValues(getResources().getColor(R.color.red), getDrawingCacheBackgroundColor());
                } else {
                    // Avoid fading to black on certain devices and Android versions that may not support transparency
                    TypedValue typedValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                    if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        va.setIntValues(getResources().getColor(R.color.red), typedValue.data);
                    } else {
                        va.setIntValues(getResources().getColor(R.color.red), getDrawingCacheBackgroundColor());
                    }
                }

                va.setEvaluator(new ArgbEvaluator());
                va.addUpdateListener(valueAnimator -> qw.setBackgroundColor((int) valueAnimator.getAnimatedValue()));
                va.setDuration(2500);
                va.start();
            }, 100);
        }
    }

    private QuestionWidget getQuestionWidget(FormIndex formIndex) {
        for (QuestionWidget qw : widgets) {
            if (formIndex.equals(qw.getFormEntryPrompt().getIndex())) {
                return qw;
            }
        }
        return null;
    }
}
