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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * The Label Widget does not return an answer. The purpose of this widget is to be the top entry in
 * a field-list with a bunch of list widgets below. This widget provides the labels, so that the
 * list widgets can hide their labels and reduce the screen clutter. This class is essentially
 * ListWidget with all the answer generating code removed.
 *
 * @author Jeff Beorse
 */
public class LabelWidget extends QuestionWidget {

    List<SelectChoice> items;
    View center;
    private TextView label;
    private ImageView imageView;
    private TextView missingImage;


    public LabelWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(
                prompt.getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr);
        } else {
            items = prompt.getSelectChoices();
        }

        // Layout holds the horizontal list of buttons
        LinearLayout buttonLayout = new LinearLayout(context);

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {

                View answerLayout = inflate(context, R.layout.list_widget_item_layout, null);

                imageView = (ImageView) answerLayout.findViewById(R.id.image);
                missingImage = (TextView) answerLayout.findViewById(R.id.missingImage);
                label = (TextView) answerLayout.findViewById(R.id.label);

                answerLayout.findViewById(R.id.radioButton).setVisibility(GONE);

                initImageView(prompt, i);
                initLabel(prompt, i);

                // Each button gets equal weight
                LinearLayout.LayoutParams answerParams =
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                answerParams.weight = 1;

                buttonLayout.addView(answerLayout, answerParams);
            }
        }
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        addAnswerView(buttonLayout);
    }

    private void initLabel(FormEntryPrompt prompt, int i) {
        // Don't assign the text to the built in label to he
        // button because it aligns horizontally, and we want the label on top
        label.setText(prompt.getSelectChoiceText(items.get(i)));
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, questionFontsize);
    }

    private void initImageView(FormEntryPrompt prompt, int i) {

        String imageURI = getImageURI(prompt, i);

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
                        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                        int screenWidth = metrics.widthPixels;
                        int screenHeight = metrics.heightPixels;
                        b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight,
                                screenWidth);
                    } catch (OutOfMemoryError e) {
                        errorMsg = "ERROR: " + e.getMessage();
                    }

                    if (b != null) {
                        imageView.setImageBitmap(b);
                        label.setVisibility(GONE);
                    } else if (errorMsg == null) {
                        // An error hasn't been logged and loading the image failed, so it's
                        // likely
                        // a bad file.
                        errorMsg = getContext().getString(R.string.file_invalid, imageFile);
                    }
                } else {
                    // An error hasn't been logged. We should have an image, but the file
                    // doesn't
                    // exist.
                    errorMsg = getContext().getString(R.string.file_missing, imageFile);
                }

                if (errorMsg != null) {
                    // errorMsg is only set when an error has occured
                    missingImage.setText(errorMsg);
                    missingImage.setVisibility(VISIBLE);
                    Timber.e(errorMsg);
                }
            } catch (InvalidReferenceException e) {
                Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
            }
        } else {
            // There's no imageURI listed, so just ignore it.
        }

    }

    private String getImageURI(FormEntryPrompt prompt, int i) {
        if (items.get(i) instanceof ExternalSelectChoice) {
            return ((ExternalSelectChoice) items.get(i)).getImage();
        } else {
            return prompt.getSpecialFormSelectChoiceText(items.get(i),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }
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


    @Override
    protected void addQuestionMediaLayout(View v) {
        center = new View(getContext());
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(0, 0);
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        center.setId(QuestionWidget.newUniqueId());
        addView(center, centerParams);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.LEFT_OF, center.getId());
        addView(v, params);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

}
