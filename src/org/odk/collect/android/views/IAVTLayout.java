
package org.odk.collect.android.views;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * This layout is used anywhere we can have image/audio/video/text. TODO: It would probably be nice
 * to put this in a layout.xml file of some sort at some point.
 * 
 * @author carlhartung
 */
public class IAVTLayout extends RelativeLayout {
    private static final String t = "AVTLayout";

    private TextView mView_Text;
    private AudioButton mAudioButton;
    private ImageButton mVideoButton;
    private ImageView mImageView;
    private TextView mMissingImage;


    public IAVTLayout(Context c) {
        super(c);
        mView_Text = null;
        mAudioButton = null;
        mImageView = null;
        mMissingImage = null;
        mVideoButton = null;
    }


    public void setAVT(TextView text, String audioURI, String imageURI, final String videoURI,
            final String bigImageURI) {
        mView_Text = text;

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams =
            new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams =
            new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams videoParams =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // First set up the audio button
        if (audioURI != null) {
            // An audio file is specified
            mAudioButton = new AudioButton(getContext(), audioURI);
            mAudioButton.setId(3245345); // random ID to be used by the relative layout.
        } else {
            // No audio file specified, so ignore.
        }

        // Then set up the video button
        if (videoURI != null) {
            // An audio file is specified
            mVideoButton = new ImageButton(getContext());
            mVideoButton.setImageResource(android.R.drawable.ic_media_play);
            mVideoButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String videoFilename = "";
                    try {
                        videoFilename =
                            ReferenceManager._().DeriveReference(videoURI).getLocalURI();
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
                    ((Activity) getContext()).startActivity(i);
                }

            });
            mVideoButton.setId(234982340);
        } else {
            // No video file specified, so ignore.
        }

        // Add the audioButton and videoButton (if applicable) and view (containing text) to the
        // relative layout.
        if (mAudioButton != null && mVideoButton == null) {
            audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            addView(mAudioButton, audioParams);
        } else if (mAudioButton == null && mVideoButton != null) {
            videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mVideoButton.getId());
            addView(mVideoButton, videoParams);
        } else if (mAudioButton != null && mVideoButton != null) {
            audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            videoParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
            addView(mAudioButton, audioParams);
            addView(mVideoButton, videoParams);
        }
        boolean textVisible = (text.getVisibility() != GONE);
        if (textVisible) {
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            addView(text, textParams);
        }

        // Now set up the image view
        String errorMsg = null;
        if (imageURI != null) {
            try {
                String imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                final File imageFile = new File(imageFilename);
                if (imageFile.exists()) {
                    Bitmap b = null;
                    try {
                        b =
                            BitmapFactory.decodeStream(ReferenceManager._()
                                    .DeriveReference(imageURI).getStream());
                    } catch (OutOfMemoryError e) {
                        errorMsg = "ERROR: " + e.getMessage();
                    }

                    if (b != null) {
                        mImageView = new ImageView(getContext());
                        mImageView.setPadding(10, 10, 10, 10);
                        mImageView.setAdjustViewBounds(true);
                        mImageView.setImageBitmap(b);
                        mImageView.setId(23423534);
                        imageParams.addRule(RelativeLayout.BELOW, text.getId());
                        if (mAudioButton != null) {
                            if (textVisible) {
                                imageParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
                            } else {
                                imageParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
                            }
                        }
                        if (mVideoButton != null) {
                            if (textVisible) {
                                imageParams.addRule(RelativeLayout.BELOW, mVideoButton.getId());
                            } else {
                                imageParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
                            }
                        }
                        if (bigImageURI != null) {
                            mImageView.setOnClickListener(new OnClickListener() {
                                String bigImageFilename = ReferenceManager._()
                                        .DeriveReference(bigImageURI).getLocalURI();
                                File bigImage = new File(bigImageFilename);


                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent("android.intent.action.VIEW");
                                    i.setDataAndType(Uri.fromFile(bigImage), "image/*");
                                    getContext().startActivity(i);
                                }
                            });
                        }
                        addView(mImageView, imageParams);
                    } else if (errorMsg == null) {
                        // An error hasn't been logged and loading the image failed, so it's likely
                        // a bad file.
                        errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                    }
                } else if (errorMsg == null) {
                    // An error hasn't been logged. We should have an image, but the file doesn't
                    // exist.
                    errorMsg = getContext().getString(R.string.file_missing, imageFile);
                }

                if (errorMsg != null) {
                    // errorMsg is only set when an error has occured
                    Log.e(t, errorMsg);
                    mMissingImage = new TextView(getContext());
                    mMissingImage.setText(errorMsg);
                    imageParams.addRule(RelativeLayout.BELOW, text.getId());
                    if (mAudioButton != null)
                        imageParams.addRule(RelativeLayout.BELOW, mAudioButton.getId());
                    if (mVideoButton != null)
                        imageParams.addRule(RelativeLayout.BELOW, mVideoButton.getId());
                    mMissingImage.setPadding(10, 10, 10, 10);
                    mMissingImage.setId(234873453);
                    addView(mMissingImage, imageParams);
                }
            } catch (IOException e) {
                Log.e(t, "Image io exception");
                e.printStackTrace();
            } catch (InvalidReferenceException e) {
                Log.e(t, "image invalid reference exception");
                e.printStackTrace();
            }
        } else {
            // There's no imageURI listed, so just ignore it.
        }
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
