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
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.StringWidget;
import org.odk.collect.android.widgets.WidgetFactory;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Contains either one {@link QuestionWidget} if the current form element is a question or
 * multiple {@link QuestionWidget}s if the current form element is a group with the
 * {@code field-list} appearance.
 */
@SuppressLint("ViewConstructor")
public class ODKView extends FrameLayout implements OnLongClickListener, WidgetValueChangedListener {

    private final LinearLayout view;
    private final LinearLayout.LayoutParams layout;
    private final ArrayList<QuestionWidget> widgets;

    public static final String FIELD_LIST = "field-list";

    private WidgetValueChangedListener widgetValueChangedListener;

    /**
     * Builds the view for a specified question or field-list of questions.
     *
     * @param context the activity creating this view
     * @param questionPrompts the questions to be included in this view
     * @param groups the group hierarchy that this question or field list is in
     * @param advancingPage whether this view is being created after a forward swipe through the
     *                      form. Used to determine whether to autoplay media.
     */
    public ODKView(Context context, final FormEntryPrompt[] questionPrompts,
                   FormEntryCaption[] groups, boolean advancingPage) {
        super(context);

        inflate(getContext(), R.layout.nested_scroll_view, this); // keep in an xml file to enable the vertical scrollbar

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

        // handle intent groups that are intended to receive multiple values from an external app
        if (groups != null && groups.length > 0) {
            // get the group we are showing -- it will be the last of the groups in the groups list
            final FormEntryCaption c = groups[groups.length - 1];
            final String intentString = c.getFormElement().getAdditionalAttribute(null, "intent");
            if (intentString != null && intentString.length() != 0) {
                readOnlyOverride = true;

                addIntentLaunchButton(context, questionPrompts, c, intentString);
            }
        }

        for (FormEntryPrompt question : questionPrompts) {
            addWidgetForQuestion(question, readOnlyOverride);
        }

        ((NestedScrollView) findViewById(R.id.odk_view_container)).addView(view);

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

    /**
     * Creates a {@link QuestionWidget} for the given {@link FormEntryPrompt}, sets its listeners,
     * and adds it to the end of the view. If this widget is not the first one, add a divider above
     * it.
     */
    private void addWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride) {
        QuestionWidget qw = configureWidgetForQuestion(question, readOnlyOverride);

        widgets.add(qw);

        if (widgets.size() > 1) {
            view.addView(getDividerView());
        }
        view.addView(qw, layout);
    }

    /**
     * Creates a {@link QuestionWidget} for the given {@link FormEntryPrompt}, sets its listeners,
     * and adds it to the view at the specified {@code index}. If this widget is not the first one,
     * add a divider above it. If the specified {@code index} is beyond the end of the widget list,
     * add it to the end.
     */
    public void addWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride, int index) {
        if (index > widgets.size() - 1) {
            addWidgetForQuestion(question, readOnlyOverride);
            return;
        }

        QuestionWidget qw = configureWidgetForQuestion(question, readOnlyOverride);

        widgets.add(index, qw);

        int indexAccountingForDividers = index * 2;
        // There may be a first TextView to display the group path. See addGroupText(FormEntryCaption[])
        if (view.getChildCount() > 0 && view.getChildAt(0) instanceof TextView) {
            indexAccountingForDividers += 1;
        }

        if (index > 0) {
            view.addView(getDividerView(), indexAccountingForDividers - 1);
        }
        view.addView(qw, indexAccountingForDividers, layout);
    }

    /**
     * Creates and configures a {@link QuestionWidget} for the given {@link FormEntryPrompt}.
     *
     * Note: if the given question is of an unsupported type, a text widget will be created.
     */
    private QuestionWidget configureWidgetForQuestion(FormEntryPrompt question, boolean readOnlyOverride) {
        QuestionWidget qw = WidgetFactory.createWidgetFromPrompt(question, getContext(), readOnlyOverride);
        qw.setOnLongClickListener(this);
        qw.setValueChangedListener(this);
        qw.setId(ViewIds.generateViewId());

        return qw;
    }

    private View getDividerView() {
        View divider = new View(getContext());
        divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
        divider.setMinimumHeight(3);

        return divider;
    }

    public Bundle getState() {
        Bundle state = new Bundle();
        for (QuestionWidget qw : getWidgets()) {
            state.putAll(qw.getCurrentState());
        }

        return state;
    }

    /**
     * Addresses 'bitmap size exceeds VM budget' crash.
     * http://code.google.com/p/android/issues/detail?id=8488
     */
    public void recycleDrawables() {
        this.destroyDrawingCache();
        view.destroyDrawingCache();
        for (QuestionWidget q : widgets) {
            q.recycleDrawables();
        }
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
     * Add a TextView containing the hierarchy of groups to which the question belongs.
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

    /**
     * @see #getGroupsPath(FormEntryCaption[], boolean)
     */
    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups) {
        return getGroupsPath(groups, false);
    }

    /**
     * Builds a string representing the 'path' of the list of groups.
     * Each level is separated by `>`.
     *
     * Some views (e.g. the repeat picker) may want to hide the multiplicity of the last item,
     * i.e. show `Friends` instead of `Friends > 1`.
     */
    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups, boolean hideLastMultiplicity) {
        if (groups == null) {
            return "";
        }

        List<String> segments = new ArrayList<>();
        int index = 1;
        for (FormEntryCaption group : groups) {
            String text = group.getLongText();

            if (text != null) {
                segments.add(text);

                boolean isMultiplicityAllowed = !(hideLastMultiplicity && index == groups.length);
                if (group.repeats() && isMultiplicityAllowed) {
                    segments.add(Integer.toString(group.getMultiplicity() + 1));
                }
            }

            index++;
        }

        return TextUtils.join(" > ", segments);
    }


    /**
     * Adds a button to launch an intent if the group displayed by this view is an intent group.
     * An intent group launches an intent and receives multiple values from the launched app.
     */
    private void addIntentLaunchButton(Context context, FormEntryPrompt[] questionPrompts,
                                       FormEntryCaption c, String intentString) {
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

        launchIntentButton.setOnClickListener(new OnClickListener() {
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

        view.addView(getDividerView());

        view.addView(launchIntentButton, layout);
    }

    public void setFocus(Context context) {
        if (!widgets.isEmpty()) {
            widgets.get(0).setFocus(context);
        }
    }

    /**
     * Returns true if any part of the question widget is currently on the screen or false otherwise.
     */
    public boolean isDisplayed(QuestionWidget qw) {
        Rect scrollBounds = new Rect();
        findViewById(R.id.odk_view_container).getHitRect(scrollBounds);
        return qw.getLocalVisibleRect(scrollBounds);
    }

    public void scrollTo(@Nullable QuestionWidget qw) {
        if (qw != null && widgets.contains(qw)) {
            findViewById(R.id.odk_view_container).scrollTo(0, qw.getTop());
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

    /**
     * Saves answers for the widgets in this view. Called when the widgets are in an intent group.
     */
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

                    ((StringWidget) questionWidget).setDisplayValueFromModel();
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

    /**
     * Highlights the question at the given {@link FormIndex} in red for 2.5 seconds, scrolls the
     * view to display that question at the top and gives it focus.
     */
    public void highlightWidget(FormIndex formIndex) {
        QuestionWidget qw = getQuestionWidget(formIndex);

        if (qw != null) {
            // postDelayed is needed because otherwise scrolling may not work as expected in case when
            // answers are validated during form finalization.
            new Handler().postDelayed(() -> {
                qw.setFocus(getContext());
                scrollTo(qw);

                ValueAnimator va = new ValueAnimator();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    va.setIntValues(getResources().getColor(R.color.red_500), getDrawingCacheBackgroundColor());
                } else {
                    // Avoid fading to black on certain devices and Android versions that may not support transparency
                    TypedValue typedValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
                    if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        va.setIntValues(getResources().getColor(R.color.red_500), typedValue.data);
                    } else {
                        va.setIntValues(getResources().getColor(R.color.red_500), getDrawingCacheBackgroundColor());
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

    /**
     * Removes the widget and corresponding divider at a particular index.
     */
    public void removeWidgetAt(int index) {
        int indexAccountingForDividers = index * 2;

        // There may be a first TextView to display the group path. See addGroupText(FormEntryCaption[])
        if (view.getChildCount() > 0 && view.getChildAt(0) instanceof TextView) {
            indexAccountingForDividers += 1;
        }
        view.removeViewAt(indexAccountingForDividers);

        if (index > 0) {
            view.removeViewAt(indexAccountingForDividers - 1);
        }

        widgets.get(index).release();
        widgets.remove(index);
    }

    public void setWidgetValueChangedListener(WidgetValueChangedListener listener) {
        widgetValueChangedListener = listener;
    }

    public void widgetValueChanged(QuestionWidget changedWidget) {
        if (widgetValueChangedListener != null) {
            widgetValueChangedListener.widgetValueChanged(changedWidget);
        }
    }
}
