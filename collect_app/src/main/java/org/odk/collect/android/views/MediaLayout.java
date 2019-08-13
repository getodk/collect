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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.ViewIds;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * This layout is used anywhere we can have image/audio/video/text
 *
 * @author carlhartung
 */
public class MediaLayout extends RelativeLayout implements View.OnClickListener {

    @BindView(R.id.audioButton)
    AudioButton audioButton;

    @BindView(R.id.videoButton)
    AppCompatImageButton videoButton;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.missingImage)
    TextView missingImage;

    @BindView(R.id.divider)
    ImageView divider;

    @BindView(R.id.select_container)
    FrameLayout flContainer;

    private TextView viewText;
    private String videoURI;
    private AudioPlayListener audioPlayListener;
    private int playTextColor = Color.BLUE;
    private CharSequence originalText;
    private String bigImageURI;
    private MediaPlayer player;
    private ReferenceManager referenceManager = ReferenceManager.instance();

    public MediaLayout(Context context) {
        super(context);

        View.inflate(context, R.layout.media_layout, this);
        ButterKnife.bind(this);
    }

    public MediaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.media_layout, this);
        ButterKnife.bind(this);
    }

    /**
     * For stubbing during unit testing
     */
    public void setReferenceManager(ReferenceManager referenceManager) {
        this.referenceManager = referenceManager;
    }

    public void playAudio() {
        if (audioPlayListener != null) {
            audioPlayListener.resetQuestionTextColor();
            audioPlayListener.resetAudioButtonImage();
        }

        audioButton.onClick();

        // have to call toString() to remove the html formatting
        // (it's a spanned thing...)
        viewText.setText(viewText.getText().toString());

        if (player.isPlaying()) {
            viewText.setTextColor(playTextColor);
        } else {
            resetTextFormatting();
        }

        player.setOnCompletionListener(mediaPlayer -> {
            resetTextFormatting();
            mediaPlayer.reset();
            audioButton.resetBitmap();
        });
    }

    public void setPlayTextColor(int textColor) {
        playTextColor = textColor;
    }

    /*
     * Resets text formatting to whatever is defaulted
     * in the form
     */
    public void resetTextFormatting() {
        // first set it to defaults
        viewText.setTextColor(new ThemeUtils(getContext()).getPrimaryTextColor());
        // then set the text to our original (brings back any html formatting)
        viewText.setText(originalText);
    }

    public void resetAudioButtonBitmap() {
        audioButton.resetBitmap();
    }

    public void playVideo() {
        String videoFilename = "";
        try {
            videoFilename = referenceManager.DeriveReference(videoURI).getLocalURI();
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

    public void setAVT(TextView text, String audioURI, String imageURI, String videoURI,
                       String bigImageURI, MediaPlayer player) {
        this.bigImageURI = bigImageURI;
        this.player = player;
        this.videoURI = videoURI;

        viewText = text;
        originalText = text.getText();
        viewText.setId(ViewIds.generateViewId());

        // Setup audio button
        if (audioURI != null) {
            audioButton.setVisibility(VISIBLE);
            audioButton.init(audioURI, player);
            audioButton.setOnClickListener(this);
        }

        // Setup video button
        if (videoURI != null) {
            videoButton.setVisibility(VISIBLE);
            Bitmap b = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
            videoButton.setImageBitmap(b);
            videoButton.setOnClickListener(this);
        }

        // Setup image view
        String errorMsg = null;
        if (imageURI != null) {
            try {
                String imageFilename = referenceManager.DeriveReference(imageURI).getLocalURI();
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
                    missingImage.setVisibility(VISIBLE);
                    missingImage.setText(errorMsg);
                }
            } catch (InvalidReferenceException e) {
                Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
            }
        }

        flContainer.removeAllViews();
        flContainer.addView(viewText);
    }

    public TextView getView_Text() {
        return viewText;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioButton:
                playAudio();
                break;
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
        viewText.setEnabled(enabled);
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
        if (viewText instanceof RadioButton) {
            ((RadioButton) viewText).setChecked(true);
        } else if (viewText instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) viewText;
            checkbox.setChecked(!checkbox.isChecked());
        }
    }

    public void setAudioListener(AudioPlayListener listener) {
        audioPlayListener = listener;
    }

    public void addDivider() {
        divider.setVisibility(VISIBLE);
    }
}
