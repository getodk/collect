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

import static org.odk.collect.android.formentry.media.FormMediaUtils.getClipID;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;

import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.QuestionTextSizeHelper;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.utilities.AnimationUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.SoftKeyboardController;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ViewUtils;
import org.odk.collect.android.widgets.interfaces.Widget;
import org.odk.collect.android.widgets.items.SelectImageMapWidget;
import org.odk.collect.imageloader.ImageLoader;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProjectKeys;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import timber.log.Timber;

public abstract class QuestionWidget extends FrameLayout implements Widget {

    private final FormEntryPrompt formEntryPrompt;
    private final AudioVideoImageTextLabel audioVideoImageTextLabel;
    protected final QuestionDetails questionDetails;
    private final TextView helpTextView;
    private final View helpTextLayout;
    private final View guidanceTextLayout;
    private final View textLayout;
    private final TextView warningText;
    private AtomicBoolean expanded;
    protected final ThemeUtils themeUtils;
    protected AudioHelper audioHelper;
    private final ViewGroup containerView;
    private final QuestionTextSizeHelper questionTextSizeHelper;

    private WidgetValueChangedListener valueChangedListener;

    @Inject
    public ReferenceManager referenceManager;

    @Inject
    public AudioHelperFactory audioHelperFactory;

    @Inject
    public Analytics analytics;

    @Inject
    public ScreenUtils screenUtils;

    @Inject
    public SoftKeyboardController softKeyboardController;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    protected
    MediaUtils mediaUtils;

    @Inject
    ImageLoader imageLoader;

    public QuestionWidget(Context context, QuestionDetails questionDetails) {
        super(context);
        getComponent(context).inject(this);
        setId(View.generateViewId());
        questionTextSizeHelper = new QuestionTextSizeHelper(settingsProvider.getUnprotectedSettings());
        this.audioHelper = audioHelperFactory.create(context);

        themeUtils = new ThemeUtils(context);

        this.questionDetails = questionDetails;
        formEntryPrompt = questionDetails.getPrompt();

        containerView = inflate(context, getLayout(), this).findViewById(R.id.question_widget_container);

        audioVideoImageTextLabel = containerView.findViewById(R.id.question_label);
        setupQuestionLabel();

        helpTextLayout = findViewById(R.id.help_text);
        guidanceTextLayout = helpTextLayout.findViewById(R.id.guidance_text_layout);
        textLayout = helpTextLayout.findViewById(R.id.text_layout);
        warningText = helpTextLayout.findViewById(R.id.warning_text);
        helpTextView = setupHelpText(helpTextLayout.findViewById(R.id.help_text_view), formEntryPrompt);
        setupGuidanceTextAndLayout(helpTextLayout.findViewById(R.id.guidance_text_view), formEntryPrompt);

        View answerView = onCreateAnswerView(context, questionDetails.getPrompt(), getAnswerFontSize());
        if (answerView != null) {
            addAnswerView(answerView);
        }

        if (context instanceof Activity && !questionDetails.isReadOnly()) {
            registerToClearAnswerOnLongPress((Activity) context, this);
        }
        hideAnswerContainerIfNeeded();
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

    private void setupQuestionLabel() {
        audioVideoImageTextLabel.setTag(getClipID(formEntryPrompt));
        audioVideoImageTextLabel.setText(formEntryPrompt.getLongText(), formEntryPrompt.isRequired(), questionTextSizeHelper.getHeadline6());
        audioVideoImageTextLabel.setMediaUtils(mediaUtils);

        String imageURI = this instanceof SelectImageMapWidget ? null : formEntryPrompt.getImageText();
        String videoURI = formEntryPrompt.getSpecialFormQuestionText("video");
        String bigImageURI = formEntryPrompt.getSpecialFormQuestionText("big-image");
        String playableAudioURI = getPlayableAudioURI(formEntryPrompt, referenceManager);
        try {
            if (imageURI != null) {
                audioVideoImageTextLabel.setImage(new File(referenceManager.deriveReference(imageURI).getLocalURI()), imageLoader);
            }
            if (bigImageURI != null) {
                audioVideoImageTextLabel.setBigImage(new File(referenceManager.deriveReference(bigImageURI).getLocalURI()));
            }
            if (videoURI != null) {
                audioVideoImageTextLabel.setVideo(new File(referenceManager.deriveReference(videoURI).getLocalURI()));
            }
            if (playableAudioURI != null) {
                audioVideoImageTextLabel.setAudio(playableAudioURI, audioHelper);
            }
        } catch (InvalidReferenceException e) {
            Timber.d(e, "Invalid media reference due to %s ", e.getMessage());
        }

        audioVideoImageTextLabel.setPlayTextColor(getPlayColor(formEntryPrompt, themeUtils));
    }

    private TextView setupGuidanceTextAndLayout(TextView guidanceTextView, FormEntryPrompt prompt) {
        TextView guidance;
        GuidanceHint setting = GuidanceHint.get(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_GUIDANCE_HINT));

        if (setting.equals(GuidanceHint.NO)) {
            return null;
        }

        String guidanceHint = prompt.getSpecialFormQuestionText(prompt.getQuestion().getHelpTextID(), "guidance");

        if (android.text.TextUtils.isEmpty(guidanceHint)) {
            return null;
        }

        guidance = configureGuidanceTextView(guidanceTextView, guidanceHint);

        expanded = new AtomicBoolean(false);

        if (setting.equals(GuidanceHint.YES)) {
            guidanceTextLayout.setVisibility(VISIBLE);
            guidanceTextView.setText(guidanceHint);
        } else if (setting.equals(GuidanceHint.YES_COLLAPSED)) {
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

        guidanceTextView.setText(HtmlUtils.textToHtml(guidance));

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

    public void setFocus(Context context) {
        softKeyboardController.hideSoftKeyboard(this);
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

    private TextView setupHelpText(TextView helpText, FormEntryPrompt prompt) {
        String s = prompt.getHelpText();

        if (s != null && !s.equals("")) {
            helpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, questionTextSizeHelper.getSubtitle1());
            // wrap to the widget of vi
            helpText.setHorizontallyScrolling(false);
            if (prompt.getLongText() == null || prompt.getLongText().isEmpty()) {
                helpText.setText(HtmlUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(s, prompt.isRequired())));
            } else {
                helpText.setText(HtmlUtils.textToHtml(s));
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

    private void hideAnswerContainerIfNeeded() {
        if (questionDetails.isReadOnly() && formEntryPrompt.getAnswerValue() == null) {
            findViewById(R.id.space_box).setVisibility(VISIBLE);
            findViewById(R.id.answer_container).setVisibility(GONE);
        }
    }

    public void showAnswerContainer() {
        findViewById(R.id.space_box).setVisibility(GONE);
        findViewById(R.id.answer_container).setVisibility(VISIBLE);
    }

    /**
     * Register this widget's child views to pop up a context menu to clear the widget when the
     * user long presses on it. Widget subclasses may override this if some or all of their
     * components need to intercept long presses.
     */
    protected void registerToClearAnswerOnLongPress(Activity activity, ViewGroup viewGroup) {
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

    //region Accessors

    /**
     * @deprecated widgets shouldn't need to know about the instance folder. They can use
     * {@link org.odk.collect.android.utilities.QuestionMediaManager} to access media attached
     * to the instance.
     */
    @Nullable
    @Deprecated
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

    public PermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    public void setPermissionsProvider(PermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
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
