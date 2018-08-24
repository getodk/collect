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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.AnimateUtils;
import org.odk.collect.android.utilities.DependencyProvider;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;
import org.odk.collect.android.widgets.interfaces.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import timber.log.Timber;

public abstract class QuestionWidget
        extends RelativeLayout
        implements Widget, AudioPlayListener {

    private final int questionFontSize;
    private final FormEntryPrompt formEntryPrompt;
    private final MediaLayout questionMediaLayout;
    private MediaPlayer player;
    private final TextView helpTextView;
    private final TextView guidanceTextView;
    private final View helpTextLayout;
    private final View guidanceTextLayout;
    private final View textLayout;
    private final TextView warningText;
    private static final String GUIDANCE_EXPANDED_STATE = "expanded_state";
    private AtomicBoolean expanded;
    private Bundle state;
    protected ThemeUtils themeUtils;
    private int playColor;

    public QuestionWidget(Context context, FormEntryPrompt prompt) {
        super(context);

        themeUtils = new ThemeUtils(context);
        playColor = themeUtils.getAccentColor();

        if (context instanceof FormEntryActivity) {
            state = ((FormEntryActivity) context).getState();
        }

        if (context instanceof DependencyProvider) {
            injectDependencies((DependencyProvider) context);
        }

        player = new MediaPlayer();
        getPlayer().setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                getQuestionMediaLayout().resetTextFormatting();
                mediaPlayer.reset();
            }

        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Timber.e("Error occured in MediaPlayer. what = %d, extra = %d",
                        what, extra);
                return false;
            }
        });

        questionFontSize = Collect.getQuestionFontsize();

        formEntryPrompt = prompt;

        setGravity(Gravity.TOP);
        setPadding(0, 7, 0, 0);

        questionMediaLayout = createQuestionMediaLayout(prompt);
        helpTextLayout = createHelpTextLayout();
        helpTextLayout.setId(ViewIds.generateViewId());
        guidanceTextLayout = helpTextLayout.findViewById(R.id.guidance_text_layout);
        textLayout = helpTextLayout.findViewById(R.id.text_layout);
        warningText = helpTextLayout.findViewById(R.id.warning_text);
        helpTextView = setupHelpText(helpTextLayout.findViewById(R.id.help_text_view), prompt);
        guidanceTextView = setupGuidanceTextAndLayout(helpTextLayout.findViewById(R.id.guidance_text_view), prompt);

        addQuestionMediaLayout(getQuestionMediaLayout());
        addHelpTextLayout(getHelpTextLayout());
    }

    private TextView setupGuidanceTextAndLayout(TextView guidanceTextView, FormEntryPrompt prompt) {

        TextView guidance = null;
        GuidanceHint setting = GuidanceHint.get((String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_GUIDANCE_HINT));

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
                    AnimateUtils.expand(guidanceTextLayout, result -> expanded.set(true));
                } else {
                    AnimateUtils.collapse(guidanceTextLayout, result -> expanded.set(false));
                }
            });

            getHelpTextView().setOnClickListener(v -> {
                if (!expanded.get()) {
                    AnimateUtils.expand(guidanceTextLayout, result -> expanded.set(true));
                } else {
                    AnimateUtils.collapse(guidanceTextLayout, result -> expanded.set(false));
                }
            });
        }

        return guidance;
    }

    private TextView configureGuidanceTextView(TextView guidanceTextView, String guidance) {
        guidanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getQuestionFontSize() - 3);
        //noinspection ResourceType
        guidanceTextView.setPadding(0, -5, 0, 7);
        // wrap to the widget of view
        guidanceTextView.setHorizontallyScrolling(false);
        guidanceTextView.setTypeface(null, Typeface.ITALIC);

        guidanceTextView.setText(TextUtils.textToHtml(guidance));

        guidanceTextView.setTextColor(themeUtils.getPrimaryTextColor());
        guidanceTextView.setMovementMethod(LinkMovementMethod.getInstance());
        return guidanceTextView;
    }

    /**
     * Releases resources held by this widget
     */
    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    //source::https://stackoverflow.com/questions/18996183/identifying-rtl-language-in-android/23203698#23203698
    public static boolean isRTL() {
        return isRTL(Locale.getDefault());
    }

    private static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    protected void injectDependencies(DependencyProvider dependencyProvider) {
        //dependencies for the widget will be wired here.
    }

    private MediaLayout createQuestionMediaLayout(FormEntryPrompt prompt) {
        String promptText = prompt.getLongText();
        // Add the text view. Textview always exists, regardless of whether there's text.
        TextView questionText = new TextView(getContext());
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getQuestionFontSize());
        questionText.setTypeface(null, Typeface.BOLD);
        questionText.setPadding(0, 0, 0, 7);
        questionText.setTextColor(themeUtils.getPrimaryTextColor());
        questionText.setText(TextUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(promptText, prompt.isRequired())));
        questionText.setMovementMethod(LinkMovementMethod.getInstance());

        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        if ((promptText == null || promptText.isEmpty())
                && !(prompt.isRequired() && (prompt.getHelpText() == null || prompt.getHelpText().isEmpty()))) {
            questionText.setVisibility(GONE);
        }

        String imageURI = this instanceof SelectImageMapWidget ? null : prompt.getImageText();
        String audioURI = prompt.getAudioText();
        String videoURI = prompt.getSpecialFormQuestionText("video");

        // shown when image is clicked
        String bigImageURI = prompt.getSpecialFormQuestionText("big-image");

        // Create the layout for audio, image, text
        MediaLayout questionMediaLayout = new MediaLayout(getContext());
        questionMediaLayout.setId(ViewIds.generateViewId()); // assign random id
        questionMediaLayout.setAVT(prompt.getIndex(), "", questionText, audioURI, imageURI, videoURI,
                bigImageURI, getPlayer());
        questionMediaLayout.setAudioListener(this);

        String playColorString = prompt.getFormElement().getAdditionalAttribute(null, "playColor");
        if (playColorString != null) {
            try {
                playColor = Color.parseColor(playColorString);
            } catch (IllegalArgumentException e) {
                Timber.e(e, "Argument %s is incorrect", playColorString);
            }
        }
        questionMediaLayout.setPlayTextColor(getPlayColor());

        return questionMediaLayout;
    }

    public TextView getHelpTextView() {
        return helpTextView;
    }

    public void playAudio() {
        playAllPromptText();
    }

    public void playVideo() {
        getQuestionMediaLayout().playVideo();
    }

    public FormEntryPrompt getFormEntryPrompt() {
        return formEntryPrompt;
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

    public abstract void setOnLongClickListener(OnLongClickListener l);

    /**
     * Override this to implement fling gesture suppression (e.g. for embedded WebView treatments).
     *
     * @return true if the fling gesture should be suppressed
     */
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
        return false;
    }

    /*
     * Add a Views containing the question text, audio (if applicable), and image (if applicable).
     * To satisfy the RelativeLayout constraints, we add the audio first if it exists, then the
     * TextView to fit the rest of the space, then the image if applicable.
     */
    /*
     * Defaults to adding questionlayout to the top of the screen.
     * Overwrite to reposition.
     */
    protected void addQuestionMediaLayout(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as questionMediaLayout");
            return;
        }
        // default for questionmedialayout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.setMargins(10, 0, 10, 0);
        addView(v, params);
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

    /**
     * Add a TextView containing the help text to the default location.
     * Override to reposition.
     */
    protected void addHelpTextLayout(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as helpTextView");
            return;
        }

        // default for helptext
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, getQuestionMediaLayout().getId());
        params.setMargins(10, 0, 10, 0);
        addView(v, params);
    }

    private View createHelpTextLayout() {
        return LayoutInflater.from(getContext()).inflate(R.layout.help_text_layout, null);
    }

    private TextView setupHelpText(TextView helpText, FormEntryPrompt prompt) {
        String s = prompt.getHelpText();

        if (s != null && !s.equals("")) {
            helpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getQuestionFontSize() - 3);
            // wrap to the widget of view
            helpText.setHorizontallyScrolling(false);
            helpText.setTypeface(null, Typeface.ITALIC);
            if (prompt.getLongText() == null || prompt.getLongText().isEmpty()) {
                helpText.setText(TextUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(s, prompt.isRequired())));
            } else {
                helpText.setText(TextUtils.textToHtml(s));
            }
            helpText.setTextColor(themeUtils.getPrimaryTextColor());
            helpText.setMovementMethod(LinkMovementMethod.getInstance());
            return helpText;
        } else {
            helpText.setVisibility(View.GONE);
            return helpText;
        }
    }

    /**
     * Default place to put the answer
     * (below the help text or question text if there is no help text)
     * If you have many elements, use this first
     * and use the standard addView(view, params) to place the rest
     */
    protected void addAnswerView(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as an answerView");
            return;
        }
        // default place to add answer
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, getHelpTextLayout().getId());

        params.setMargins(10, 0, 10, 0);
        addView(v, params);
    }

    /**
     * Every subclassed widget should override this, adding any views they may contain, and calling
     * super.cancelLongPress()
     */
    public void cancelLongPress() {
        super.cancelLongPress();
        if (getQuestionMediaLayout() != null) {
            getQuestionMediaLayout().cancelLongPress();
        }
        if (getHelpTextView() != null) {
            getHelpTextView().cancelLongPress();
        }
    }

    /*
     * Prompts with items must override this
     */
    public void playAllPromptText() {
        getQuestionMediaLayout().playAudio();
    }

    public void resetQuestionTextColor() {
        getQuestionMediaLayout().resetTextFormatting();
    }

    public void resetAudioButtonImage() {
        getQuestionMediaLayout().resetAudioButtonBitmap();
    }

    public void showWarning(String warningBody) {
        warningText.setVisibility(View.VISIBLE);
        warningText.setText(warningBody);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == INVISIBLE || visibility == GONE) {
            stopAudio();
        }
    }

    public void stopAudio() {
        if (player != null && player.isPlaying()) {
            Timber.i("stopAudio " + player);
            player.stop();
            player.reset();
        }
    }

    protected Button getSimpleButton(String text, @IdRes final int withId) {
        final QuestionWidget questionWidget = this;
        final Button button = new Button(getContext());

        button.setId(withId);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        button.setPadding(20, 20, 20, 20);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        button.setLayoutParams(params);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Collect.allowClick()) {
                    ((ButtonWidget) questionWidget).onButtonClick(withId);
                }
            }
        });
        return button;
    }

    protected Button getSimpleButton(@IdRes int id) {
        return getSimpleButton(null, id);
    }

    protected Button getSimpleButton(String text) {
        return getSimpleButton(text, R.id.simple_button);
    }

    protected TextView getCenteredAnswerTextView() {
        TextView textView = getAnswerTextView();
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    protected TextView getAnswerTextView() {
        return getAnswerTextView("");
    }

    protected TextView getAnswerTextView(String text) {
        TextView textView = new TextView(getContext());

        textView.setId(R.id.answer_text);
        textView.setTextColor(themeUtils.getPrimaryTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        textView.setPadding(20, 20, 20, 20);
        textView.setText(text);

        return textView;
    }

    protected ImageView getAnswerImageView(Bitmap bitmap) {
        final ImageView imageView = new ImageView(getContext());
        imageView.setId(ViewIds.generateViewId());
        imageView.setPadding(10, 10, 10, 10);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    /**
     * It's needed only for external choices. Everything works well and
     * out of the box when we use internal choices instead
     */
    protected void clearNextLevelsOfCascadingSelect() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }

        if (formController.currentCaptionPromptIsQuestion()) {
            try {
                FormIndex startFormIndex = formController.getQuestionPrompt().getIndex();
                formController.stepToNextScreenEvent();
                while (formController.currentCaptionPromptIsQuestion()
                        && formController.getQuestionPrompt().getFormElement().getAdditionalAttribute(null, "query") != null) {
                    formController.saveAnswer(formController.getQuestionPrompt().getIndex(), null);
                    formController.stepToNextScreenEvent();
                }
                formController.jumpToIndex(startFormIndex);
            } catch (JavaRosaException e) {
                Timber.d(e);
            }
        }
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

    @NonNull
    public final ActivityLogger getActivityLogger() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        return collect.getActivityLogger();
    }

    public int getQuestionFontSize() {
        return questionFontSize;
    }

    public int getAnswerFontSize() {
        return questionFontSize + 2;
    }

    public TextView getGuidanceTextView() {
        return guidanceTextView;
    }

    public View getHelpTextLayout() {
        return helpTextLayout;
    }

    public MediaLayout getQuestionMediaLayout() {
        return questionMediaLayout;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public int getPlayColor() {
        return playColor;
    }
}
