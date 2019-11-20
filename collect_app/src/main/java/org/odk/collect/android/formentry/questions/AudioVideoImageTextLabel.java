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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;

import com.google.android.material.button.MaterialButton;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.ViewIds;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Represents a label for a prompt/question or a select choice. The label can have media
 * attached to it as well as text (such as audio, video or an image).
 */
public class AudioVideoImageTextLabel extends RelativeLayout implements View.OnClickListener {

    @BindView(R.id.audioButton)
    AudioButton audioButton;

    @BindView(R.id.videoButton)
    MaterialButton videoButton;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.missingImage)
    TextView missingImage;

    @BindView(R.id.text_container)
    FrameLayout textContainer;

    private TextView labelTextView;
    private String videoURI;
    private int playTextColor = Color.BLUE;
    private CharSequence originalText;
    private String bigImageURI;
    private ReferenceManager referenceManager;

    public AudioVideoImageTextLabel(Context context) {
        super(context);

        View.inflate(context, R.layout.audio_video_image_text_label, this);
        ButterKnife.bind(this);
    }

    public AudioVideoImageTextLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.audio_video_image_text_label, this);
        ButterKnife.bind(this);
    }

    public void setText(TextView questionText) {
        originalText = questionText.getText();

        this.labelTextView = questionText;
        this.labelTextView.setId(ViewIds.generateViewId());
        textContainer.removeAllViews();
        textContainer.addView(this.labelTextView);
    }

    public void setAudio(String audioURI, AudioHelper audioHelper) {
        setupAudioButton(audioURI, audioHelper);
    }

    /**
     * This should move to separate setters like {@link #setAudio(String, AudioHelper)}
     */
    @Deprecated
    public void setImageVideo(String imageURI, String videoURI,
                              String bigImageURI, ReferenceManager referenceManager) {
        this.bigImageURI = bigImageURI;
        this.videoURI = videoURI;
        this.referenceManager = referenceManager;

        if (videoURI != null) {
            setupVideoButton();
        }

        if (imageURI != null) {
            setupBigImage(imageURI);
        }
    }

    public void setPlayTextColor(int textColor) {
        playTextColor = textColor;
        audioButton.setColors(getThemeUtils().getColorOnSurface(), playTextColor);
    }

    public void playVideo() {
        String videoFilename = "";
        try {
            videoFilename = referenceManager.deriveReference(videoURI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid reference exception due to %s ", e.getMessage());
        }

        File videoFile = new File(videoFilename);
        if (!videoFile.exists()) {
            // We should have a video clip, but the file doesn't exist.
            String errorMsg = getContext().getString(R.string.file_missing, videoFilename);
            Timber.d("File %s is missing", videoFilename);
            ToastUtils.showLongToast(errorMsg);
            return;
        }

        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri =
                FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", videoFile);
        FileUtils.grantFileReadPermissions(intent, uri, getContext());
        intent.setDataAndType(uri, "video/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        } else {
            ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found, getContext().getString(R.string.view_video)));
        }
    }

    public TextView getLabelTextView() {
        return labelTextView;
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
        labelTextView.setEnabled(enabled);
        imageView.setEnabled(enabled);
    }

    private void onImageClick() {
        if (bigImageURI != null) {
            openImage();
        } else {
            selectItem();
        }
    }

    private void openImage() {
        try {
            File bigImage = new File(referenceManager.DeriveReference(bigImageURI).getLocalURI());
            Intent intent = new Intent("android.intent.action.VIEW");
            Uri uri =
                    FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", bigImage);
            FileUtils.grantFileReadPermissions(intent, uri, getContext());
            intent.setDataAndType(uri, "image/*");
            getContext().startActivity(intent);
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        } catch (ActivityNotFoundException e) {
            Timber.d(e, "No Activity found to handle due to %s", e.getMessage());
            ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.view_image)));
        }
    }

    private void selectItem() {
        if (labelTextView instanceof RadioButton) {
            ((RadioButton) labelTextView).setChecked(true);
        } else if (labelTextView instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) labelTextView;
            checkbox.setChecked(!checkbox.isChecked());
        }
    }

    private void setupBigImage(String imageURI) {
        String errorMsg = null;

        try {
            String imageFilename = this.referenceManager.deriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;
                Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                if (b != null) {
                    imageView.setVisibility(VISIBLE);
                    imageView.setImageBitmap(b);
                    imageView.setOnClickListener(this);
                } else {
                    // Loading the image failed, so it's likely a bad file.
                    errorMsg = getContext().getString(R.string.file_invalid, imageFile);
                }
            } else {
                // We should have an image, but the file doesn't exist.
                errorMsg = getContext().getString(R.string.file_missing, imageFile);
            }

            if (errorMsg != null) {
                // errorMsg is only set when an error has occurred
                Timber.e(errorMsg);
                imageView.setVisibility(View.GONE);
                missingImage.setVisibility(VISIBLE);
                missingImage.setText(errorMsg);
            }
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        }
    }

    private void setupVideoButton() {
        videoButton.setVisibility(VISIBLE);
        videoButton.setOnClickListener(this);
    }

    private void setupAudioButton(String audioURI, AudioHelper audioHelper) {
        audioButton.setVisibility(VISIBLE);

        ScreenContext activity = getScreenContext();
        String clipID = getTag() != null ? getTag().toString() : "";
        LiveData<Boolean> isPlayingLiveData = audioHelper.setAudio(audioButton, new Clip(clipID, audioURI));
        isPlayingLiveData.observe(activity.getViewLifecycle(), isPlaying -> {
            if (isPlaying) {
                labelTextView.setTextColor(playTextColor);
            } else {
                labelTextView.setTextColor(getThemeUtils().getColorOnSurface());
                // then set the text to our original (brings back any html formatting)
                labelTextView.setText(originalText);
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
}
