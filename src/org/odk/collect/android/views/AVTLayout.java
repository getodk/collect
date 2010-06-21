package org.odk.collect.android.views;

import java.io.File;
import java.io.IOException;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This layout is used anywhere we can have text/audio/video. TODO: It would probably be nice to put
 * this in a layout.xml file of some sort at some point.
 * 
 * @author carlhartung
 * 
 */
public class AVTLayout extends RelativeLayout {
    private static final String t = "AVTLayout";

    private View mView_Text;
    private AudioButton mAudioButton;
    private ImageView mImageView;
    private TextView mMissingImage;


    public AVTLayout(Context c) {
        super(c);
        mView_Text = null;
        mAudioButton = null;
        mImageView = null;
        mMissingImage = null;
    }


    public void setAVT(View text, String audioURI, String imageURI) {
        mView_Text = text;

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams =
                new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams =
                new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        // First set up the audio button
        if (audioURI != null) {
            // An audio file is specified
            Log.e("carl", "adding new audio button");
            mAudioButton = new AudioButton(getContext(), audioURI);
        } else {
            // No audio file specified, so ignore.
        }

        // Add the audioButton (if applicable) and view (containing text) to the relative layout.
        if (mAudioButton != null) {
            mAudioButton.setId(3245345); // random ID to be used by the relative layout.
            audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
            this.addView(mAudioButton, audioParams);
        }
        textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        this.addView(text, textParams);

        //TODO:  Make it so clicking on the image brings up an imageviewer.
        // Now set up the image view
        String errorMsg = null;
        if (imageURI != null) {
            try {
                String imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                File imageFile = new File(imageFilename);
                if (imageFile.exists()) {
                    Bitmap b =
                            BitmapFactory.decodeStream(ReferenceManager._().DeriveReference(
                                    imageURI).getStream());
                    if (b != null) {
                        mImageView = new ImageView(getContext());
                        mImageView.setPadding(10, 10, 10, 10);
                        mImageView.setAdjustViewBounds(true);
                        mImageView.setImageBitmap(b);
                        mImageView.setId(23423534);
                        imageParams.addRule(RelativeLayout.BELOW, text.getId());
                        this.addView(mImageView, imageParams);
                    } else {
                        // Loading the image failed, so it's likely a bad file.
                        errorMsg = "File: " + imageFile + " is not a valid image!";
                    }
                } else {
                    // We should have an image, but the file doesn't exist.
                    errorMsg = "! " + " Image file: " + imageFile + " does not exist !";
                }

                if (errorMsg != null) {
                    // errorMsg is only set when an error has occured
                    Log.e(t, errorMsg);
                    mMissingImage = new TextView(getContext());
                    mMissingImage.setText(errorMsg);
                    imageParams.addRule(RelativeLayout.BELOW, text.getId());
                    mMissingImage.setPadding(10, 10, 10, 10);
                    mMissingImage.setId(234873453);
                    this.addView(mMissingImage, imageParams);
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
     * This adds a divider at the bottom of this layout.  Used to separate fields in lists.
     * @param v
     */
    public void addDivider(ImageView v) {
        RelativeLayout.LayoutParams dividerParams =
                new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        if (mImageView != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mImageView.getId());
        } else if (mMissingImage != null) {
            dividerParams.addRule(RelativeLayout.BELOW, mMissingImage.getId());
        } else if (mView_Text != null) {
            // No picture
            dividerParams.addRule(RelativeLayout.BELOW, mView_Text.getId());
        } else {
            Log.e(t, "Tried to add divider to uninitialized ATVWidget");
            return;
        }
        addView(v, dividerParams);
    }


}
