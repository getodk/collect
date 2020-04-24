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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.formentry.questions.QuestionTextSizeHelper;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.utilities.AnimationUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ViewUtils;
import org.odk.collect.android.widgets.interfaces.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUDIO_QUESTION;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getClipID;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;

public abstract class QuestionWidget
        extends FrameLayout
        implements Widget {

    private final FormEntryPrompt formEntryPrompt;
    private final AudioVideoImageTextLabel audioVideoImageTextLabel;
    private final QuestionDetails questionDetails;
    private final TextView helpTextView;
    private final View helpTextLayout;
    private final View guidanceTextLayout;
    private final View textLayout;
    private final TextView warningText;
    private PermissionUtils permissionUtils;
    private static final String GUIDANCE_EXPANDED_STATE = "expanded_state";
    private AtomicBoolean expanded;
    private Bundle state;
    protected final ThemeUtils themeUtils;
    protected final AudioHelper audioHelper;
    private final ViewGroup containerView;
    private final QuestionTextSizeHelper questionTextSizeHelper = new QuestionTextSizeHelper();

    private WidgetValueChangedListener valueChangedListener;

    @Inject
    public ReferenceManager referenceManager;

    @Inject
    public AudioHelperFactory audioHelperFactory;

    @Inject
    public Analytics analytics;

    public QuestionWidget(Context context, QuestionDetails questionDetails) {
        this(context, questionDetails, true);
    }

    public QuestionWidget(Context context, QuestionDetails questionDetails, boolean registerForContextMenu) {
        super(context);
        getComponent(context).inject(this);
        setId(View.generateViewId());
        this.audioHelper = audioHelperFactory.create(context);

        themeUtils = new ThemeUtils(context);

        if (context instanceof FormEntryActivity) {
            state = ((FormEntryActivity) context).getState();
            permissionUtils = new PermissionUtils();
        }

        this.questionDetails = questionDetails;
        formEntryPrompt = questionDetails.getPrompt();

        containerView = inflate(context, getLayout(), this).findViewById(R.id.question_widget_container);

        audioVideoImageTextLabel = containerView.findViewById(R.id.question_label);
        setupQuestionLabel(audioVideoImageTextLabel, formEntryPrompt);

        helpTextLayout = findViewById(R.id.help_text);
        guidanceTextLayout = helpTextLayout.findViewById(R.id.guidance_text_layout);
        textLayout = helpTextLayout.findViewById(R.id.text_layout);
        warningText = helpTextLayout.findViewById(R.id.warning_text);
        helpTextView = setupHelpText(helpTextLayout.findViewById(R.id.help_text_view), formEntryPrompt);
        setupGuidanceTextAndLayout(helpTextLayout.findViewById(R.id.guidance_text_view), formEntryPrompt);

        View answerView = onCreateAnswerView(context, getFormEntryPrompt(), getAnswerFontSize());
        if (answerView != null) {
            addAnswerView(answerView);
        }

        if (registerForContextMenu && context instanceof FormEntryActivity && !getFormEntryPrompt().isReadOnly()) {
            registerToClearAnswerOnLongPress((FormEntryActivity) context, this);
        }
    }

    /**
     * Returns the `View` object that represents the interface for answering the question. This
     * will be rendered underneath the question's `label`, `hint` and `guidance_hint`. This method
     * is passed the question itself (as a `FormEntryPrompt`) which will often be needed in
     * rendering the widget. It is also passed the size to be used for question text.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        return null;
    }

    /**
     * Used to make sure clickable views in the widget work with the long click feature (shows
     * the "Edit Prompt" menu). The passed listener should be set as the long click listener on
     * clickable views in the widget.
     */
    public abstract void setOnLongClickListener(OnLongClickListener l);

    protected int getLayout() {
        return R.layout.question_widget;
    }

    private void setupQuestionLabel(AudioVideoImageTextLabel label, FormEntryPrompt prompt) {
        label.setTag(getClipID(prompt));
        label.setText(prompt.getLongText(), prompt.isRequired(), questionTextSizeHelper.getHeadline6());

        String imageURI = this instanceof SelectImageMapWidget ? null : prompt.getImageText();
        String videoURI = prompt.getSpecialFormQuestionText("video");
        String bigImageURI = prompt.getSpecialFormQuestionText("big-image");
        label.setImageVideo(
                imageURI,
                videoURI,
                bigImageURI,
                getReferenceManager()
        );

        String playableAudioURI = getPlayableAudioURI(prompt, referenceManager);
        if (playableAudioURI != null) {
            label.setAudio(playableAudioURI, audioHelper);
            analytics.logEvent(AUDIO_QUESTION, "AudioLabel", questionDetails.getFormAnalyticsID());
        }

        label.setPlayTextColor(getPlayColor(formEntryPrompt, themeUtils));
    }

    private TextView setupGuidanceTextAndLayout(TextView guidanceTextView, FormEntryPrompt prompt) {
        TextView guidance;
        GuidanceHint setting = GuidanceHint.get((String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_GUIDANCE_HINT));

        if (setting.equals(GuidanceHint.No)) {
            return null;
        }

        String guidanceHint = prompt.getSpecialFormQuestionText(prompt.getQuestion().getHelpTextID(), "guidance");

        if (android.text.TextUtils.isEmpty(guidanceHint)) {
            return null;
        }

        guidance = configureGuidanceTextView(guidanceTextView, guidanceHint);

        expanded = new AtomicBoolean(false);

        if (getState() != null) {
            if (getState().containsKey(GUIDANCE_EXPANDED_STATE + getFormEntryPrompt().getIndex())) {
                Boolean result = getState().getBoolean(GUIDANCE_EXPANDED_STATE + getFormEntryPrompt().getIndex());
                expanded = new AtomicBoolean(result);
            }
        }

        if (setting.equals(GuidanceHint.Yes)) {
            guidanceTextLayout.setVisibility(VISIBLE);
            guidanceTextView.setText(guidanceHint);
        } else if (setting.equals(GuidanceHint.YesCollapsed)) {
            guidanceTextLayout.setVisibility(expanded.get() ? VISIBLE : GONE);

            View icon = textLayout.findViewById(R.id.help_icon);
            icon.setVisibility(VISIBLE);

            /**
             * Added click listeners to the individual views because the TextView
             * intercepts click events when they are being passed to the parent layout.
             */
            icon.setOnClickListener(v -> {
                if (!expanded.get()) {
                    AnimationUtils.expand(guidanceTextLayout, result -> expanded.set(true));
                } else {
                    AnimationUtils.collapse(guidanceTextLayout, result -> expanded.set(false));
                }
            });

            getHelpTextView().setOnClickListener(v -> {
                if (!expanded.get()) {
                    AnimationUtils.expand(guidanceTextLayout, result -> expanded.set(true));
                } else {
                    AnimationUtils.collapse(guidanceTextLayout, result -> expanded.set(false));
                }
            });
        }

        return guidance;
    }

    private TextView configureGuidanceTextView(TextView guidanceTextView, String guidance) {
        guidanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, questionTextSizeHelper.getSubtitle1());
        guidanceTextView.setHorizontallyScrolling(false);

        guidanceTextView.setText(StringUtils.textToHtml(guidance));

        guidanceTextView.setMovementMethod(LinkMovementMethod.getInstance());
        return guidanceTextView;
    }

    //source::https://stackoverflow.com/questions/18996183/identifying-rtl-language-in-android/23203698#23203698
    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }

    private static boolean isRTL(Locale locale) {
        if (locale.getDisplayName().isEmpty()) {
            return false;
        }
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public TextView getHelpTextView() {
        return helpTextView;
    }

    public FormEntryPrompt getFormEntryPrompt() {
        return formEntryPrompt;
    }

    public QuestionDetails getQuestionDetails() {
        return questionDetails;
    }

    // http://code.google.com/p/android/issues/detail?id=8488
    private void recycleDrawablesRecursive(ViewGroup viewGroup, List<ImageView> images) {

        int childCount = viewGroup.getChildCount();
        for (int index = 0; index < childCount; index++) {
            View child = viewGroup.getChildAt(index);
            if (child instanceof ImageView) {
                images.add((ImageView) child);
            } else if (child instanceof ViewGroup) {
                recycleDrawablesRecursive((ViewGroup) child, images);
            }
        }
        viewGroup.destroyDrawingCache();
    }

    // http://code.google.com/p/android/issues/detail?id=8488
    public void recycleDrawables() {
        List<ImageView> images = new ArrayList<>();
        // collect all the image views
        recycleDrawablesRecursive(this, images);
        for (ImageView imageView : images) {
            imageView.destroyDrawingCache();
            Drawable d = imageView.getDrawable();
            if (d != null && d instanceof BitmapDrawable) {
                imageView.setImageDrawable(null);
                BitmapDrawable bd = (BitmapDrawable) d;
                Bitmap bmp = bd.getBitmap();
                if (bmp != null) {
                    bmp.recycle();
                }
            }
        }
    }

    public void setFocus(Context context) {
        SoftKeyboardUtils.hideSoftKeyboard(this);
    }

    /**
     * Override this to implement fling gesture suppression (e.g. for embedded WebView treatments).
     *
     * @return true if the fling gesture should be suppressed
     */
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
        return false;
    }

    @Deprecated
    protected void addQuestionLabel(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as questionMediaLayout");
            return;
        }
        // default for questionmedialayout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        containerView.addView(v, params);
    }

    public Bundle getState() {
        return state;
    }

    public Bundle getCurrentState() {
        saveState();
        return state;
    }

    @OverridingMethodsMustInvokeSuper
    protected void saveState() {
        state = new Bundle();

        if (expanded != null) {
            state.putBoolean(GUIDANCE_EXPANDED_STATE + getFormEntryPrompt().getIndex(), expanded.get());
        }
    }

    private TextView setupHelpText(TextView helpText, FormEntryPrompt prompt) {
        String s = prompt.getHelpText();

        if (s != null && !s.equals("")) {
            helpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, questionTextSizeHelper.getSubtitle1());
            // wrap to the widget of vi
            helpText.setHorizontallyScrolling(false);
            if (prompt.getLongText() == null || prompt.getLongText().isEmpty()) {
                helpText.setText(StringUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(s, prompt.isRequired())));
            } else {
                helpText.setText(StringUtils.textToHtml(s));
            }
            helpText.setMovementMethod(LinkMovementMethod.getInstance());
            return helpText;
        } else {
            helpText.setVisibility(View.GONE);
            return helpText;
        }
    }

    @Deprecated
    protected final void addAnswerView(View v) {
        addAnswerView(v, null);
    }

    /**
     * Widget should use {@link #onCreateAnswerView} to define answer view
     */
    @Deprecated
    protected final void addAnswerView(View v, Integer margin) {
        ViewGroup answerContainer = findViewById(R.id.answer_container);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        if (margin != null) {
            params.setMargins(ViewUtils.pxFromDp(getContext(), margin), 0, ViewUtils.pxFromDp(getContext(), margin), 0);
        }

        answerContainer.addView(v, params);
    }

    /**
     * Register this widget's child views to pop up a context menu to clear the widget when the
     * user long presses on it. Widget subclasses may override this if some or all of their
     * components need to intercept long presses.
     */
    protected void registerToClearAnswerOnLongPress(FormEntryActivity activity, ViewGroup viewGroup) {
        activity.registerForContextMenu(this);
    }

    /**
     * Every subclassed widget should override this, adding any views they may contain, and calling
     * super.cancelLongPress()
     */
    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        if (getAudioVideoImageTextLabel() != null) {
            getAudioVideoImageTextLabel().cancelLongPress();
        }
        if (getHelpTextView() != null) {
            getHelpTextView().cancelLongPress();
        }
    }

    public void showWarning(String warningBody) {
        warningText.setVisibility(View.VISIBLE);
        warningText.setText(warningBody);
    }

    //region Data waiting

    @Override
    public final void waitForData() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(getFormEntryPrompt().getIndex());
    }

    @Override
    public final void cancelWaitingForData() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }

    @Override
    public final boolean isWaitingForData() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return false;
        }

        FormIndex index = getFormEntryPrompt().getIndex();
        return index.equals(formController.getIndexWaitingForData());
    }

    //region Accessors

    @Nullable
    public final String getInstanceFolder() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return null;
        }

        return formController.getInstanceFile().getParent();
    }

    public int getAnswerFontSize() {
        return (int) questionTextSizeHelper.getHeadline6();
    }

    public View getHelpTextLayout() {
        return helpTextLayout;
    }

    public AudioVideoImageTextLabel getAudioVideoImageTextLabel() {
        return audioVideoImageTextLabel;
    }

    public AudioHelper getAudioHelper() {
        return audioHelper;
    }

    public ReferenceManager getReferenceManager() {
        return referenceManager;
    }

    public PermissionUtils getPermissionUtils() {
        return permissionUtils;
    }

    public void setPermissionUtils(PermissionUtils permissionUtils) {
        this.permissionUtils = permissionUtils;
    }

    public void setValueChangedListener(WidgetValueChangedListener valueChangedListener) {
        this.valueChangedListener = valueChangedListener;
    }

    public void widgetValueChanged() {
        if (valueChangedListener != null) {
            valueChangedListener.widgetValueChanged(this);
        }
    }
}
