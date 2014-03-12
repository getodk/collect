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
import java.util.ArrayList;
import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
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
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * ListMultiWidget handles multiple selection fields using check boxes. The check boxes are aligned
 * horizontally. They are typically meant to be used in a field list, where multiple questions with
 * the same multiple choice answers can sit on top of each other and make a grid of buttons that is
 * easy to navigate quickly. Optionally, you can turn off the labels. This would be done if a label
 * widget was at the top of your field list to provide the labels. If audio or video are specified
 * in the select answers they are ignored. This class is almost identical to ListWidget, except it
 * uses checkboxes. It also did not require a custom clickListener class.
 * 
 * @author Jeff Beorse (jeff@beorse.net)
 */
public class ListMultiWidget extends QuestionWidget {
    private static final String t = "ListMultiWidget";

    // Holds the entire question and answers. It is a horizontally aligned linear layout
    // needed because it is created in the super() constructor via addQuestionText() call.
    LinearLayout questionLayout;

    private boolean mCheckboxInit = true;
    
    private Vector<SelectChoice> mItems; // may take a while to compute...

    private ArrayList<CheckBox> mCheckboxes;


    @SuppressWarnings("unchecked")
    public ListMultiWidget(Context context, FormEntryPrompt prompt, boolean displayLabel) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }
        mCheckboxes = new ArrayList<CheckBox>();
        mPrompt = prompt;

        // Layout holds the horizontal list of buttons
        LinearLayout buttonLayout = new LinearLayout(context);

        Vector<Selection> ve = new Vector<Selection>();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector<Selection>) prompt.getAnswerValue().getValue();
        }

        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++) {
                CheckBox c = new CheckBox(getContext());
                c.setTag(Integer.valueOf(i));
                c.setId(QuestionWidget.newUniqueId());
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());
                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (mItems.get(i).getValue().equals(ve.elementAt(vi).getValue())) {
                        c.setChecked(true);
                        break;
                    }

                }
                mCheckboxes.add(c);

                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && mPrompt.isReadOnly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                               	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onItemClick.deselect", 
                            			mItems.get((Integer)buttonView.getTag()).getValue(), mPrompt.getIndex());
                            } else {
                                buttonView.setChecked(true);
                               	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onItemClick.select", 
                            			mItems.get((Integer)buttonView.getTag()).getValue(), mPrompt.getIndex());
                            }
                        }
                    }
                });

                String imageURI;
                if (mItems.get(i) instanceof ExternalSelectChoice) {
                    imageURI = ((ExternalSelectChoice) mItems.get(i)).getImage();
                } else {
                    imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE);
                }

                // build image view (if an image is provided)
                ImageView mImageView = null;
                TextView mMissingImage = null;

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
                TextView label = new TextView(getContext());
                label.setText(prompt.getSelectChoiceText(mItems.get(i)));
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
                label.setGravity(Gravity.CENTER_HORIZONTAL);
                if (!displayLabel) {
                    label.setVisibility(View.GONE);
                }

                // answer layout holds the label text/image on top and the radio button on bottom
                RelativeLayout answer = new RelativeLayout(getContext());
                RelativeLayout.LayoutParams headerParams =
                        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                headerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                headerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                
                RelativeLayout.LayoutParams buttonParams =
                        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                if (mImageView != null) {
                	mImageView.setScaleType(ScaleType.CENTER);
                    if (!displayLabel) {
                        mImageView.setVisibility(View.GONE);
                    }
                    answer.addView(mImageView, headerParams);
                } else if (mMissingImage != null) {
                    answer.addView(mMissingImage, headerParams);
                } else {
                    if (displayLabel) {
                    	label.setId(labelId);
                        answer.addView(label, headerParams);
                    }

                }
                if (displayLabel) {
                	buttonParams.addRule(RelativeLayout.BELOW, labelId);
                }
                answer.addView(c, buttonParams);
                answer.setPadding(4, 0, 4, 0);

                // /Each button gets equal weight
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
        // LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
        for (int i = 0; i < mCheckboxes.size(); i++) {
        	CheckBox c = mCheckboxes.get(i);
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }


    @Override
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < mCheckboxes.size(); i++) {
        	CheckBox c = mCheckboxes.get(i);
            if (c.isChecked()) {
                vc.add(new Selection(mItems.get(i)));
            }
        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    // Override QuestionWidget's add question text. Build it the same
    // but add it to the questionLayout
    protected void addQuestionText(FormEntryPrompt p) {

        // Add the text view. Textview always exists, regardless of whether there's text.
    	TextView questionText = new TextView(getContext());
        questionText.setText(p.getLongText());
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
        questionText.setTypeface(null, Typeface.BOLD);
        questionText.setPadding(0, 0, 0, 7);
        questionText.setId(QuestionWidget.newUniqueId()); // assign random id

        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        if (p.getLongText() == null) {
            questionText.setVisibility(GONE);
        }

        // Put the question text on the left half of the screen
        LinearLayout.LayoutParams labelParams =
            new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        labelParams.weight = 1;

        questionLayout = new LinearLayout(getContext());
        questionLayout.setOrientation(LinearLayout.HORIZONTAL);

        questionLayout.addView(questionText, labelParams);
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (CheckBox c : mCheckboxes) {
            c.setOnLongClickListener(l);
        }
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (CheckBox c : mCheckboxes) {
            c.cancelLongPress();
        }
    }
}
