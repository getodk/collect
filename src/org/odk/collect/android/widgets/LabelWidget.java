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

import java.io.File;
import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.FileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The Label Widget does not return an answer. The purpose of this widget is to be the top entry in
 * a field-list with a bunch of list widgets below. This widget provides the labels, so that the
 * list widgets can hide their labels and reduce the screen clutter. This class is essentially
 * ListWidget with all the answer generating code removed.
 * 
 * @author Jeff Beorse
 */
public class LabelWidget extends QuestionWidget {
    private static final String t = "LabelWidget";

    LinearLayout buttonLayout;
    LinearLayout questionLayout;
    Vector<SelectChoice> mItems;

    private TextView mQuestionText;
    private TextView mMissingImage;
    private ImageView mImageView;
    private TextView label;


    public LabelWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }
        mPrompt = prompt;

        buttonLayout = new LinearLayout(context);

        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++) {

                String imageURI;
                if (mItems.get(i) instanceof ExternalSelectChoice) {
                    imageURI = ((ExternalSelectChoice) mItems.get(i)).getImage();
                } else {
                    imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE);
                }

                // build image view (if an image is provided)
                mImageView = null;
                mMissingImage = null;

                final int labelId = QuestionWidget.newUniqueId();

                // Now set up the image view
                String errorMsg = null;
                if (imageURI != null) {
                    try {
                        String imageFilename =
                            ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                        final File imageFile = new File(imageFilename);
                        if (imageFile.exists()) {
                            Bitmap b = null;
                            try {
                                Display display =
                                    ((WindowManager) getContext().getSystemService(
                                        Context.WINDOW_SERVICE)).getDefaultDisplay();
                                int screenWidth = display.getWidth();
                                int screenHeight = display.getHeight();
                                b =
                                    FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight,
                                        screenWidth);
                            } catch (OutOfMemoryError e) {
                                errorMsg = "ERROR: " + e.getMessage();
                            }

                            if (b != null) {
                                mImageView = new ImageView(getContext());
                                mImageView.setPadding(2, 2, 2, 2);
                                mImageView.setAdjustViewBounds(true);
                                mImageView.setImageBitmap(b);
                                mImageView.setId(labelId);
                            } else if (errorMsg == null) {
                                // An error hasn't been logged and loading the image failed, so it's
                                // likely
                                // a bad file.
                                errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                            }
                        } else if (errorMsg == null) {
                            // An error hasn't been logged. We should have an image, but the file
                            // doesn't
                            // exist.
                            errorMsg = getContext().getString(R.string.file_missing, imageFile);
                        }

                        if (errorMsg != null) {
                            // errorMsg is only set when an error has occured
                            Log.e(t, errorMsg);
                            mMissingImage = new TextView(getContext());
                            mMissingImage.setText(errorMsg);

                            mMissingImage.setPadding(2, 2, 2, 2);
                            mMissingImage.setId(labelId);
                        }
                    } catch (InvalidReferenceException e) {
                        Log.e(t, "image invalid reference exception");
                        e.printStackTrace();
                    }
                } else {
                    // There's no imageURI listed, so just ignore it.
                }

                // build text label. Don't assign the text to the built in label to he
                // button because it aligns horizontally, and we want the label on top
                label = new TextView(getContext());
                label.setText(prompt.getSelectChoiceText(mItems.get(i)));
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
                label.setGravity(Gravity.CENTER_HORIZONTAL);

                // answer layout holds the label text/image on top and the radio button on bottom
                RelativeLayout answer = new RelativeLayout(getContext());
                RelativeLayout.LayoutParams headerParams =
                        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                headerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                
                if (mImageView != null) {
                	mImageView.setScaleType(ScaleType.CENTER);
                    answer.addView(mImageView, headerParams);
                } else if (mMissingImage != null) {
                    answer.addView(mMissingImage, headerParams);
                } else {
                	label.setId(labelId);
                    answer.addView(label, headerParams);
                }
                answer.setPadding(4, 0, 4, 0);

                // Each button gets equal weight
                LinearLayout.LayoutParams answerParams =
                    new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                            LayoutParams.WRAP_CONTENT);
                answerParams.weight = 1;

                buttonLayout.addView(answer, answerParams);

            }
        }

        // Align the buttons so that they appear horizonally and are right justified
        // buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        // LinearLayout.LayoutParams params = new
        // LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        // buttonLayout.setLayoutParams(params);

        // The buttons take up the right half of the screen
        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1;

        questionLayout.addView(buttonLayout, buttonParams);
        addView(questionLayout);

    }


    @Override
    public void clearAnswer() {
        // Do nothing, no answers to clear
    }


    @Override
    public IAnswerData getAnswer() {
        return null;
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    // Override QuestionWidget's add question text. Build it the same
    // but add it to the relative layout
    protected void addQuestionText(FormEntryPrompt p) {

        // Add the text view. Textview always exists, regardless of whether there's text.
        mQuestionText = new TextView(getContext());
        mQuestionText.setText(p.getLongText());
        mQuestionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
        mQuestionText.setTypeface(null, Typeface.BOLD);
        mQuestionText.setPadding(0, 0, 0, 7);
        mQuestionText.setId(QuestionWidget.newUniqueId()); // assign random id

        // Wrap to the size of the parent view
        mQuestionText.setHorizontallyScrolling(false);

        if (p.getLongText() == null) {
            mQuestionText.setVisibility(GONE);
        }

        // Put the question text on the left half of the screen
        LinearLayout.LayoutParams labelParams =
            new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        labelParams.weight = 1;

        questionLayout = new LinearLayout(getContext());
        questionLayout.setOrientation(LinearLayout.HORIZONTAL);

        questionLayout.addView(mQuestionText, labelParams);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mQuestionText.cancelLongPress();

        if (mMissingImage != null) {
            mMissingImage.cancelLongPress();
        }
        if (mImageView != null) {
            mImageView.cancelLongPress();
        }
        if (label != null) {
            label.cancelLongPress();
        }
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mQuestionText.setOnLongClickListener(l);
        if (mMissingImage != null) {
            mMissingImage.setOnLongClickListener(l);
        }
        if (mImageView != null) {
            mImageView.setOnLongClickListener(l);
        }
        if (label != null) {
            label.setOnLongClickListener(l);
        }
    }

}
