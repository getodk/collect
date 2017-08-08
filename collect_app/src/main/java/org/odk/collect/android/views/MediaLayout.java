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
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.QuestionWidget;

import java.io.File;

import timber.log.Timber;

/**
 * This layout is used anywhere we can have image/audio/video/text. TODO: It would probably be nice
 * to put this in a layout.xml file of some sort at some point.
 *
 * @author carlhartung
 */
public class MediaLayout extends RelativeLayout implements OnClickListener {

    private String selectionDesignator;
    private FormIndex index;
    private TextView viewText;
    private AudioButton audioButton;
    private AppCompatImageButton videoButton;
    private ImageView imageView;
    private TextView missingImage;

    private String videoURI = null;
    private MediaPlayer player;
    private AudioPlayListener audioPlayListener;
    private int playTextColor;
    private int playBackgroundTextColor;
    
    private Context context;

    private CharSequence originalText;


    public MediaLayout(Context c, MediaPlayer player) {
        super(c);
        context = c;
        viewText = null;
        audioButton = null;
        imageView = null;
        missingImage = null;
        videoButton = null;
        index = null;
        this.player = player;
        audioPlayListener = null;
        playTextColor = Color.BLUE;
    }

    public void playAudio() {
        if (audioButton != null) {
            // have to call toString() to remove the html formatting
            // (it's a spanned thing...)
            viewText.setText(viewText.getText().toString());
            viewText.setTextColor(playTextColor);
            audioButton.playAudio();
        }
    }

    public void setPlayTextColor(int textColor) {
        playTextColor = textColor;
    }

    public void setPlayTextBackgroundColor(int textColor) {
        playBackgroundTextColor = textColor;
    }

    /*
     * Resets text formatting to whatever is defaulted
     * in the form
     */
    public void resetTextFormatting() {
        // first set it to defaults
        viewText.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
        // then set the text to our original (brings back any html formatting)
        viewText.setText(originalText);
    }

    public void playVideo() {
        if (videoURI != null) {
            String videoFilename = "";
            try {
                videoFilename =
                        ReferenceManager._().DeriveReference(videoURI).getLocalURI();
            } catch (InvalidReferenceException e) {
                Timber.e(e, "Invalid reference exception due to %s ", e.getMessage());
            }

            File videoFile = new File(videoFilename);
            if (!videoFile.exists()) {
                // We should have a video clip, but the file doesn't exist.
                String errorMsg =
                        getContext().getString(R.string.file_missing, videoFilename);
                Timber.d("File %s is missing", videoFilename);
                ToastUtils.showLongToast(errorMsg);
                return;
            }

            Intent i = new Intent("android.intent.action.VIEW");
            i.setDataAndType(Uri.fromFile(videoFile), "video/*");
            if (i.resolveActivity(getContext().getPackageManager()) != null) {
                getContext().startActivity(i);
            } else {
                ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found, "view video"));
            }
        }
    }

    public void setAVT(FormIndex index, String selectionDesignator, TextView text, String audioURI,
            String imageURI, String videoURI,
            final String bigImageURI) {
        this.selectionDesignator = selectionDesignator;
        this.index = index;
        viewText = text;
        originalText = text.getText();
        viewText.setId(QuestionWidget.newUniqueId());
        this.videoURI = videoURI;

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams videoParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

        // First set up the audio button
        if (audioURI != null) {
            // An audio file is specified
            audioButton = new AudioButton(getContext(), this.index, this.selectionDesignator, audioURI,
                    player);
            audioButton.setPadding(22, 12, 22, 12);
            audioButton.setBackgroundColor(Color.LTGRAY);
            audioButton.setOnClickListener(this);
            audioButton.setId(QuestionWidget.newUniqueId()); // random ID to be used by the
            // relative layout.
        } else {
            // No audio file specified, so ignore.
        }

        // Then set up the video button
        if (videoURI != null) {
            // An video file is specified
            videoButton = new AppCompatImageButton(getContext());
            Bitmap b =
                    BitmapFactory.decodeResource(getContext().getResources(),
                            android.R.drawable.ic_media_play);
            videoButton.setImageBitmap(b);
            videoButton.setPadding(22, 12, 22, 12);
            videoButton.setBackgroundColor(Color.LTGRAY);
            videoButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this, "onClick",
                            "playVideoPrompt" + MediaLayout.this.selectionDesignator, MediaLayout.this.index);
                    MediaLayout.this.playVideo();
                }

            });
            videoButton.setId(QuestionWidget.newUniqueId());
        } else {
            // No video file specified, so ignore.
        }

        // Now set up the image view
        String errorMsg = null;
        final int imageId = QuestionWidget.newUniqueId();
        if (imageURI != null) {
            try {
                String imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                final File imageFile = new File(imageFilename);
                if (imageFile.exists()) {
                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                    int screenWidth = metrics.widthPixels;
                    int screenHeight = metrics.heightPixels;
                    Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight,
                            screenWidth);
                    if (b != null) {
                        imageView = new ImageView(getContext());
                        imageView.setPadding(2, 2, 2, 2);
                        imageView.setImageBitmap(b);
                        imageView.setId(imageId);

                        if (bigImageURI != null) {
                            imageView.setOnClickListener(new OnClickListener() {
                                String bigImageFilename = ReferenceManager._()
                                        .DeriveReference(bigImageURI).getLocalURI();
                                File bigImage = new File(bigImageFilename);


                                @Override
                                public void onClick(View v) {
                                    Collect.getInstance().getActivityLogger().logInstanceAction(
                                            this, "onClick",
                                            "showImagePromptBigImage" + MediaLayout.this.selectionDesignator,
                                            MediaLayout.this.index);

                                    Intent i = new Intent("android.intent.action.VIEW");
                                    i.setDataAndType(Uri.fromFile(bigImage), "image/*");
                                    try {
                                        getContext().startActivity(i);
                                    } catch (ActivityNotFoundException e) {
                                        Timber.d(e, "No Activity found to handle due to %s", e.getMessage());
                                        ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found,
                                                        "view image"));
                                    }
                                }
                            });
                        }
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
                    missingImage = new TextView(getContext());
                    missingImage.setText(errorMsg);
                    missingImage.setPadding(10, 10, 10, 10);
                    missingImage.setId(imageId);
                }
            } catch (InvalidReferenceException e) {
                Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
            }
        } else {
            // There's no imageURI listed, so just ignore it.
        }

        // e.g., for TextView that flag will be true
        boolean isNotAMultipleChoiceField = !RadioButton.class.isAssignableFrom(text.getClass())
                && !CheckBox.class.isAssignableFrom(text.getClass());

        // Determine the layout constraints...
        // Assumes LTR, TTB reading bias!
        if (viewText.getText().length() == 0 && (imageView != null || missingImage != null)) {
            // No text; has image. The image is treated as question/choice icon.
            // The Text view may just have a radio button or checkbox. It
            // needs to remain in the layout even though it is blank.
            //
            // The image view, as created above, will dynamically resize and
            // center itself. We want it to resize but left-align itself
            // in the resized area and we want a white background, as otherwise
            // it will show a grey bar to the right of the image icon.
            if (imageView != null) {
                imageView.setScaleType(ScaleType.FIT_START);
            }
            //
            // In this case, we have:
            // Text upper left; image upper, left edge aligned with text right edge;
            // audio upper right; video below audio on right.
            textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            if (isNotAMultipleChoiceField) {
                imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            } else {
                imageParams.addRule(RelativeLayout.RIGHT_OF, viewText.getId());
            }
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (audioButton != null && videoButton == null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                audioParams.setMargins(0, 0, 11, 0);
                imageParams.addRule(RelativeLayout.LEFT_OF, audioButton.getId());
            } else if (audioButton == null && videoButton != null) {
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.setMargins(0, 0, 11, 0);
                imageParams.addRule(RelativeLayout.LEFT_OF, videoButton.getId());
            } else if (audioButton != null && videoButton != null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                audioParams.setMargins(0, 0, 11, 0);
                imageParams.addRule(RelativeLayout.LEFT_OF, audioButton.getId());
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.addRule(RelativeLayout.BELOW, audioButton.getId());
                videoParams.setMargins(0, 20, 11, 0);
                imageParams.addRule(RelativeLayout.LEFT_OF, videoButton.getId());
            } else {
                // the image will implicitly scale down to fit within parent...
                // no need to bound it by the width of the parent...
                if (!isNotAMultipleChoiceField) {
                    imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
            }
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            // We have a non-blank text label -- image is below the text.
            // In this case, we want the image to be centered...
            if (imageView != null) {
                imageView.setScaleType(ScaleType.FIT_START);
            }
            //
            // Text upper left; audio upper right; video below audio on right.
            // image below text, audio and video buttons; left-aligned with text.
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (audioButton != null && videoButton == null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                audioParams.setMargins(0, 0, 11, 0);
                textParams.addRule(RelativeLayout.LEFT_OF, audioButton.getId());
            } else if (audioButton == null && videoButton != null) {
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.setMargins(0, 0, 11, 0);
                textParams.addRule(RelativeLayout.LEFT_OF, videoButton.getId());
            } else if (audioButton != null && videoButton != null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                audioParams.setMargins(0, 0, 11, 0);
                textParams.addRule(RelativeLayout.LEFT_OF, audioButton.getId());
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.setMargins(0, 20, 11, 0);
                videoParams.addRule(RelativeLayout.BELOW, audioButton.getId());
            } else {
                textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }

            if (imageView != null || missingImage != null) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                if (videoButton != null) {
                    imageParams.addRule(RelativeLayout.LEFT_OF, videoButton.getId());
                } else if (audioButton != null) {
                    imageParams.addRule(RelativeLayout.LEFT_OF, audioButton.getId());
                }
                imageParams.addRule(RelativeLayout.BELOW, viewText.getId());
            } else {
                textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        }

        addView(viewText, textParams);
        if (audioButton != null) {
            addView(audioButton, audioParams);
        }
        if (videoButton != null) {
            addView(videoButton, videoParams);
        }
        if (imageView != null) {
            addView(imageView, imageParams);
        } else if (missingImage != null) {
            addView(missingImage, imageParams);
        }
    }


    /**
     * This adds a divider at the bottom of this layout. Used to separate fields in lists.
     */
    public void addDivider(ImageView v) {
        RelativeLayout.LayoutParams dividerParams =
                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
        if (imageView != null) {
            dividerParams.addRule(RelativeLayout.BELOW, imageView.getId());
        } else if (missingImage != null) {
            dividerParams.addRule(RelativeLayout.BELOW, missingImage.getId());
        } else if (videoButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, videoButton.getId());
        } else if (audioButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, audioButton.getId());
        } else if (viewText != null) {
            // No picture
            dividerParams.addRule(RelativeLayout.BELOW, viewText.getId());
        } else {
            Timber.e("Tried to add divider to uninitialized ATVWidget");
            return;
        }
        addView(v, dividerParams);
    }


    public void setTextcolor(int color) {
        viewText.setTextColor(color);
    }

    public TextView getView_Text() {
        return viewText;
    }

    /**
     * This is what gets called when the AudioButton gets clicked
     */
    @Override
    public void onClick(View v) {
        if (audioPlayListener != null) {
            audioPlayListener.resetQuestionTextColor();
        }
        if (player.isPlaying()) {
            player.stop();
            Bitmap b =
                    BitmapFactory.decodeResource(getContext().getResources(),
                            android.R.drawable.ic_lock_silent_mode_off);
            audioButton.setImageBitmap(b);

        } else {
            playAudio();
            Bitmap b =
                    BitmapFactory.decodeResource(getContext().getResources(),
                            android.R.drawable.ic_media_pause);
            audioButton.setImageBitmap(b);
        }
        player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                resetTextFormatting();
                mediaPlayer.reset();
                Bitmap b =
                        BitmapFactory.decodeResource(getContext().getResources(),
                                android.R.drawable.ic_lock_silent_mode_off);
                audioButton.setImageBitmap(b);
            }
        });
    }

    public void setAudioListener(AudioPlayListener listener) {
        audioPlayListener = listener;
    }

}
