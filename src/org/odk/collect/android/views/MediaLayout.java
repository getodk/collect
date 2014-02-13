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

import java.io.File;

import android.widget.*;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.widgets.QuestionWidget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;

/**
 * This layout is used anywhere we can have image/audio/video/text. TODO: It would probably be nice
 * to put this in a layout.xml file of some sort at some point.
 * 
 * @author carlhartung
 */
public class MediaLayout extends RelativeLayout {
    private static final String t = "AVTLayout";

    private String mSelectionDesignator;
    private FormIndex mIndex;
    private TextView mView_Text;
    private AudioButton mAudioButton;
    private ImageButton mVideoButton;
    private ImageView mImageView;
    private TextView mMissingImage;
    
    private String mVideoURI = null;


    public MediaLayout(Context c) {
        super(c);
        mView_Text = null;
        mAudioButton = null;
        mImageView = null;
        mMissingImage = null;
        mVideoButton = null;
        mIndex = null;
    }

    public void playAudio() {
    	if ( mAudioButton != null ) {
    		mAudioButton.playAudio();
    	}
    }

    public void playVideo() {
    	if ( mVideoURI != null ) {
            String videoFilename = "";
            try {
                videoFilename =
                    ReferenceManager._().DeriveReference(mVideoURI).getLocalURI();
            } catch (InvalidReferenceException e) {
                Log.e(t, "Invalid reference exception");
                e.printStackTrace();
            }

            File videoFile = new File(videoFilename);
            if (!videoFile.exists()) {
                // We should have a video clip, but the file doesn't exist.
                String errorMsg =
                    getContext().getString(R.string.file_missing, videoFilename);
                Log.e(t, errorMsg);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                return;
            }

            Intent i = new Intent("android.intent.action.VIEW");
            i.setDataAndType(Uri.fromFile(videoFile), "video/*");
            try {
                ((Activity) getContext()).startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, "view video"),
                    Toast.LENGTH_SHORT).show();
            }
    	}
    }

    public void setAVT(FormIndex index, String selectionDesignator, TextView text, String audioURI, String imageURI, String videoURI,
            final String bigImageURI) {
    	mSelectionDesignator = selectionDesignator;
    	mIndex = index;
        mView_Text = text;
        mView_Text.setId(QuestionWidget.newUniqueId());
        mVideoURI = videoURI;

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams videoParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // First set up the audio button
        if (audioURI != null) {
            // An audio file is specified
            mAudioButton = new AudioButton(getContext(), mIndex, mSelectionDesignator, audioURI);
            mAudioButton.setId(QuestionWidget.newUniqueId()); // random ID to be used by the
                                                                      // relative layout.
        } else {
            // No audio file specified, so ignore.
        }

        // Then set up the video button
        if (videoURI != null) {
            // An video file is specified
            mVideoButton = new ImageButton(getContext());
            Bitmap b =
                    BitmapFactory.decodeResource(getContext().getResources(),
                        android.R.drawable.ic_media_play);
            mVideoButton.setImageBitmap(b);
            mVideoButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onClick", "playVideoPrompt"+mSelectionDesignator, mIndex);
                	MediaLayout.this.playVideo();
                }

            });
            mVideoButton.setId(QuestionWidget.newUniqueId());
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
                    Display display =
                        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                                .getDefaultDisplay();
                    int screenWidth = display.getWidth();
                    int screenHeight = display.getHeight();
                    Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                    if (b != null) {
                        mImageView = new ImageView(getContext());
                        mImageView.setPadding(2, 2, 2, 2);
                        mImageView.setBackgroundColor(Color.WHITE);
                        mImageView.setImageBitmap(b);
                        mImageView.setId(imageId);

                        if (bigImageURI != null) {
                            mImageView.setOnClickListener(new OnClickListener() {
                            	String bigImageFilename = ReferenceManager._()
                                        .DeriveReference(bigImageURI).getLocalURI();
                                File bigImage = new File(bigImageFilename);


                                @Override
                                public void onClick(View v) {
                                	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onClick", "showImagePromptBigImage"+mSelectionDesignator, mIndex);

                                    Intent i = new Intent("android.intent.action.VIEW");
                                    i.setDataAndType(Uri.fromFile(bigImage), "image/*");
                                    try {
                                        getContext().startActivity(i);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(
                                            getContext(),
                                            getContext().getString(R.string.activity_not_found,
                                                "view image"), Toast.LENGTH_SHORT).show();
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
                    Log.e(t, errorMsg);
                    mMissingImage = new TextView(getContext());
                    mMissingImage.setText(errorMsg);
                    mMissingImage.setPadding(10, 10, 10, 10);
                    mMissingImage.setId(imageId);
                }
            } catch (InvalidReferenceException e) {
                Log.e(t, "image invalid reference exception");
                e.printStackTrace();
            }
        } else {
            // There's no imageURI listed, so just ignore it.
        }

        // e.g., for TextView that flag will be true
        boolean isNotAMultipleChoiceField = !RadioButton.class.isAssignableFrom(text.getClass()) && !CheckBox.class.isAssignableFrom(text.getClass());

        // Determine the layout constraints...
        // Assumes LTR, TTB reading bias!
        if (mView_Text.getText().length() == 0 && (mImageView != null || mMissingImage != null)) {
            // No text; has image. The image is treated as question/choice icon.
            // The Text view may just have a radio button or checkbox. It
            // needs to remain in the layout even though it is blank.
            //
            // The image view, as created above, will dynamically resize and
            // center itself. We want it to resize but left-align itself
            // in the resized area and we want a white background, as otherwise
            // it will show a grey bar to the right of the image icon.
            if (mImageView != null) {
                mImageView.setScaleType(ScaleType.FIT_START);
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
                imageParams.addRule(RelativeLayout.RIGHT_OF, mView_Text.getId());
            }
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (mAudioButton != null && mVideoButton == null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            } else if (mAudioButton == null && mVideoButton != null) {
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
            } else if (mAudioButton != null && mVideoButton != null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
                imageParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
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
            if (mImageView != null) {
                mImageView.setScaleType(ScaleType.FIT_START);
            }
            //
            // Text upper left; audio upper right; video below audio on right.
            // image below text, audio and video buttons; left-aligned with text.
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (mAudioButton != null && mVideoButton == null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
          } else if (mAudioButton == null && mVideoButton != null) {
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
            } else if (mAudioButton != null && mVideoButton != null) {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
                videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                videoParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
            } else {
                textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }

            if (mImageView != null || mMissingImage != null) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                imageParams.addRule(RelativeLayout.BELOW, mView_Text.getId());
            } else {
                textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        }

        addView(mView_Text, textParams);
        if (mAudioButton != null)
            addView(mAudioButton, audioParams);
        if (mVideoButton != null)
            addView(mVideoButton, videoParams);
        if (mImageView != null)
            addView(mImageView, imageParams);
        else if (mMissingImage != null)
            addView(mMissingImage, imageParams);
    }


    /**
     * This adds a divider at the bottom of this layout. Used to separate fields in lists.
     * 
     * @param v
     */
    public void addDivider(ImageView v) {
        RelativeLayout.LayoutParams dividerParams =
            new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        if (mImageView != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mImageView.getId());
        } else if (mMissingImage != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mMissingImage.getId());
        } else if (mVideoButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mVideoButton.getId());
        } else if (mAudioButton != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
        } else if (mView_Text != null) {
            // No picture
            dividerParams.addRule(RelativeLayout.BELOW, mView_Text.getId());
        } else {
            Log.e(t, "Tried to add divider to uninitialized ATVWidget");
            return;
        }
        addView(v, dividerParams);
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE) {
            if (mAudioButton != null) {
                mAudioButton.stopPlaying();
            }
        }
    }

}
