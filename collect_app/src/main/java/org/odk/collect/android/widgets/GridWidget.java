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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
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
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.AudioButton.AudioHandler;
import org.odk.collect.android.views.ExpandedHeightGridView;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * GridWidget handles select-one fields using a grid of icons. The user clicks the desired icon and
 * the background changes from black to orange. If text, audio, or video are specified in the
 * select
 * answers they are ignored.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class GridWidget extends QuestionWidget implements MultiChoiceWidget {

    // The RGB value for the orange background
    public static final int ORANGE_RED_VAL = 255;
    public static final int ORANGE_GREEN_VAL = 140;
    public static final int ORANGE_BLUE_VAL = 0;

    private static final int HORIZONTAL_PADDING = 7;
    private static final int VERTICAL_PADDING = 5;
    private static final int SPACING = 2;
    private static final int IMAGE_PADDING = 8;
    private static final int SCROLL_WIDTH = 16;

    List<SelectChoice> items;

    // The possible select choices
    String[] choices;

    // The Gridview that will hold the icons
    ExpandedHeightGridView gridview;

    // Defines which icon is selected
    boolean[] selected;

    // The image views for each of the icons
    View[] imageViews;
    AudioHandler[] audioHandlers;

    // Whether to advance immediately after the image is clicked
    boolean quickAdvance;

    @Nullable
    private AdvanceToNextListener listener;

    int resizeWidth;

    public GridWidget(Context context, FormEntryPrompt prompt, int numColumns,
                      final boolean quickAdvance) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(
                prompt.getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr);
        } else {
            items = prompt.getSelectChoices();
        }

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }

        selected = new boolean[items.size()];
        choices = new String[items.size()];
        gridview = new ExpandedHeightGridView(context);
        imageViews = new View[items.size()];
        audioHandlers = new AudioHandler[items.size()];
        // The max width of an icon in a given column. Used to line
        // up the columns and automatically fit the columns in when
        // they are chosen automatically
        int maxColumnWidth = -1;
        int maxCellHeight = -1;

        for (int i = 0; i < items.size(); i++) {
            imageViews[i] = new ImageView(getContext());
        }
        this.quickAdvance = quickAdvance;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        if (numColumns > 0) {
            resizeWidth = ((screenWidth - 2 * HORIZONTAL_PADDING - SCROLL_WIDTH
                    - (IMAGE_PADDING + SPACING) * (numColumns + 1)) / numColumns);
        }

        if (prompt.isReadOnly()) {
            gridview.setEnabled(false);
        }

        // Build view
        for (int i = 0; i < items.size(); i++) {
            SelectChoice sc = items.get(i);

            int curHeight = -1;

            // Create an audioHandler iff there is an audio prompt associated with this selection.
            String audioURI =
                    prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_AUDIO);
            if (audioURI != null) {
                audioHandlers[i] = new AudioHandler(prompt.getIndex(), sc.getValue(), audioURI,
                        getPlayer());
            } else {
                audioHandlers[i] = null;
            }
            // Read the image sizes and set maxColumnWidth. This allows us to make sure all of our
            // columns are going to fit
            String imageURI;
            if (items.get(i) instanceof ExternalSelectChoice) {
                imageURI = ((ExternalSelectChoice) sc).getImage();
            } else {
                imageURI = prompt.getSpecialFormSelectChoiceText(sc,
                        FormEntryCaption.TEXT_FORM_IMAGE);
            }

            String errorMsg = null;
            if (imageURI != null) {
                choices[i] = imageURI;

                String imageFilename;
                try {
                    imageFilename = ReferenceManager.instance()
                            .DeriveReference(imageURI).getLocalURI();

                    final File imageFile = new File(imageFilename);
                    if (imageFile.exists()) {
                        Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                        if (b != null) {

                            if (b.getWidth() > maxColumnWidth) {
                                maxColumnWidth = b.getWidth();
                            }

                            ImageView imageView = (ImageView) imageViews[i];


                            if (numColumns > 0) {
                                int resizeHeight = (b.getHeight() * resizeWidth) / b.getWidth();
                                b = Bitmap.createScaledBitmap(b, resizeWidth, resizeHeight, false);
                            }

                            imageView.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING,
                                    IMAGE_PADDING);
                            imageView.setImageBitmap(b);
                            imageView.setLayoutParams(
                                    new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
                                            ListView.LayoutParams.WRAP_CONTENT));
                            imageView.setScaleType(ScaleType.FIT_XY);

                            imageView.measure(0, 0);
                            curHeight = imageView.getMeasuredHeight();
                        } else {
                            // Loading the image failed, so it's likely a bad file.
                            errorMsg = getContext().getString(R.string.file_invalid, imageFile);
                        }
                    } else {
                        // We should have an image, but the file doesn't exist.
                        errorMsg = getContext().getString(R.string.file_missing, imageFile);
                    }
                } catch (InvalidReferenceException e) {
                    Timber.e("image invalid reference exception");
                }
            } else {
                errorMsg = "";
            }

            if (errorMsg != null) {
                choices[i] = prompt.getSelectChoiceText(sc);

                TextView missingImage = new TextView(getContext());
                missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
                missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                missingImage.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);

                if (choices[i] != null && choices[i].length() != 0) {
                    missingImage.setText(choices[i]);
                } else {
                    // errorMsg is only set when an error has occurred
                    Timber.e(errorMsg);
                    missingImage.setText(errorMsg);
                }

                if (numColumns > 0) {
                    maxColumnWidth = resizeWidth;
                    // force max width to find needed height...
                    missingImage.setMaxWidth(resizeWidth);
                    missingImage.measure(
                            MeasureSpec.makeMeasureSpec(resizeWidth, MeasureSpec.EXACTLY), 0);
                    curHeight = missingImage.getMeasuredHeight();
                } else {
                    missingImage.measure(0, 0);
                    int width = missingImage.getMeasuredWidth();
                    if (width > maxColumnWidth) {
                        maxColumnWidth = width;
                    }
                    curHeight = missingImage.getMeasuredHeight();
                }
                imageViews[i] = missingImage;
            }

            // if we get a taller image/text, force all cells to be that height
            // could also set cell heights on a per-row basis if user feedback requests it.
            if (curHeight > maxCellHeight) {
                maxCellHeight = curHeight;
                for (int j = 0; j < i; j++) {
                    imageViews[j].setMinimumHeight(maxCellHeight);
                }
            }
            imageViews[i].setMinimumHeight(maxCellHeight);
        }

        // Read the screen dimensions and fit the grid view to them. It is important that the grid
        // knows how far out it can stretch.

        if (numColumns > 0) {
            // gridview.setNumColumns(numColumns);
            gridview.setNumColumns(GridView.AUTO_FIT);
        } else {
            resizeWidth = maxColumnWidth;
            gridview.setNumColumns(GridView.AUTO_FIT);
        }

        gridview.setColumnWidth(resizeWidth);

        gridview.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING,
                VERTICAL_PADDING);
        gridview.setHorizontalSpacing(SPACING);
        gridview.setVerticalSpacing(SPACING);
        gridview.setGravity(Gravity.CENTER);
        gridview.setScrollContainer(false);
        gridview.setStretchMode(GridView.NO_STRETCH);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                // Imitate the behavior of a radio button. Clear all buttons
                // and then check the one clicked by the user. Update the
                // background color accordingly
                for (int i = 0; i < selected.length; i++) {
                    // if we have an audio handler, be sure audio is stopped.
                    if (selected[i] && (audioHandlers[i] != null)) {
                        stopAudio();
                    }
                    selected[i] = false;
                    imageViews[i].setBackgroundColor(0);
                }
                selected[position] = true;
                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                        "onItemClick.select",
                        items.get(position).getValue(), getFormEntryPrompt().getIndex());
                imageViews[position].setBackgroundColor(Color.rgb(ORANGE_RED_VAL, ORANGE_GREEN_VAL,
                        ORANGE_BLUE_VAL));

                if (quickAdvance && listener != null) {
                    listener.advance();

                } else if (audioHandlers[position] != null) {
                    audioHandlers[position].playAudio(getContext());
                }
            }
        });

        // Fill in answer
        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        for (int i = 0; i < items.size(); ++i) {
            String match = items.get(i).getValue();

            selected[i] = match.equals(s);
            if (selected[i]) {
                imageViews[i].setBackgroundColor(Color.rgb(ORANGE_RED_VAL, ORANGE_GREEN_VAL,
                        ORANGE_BLUE_VAL));
            }
        }

        // Use the custom image adapter and initialize the grid view
        ImageAdapter ia = new ImageAdapter(choices);
        gridview.setAdapter(ia);
        addAnswerView(gridview);
    }


    @Override
    public IAnswerData getAnswer() {
        for (int i = 0; i < choices.length; ++i) {
            if (selected[i]) {
                SelectChoice sc = items.get(i);
                return new SelectOneData(new Selection(sc));
            }
        }
        return null;
    }


    @Override
    public void clearAnswer() {
        for (int i = 0; i < items.size(); ++i) {
            selected[i] = false;
            imageViews[i].setBackgroundColor(0);
        }

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        gridview.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        gridview.cancelLongPress();
    }

    @Override
    public int getChoiceCount() {
        return selected.length;
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        for (int i = 0; i < selected.length; i++) {
            selected[i] = false;
        }

        selected[choiceIndex] = isSelected;
    }

    // Custom image adapter. Most of the code is copied from
    // media layout for using a picture.
    private class ImageAdapter extends BaseAdapter {
        private final String[] choices;

        ImageAdapter(String[] choices) {
            this.choices = choices;
        }

        public int getCount() {
            return choices.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < imageViews.length) {
                return imageViews[position];
            } else {
                return convertView;
            }
        }
    }
}
