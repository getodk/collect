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

package org.odk.collect.android.formentry.questions;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.databinding.AudioVideoImageTextLabelBinding;
import org.odk.collect.android.listeners.SelectItemClickListener;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.audioclips.Clip;

import java.io.File;

import timber.log.Timber;

/**
 * Represents a label for a prompt/question or a select choice. The label can have media
 * attached to it as well as text (such as audio, video or an image).
 */
public class AudioVideoImageTextLabel extends RelativeLayout implements View.OnClickListener {
    AudioVideoImageTextLabelBinding binding;

    private TextView textLabel;
    private int originalTextColor;
    private int playTextColor = Color.BLUE;
    private CharSequence questionText;
    private SelectItemClickListener listener;
    private File videoFile;
    private File bigImageFile;

    public AudioVideoImageTextLabel(Context context) {
        super(context);
        binding = AudioVideoImageTextLabelBinding.inflate(LayoutInflater.from(context), this, true);
        textLabel = binding.textLabel;
    }

    public AudioVideoImageTextLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = AudioVideoImageTextLabelBinding.inflate(LayoutInflater.from(context), this, true);
        textLabel = binding.textLabel;
    }

    public void setTextView(TextView questionText) {
        this.questionText = questionText.getText();

        textLabel = questionText;
        textLabel.setId(R.id.text_label);
        textLabel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked();
            }
        });

        binding.textContainer.removeAllViews();
        binding.textContainer.addView(textLabel);
    }

    public void setText(String questionText, boolean isRequiredQuestion, float fontSize) {
        this.questionText = questionText;

        if (questionText != null && !questionText.isEmpty()) {
            textLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            textLabel.setText(HtmlUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(questionText, isRequiredQuestion)));
            textLabel.setMovementMethod(LinkMovementMethod.getInstance());

            // Wrap to the size of the parent view
            textLabel.setHorizontallyScrolling(false);
        } else {
            textLabel.setVisibility(View.GONE);
        }
    }

    public void setAudio(String audioURI, AudioHelper audioHelper) {
        setupAudioButton(audioURI, audioHelper);
    }

    public void setImage(@NonNull File imageFile) {
        if (imageFile.exists()) {
            binding.imageView.layout(0, 0, 0, 0);

            Glide.with(this)
                    .load(imageFile)
                    .centerInside()
                    .into(binding.imageView);

            binding.imageView.setVisibility(VISIBLE);
            binding.imageView.setOnClickListener(this);
        } else {
            binding.missingImage.setVisibility(VISIBLE);
            binding.missingImage.setText(getContext().getString(R.string.file_missing, imageFile));
        }
    }

    public void setBigImage(@NonNull File bigImageFile) {
        this.bigImageFile = bigImageFile;
    }

    public void setVideo(@NonNull File videoFile) {
        this.videoFile = videoFile;
        setupVideoButton();
    }

    public void setPlayTextColor(int textColor) {
        playTextColor = textColor;
        binding.audioButton.setColors(getThemeUtils().getColorOnSurface(), playTextColor);
    }

    public void playVideo() {
        if (!videoFile.exists()) {
            // We should have a video clip, but the file doesn't exist.
            String errorMsg = getContext().getString(R.string.file_missing, videoFile);
            Timber.d("File %s is missing", videoFile);
            ToastUtils.showLongToast(getContext(), errorMsg);
            return;
        }

        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri =
                ContentUriProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", videoFile);
        FileUtils.grantFileReadPermissions(intent, uri, getContext());
        intent.setDataAndType(uri, "video/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        } else {
            ToastUtils.showShortToast(getContext(), getContext().getString(R.string.activity_not_found, getContext().getString(R.string.view_video)));
        }
    }

    public TextView getLabelTextView() {
        return textLabel;
    }

    public ImageView getImageView() {
        return binding.imageView;
    }

    public TextView getMissingImage() {
        return binding.missingImage;
    }

    public Button getVideoButton() {
        return binding.videoButton;
    }

    public Button getAudioButton() {
        return binding.audioButton;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoButton:
                playVideo();
                break;
            case R.id.imageView:
                onImageClick();
                break;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        textLabel.setEnabled(enabled);
        binding.imageView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return textLabel.isEnabled() && binding.imageView.isEnabled();
    }

    private void onImageClick() {
        if (bigImageFile != null) {
            openImage();
        } else {
            selectItem();
        }
    }

    private void openImage() {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            Uri uri =
                    ContentUriProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", bigImageFile);
            FileUtils.grantFileReadPermissions(intent, uri, getContext());
            intent.setDataAndType(uri, "image/*");
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Timber.d(e, "No Activity found to handle due to %s", e.getMessage());
            ToastUtils.showShortToast(getContext(), getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.view_image)));
        }
    }

    private void selectItem() {
        if (textLabel instanceof RadioButton) {
            ((RadioButton) textLabel).setChecked(true);
        } else if (textLabel instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) textLabel;
            checkbox.setChecked(!checkbox.isChecked());
        }
        if (listener != null) {
            listener.onItemClicked();
        }
    }

    private void setupVideoButton() {
        binding.videoButton.setVisibility(VISIBLE);
        binding.mediaButtons.setVisibility(VISIBLE);
        binding.videoButton.setOnClickListener(this);
    }

    private void setupAudioButton(String audioURI, AudioHelper audioHelper) {
        binding.audioButton.setVisibility(VISIBLE);
        binding.mediaButtons.setVisibility(VISIBLE);

        ScreenContext activity = getScreenContext();
        String clipID = getTag() != null ? getTag().toString() : "";
        LiveData<Boolean> isPlayingLiveData = audioHelper.setAudio(binding.audioButton, new Clip(clipID, audioURI));

        originalTextColor = textLabel.getTextColors().getDefaultColor();
        isPlayingLiveData.observe(activity.getViewLifecycle(), isPlaying -> {
            if (isPlaying) {
                textLabel.setTextColor(playTextColor);
            } else {
                textLabel.setTextColor(originalTextColor);
                // then set the text to our original (brings back any html formatting)
                textLabel.setText(questionText);
            }
        });
    }

    @NotNull
    private ThemeUtils getThemeUtils() {
        return new ThemeUtils(getContext());
    }

    private ScreenContext getScreenContext() {
        try {
            return (ScreenContext) getContext();
        } catch (ClassCastException e) {
            throw new RuntimeException(getContext().toString() + " must implement " + ScreenContext.class.getName());
        }
    }

    public void setItemClickListener(SelectItemClickListener listener) {
        this.listener = listener;
    }
}
