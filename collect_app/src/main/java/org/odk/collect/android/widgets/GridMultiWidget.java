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
import java.util.List;

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
import org.odk.collect.android.views.AudioButton.AudioHandler;
import org.odk.collect.android.views.ExpandedHeightGridView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * GridWidget handles multiple selection fields using a grid of icons. The user clicks the desired
 * icon and the background changes from black to orange. If text, audio or video are specified in
 * the select answers they are ignored. This is almost identical to GridWidget, except multiple
 * icons can be selected simultaneously.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
public class GridMultiWidget extends QuestionWidget {

    // The RGB value for the orange background
    public static final int orangeRedVal = 255;
    public static final int orangeGreenVal = 140;
    public static final int orangeBlueVal = 0;

    private static final int HORIZONTAL_PADDING = 7;
    private static final int VERTICAL_PADDING = 5;
    private static final int SPACING = 2;
    private static final int IMAGE_PADDING = 8;
    private static final int SCROLL_WIDTH = 16;

    List<SelectChoice> mItems;

    // The possible select choices
    String[] choices;

    // The Gridview that will hold the icons
    ExpandedHeightGridView gridview;

    // Defines which icon is selected
    boolean[] selected;

    // The image views for each of the icons
    View[] imageViews;
    AudioHandler[] audioHandlers;

    // need to remember the last click position for audio treatment
    int lastClickPosition = 0;

    // The number of columns in the grid, can be user defined (<= 0 if unspecified)
    int numColumns;

    int resizeWidth;

    @SuppressWarnings("unchecked")
    public GridMultiWidget(Context context, FormEntryPrompt prompt, int numColumns) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }
        mPrompt = prompt;

        selected = new boolean[mItems.size()];
        choices = new String[mItems.size()];
        gridview = new ExpandedHeightGridView(context);
        imageViews = new View[mItems.size()];
        audioHandlers = new AudioHandler[mItems.size()];
        // The max width of an icon in a given column. Used to line
        // up the columns and automatically fit the columns in when
        // they are chosen automatically
        int maxColumnWidth = -1;
        int maxCellHeight = -1;
        this.numColumns = numColumns;
        for (int i = 0; i < mItems.size(); i++) {
            imageViews[i] = new ImageView(getContext());
        }

        Display display =
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        if ( display.getOrientation() % 2 == 1 ) {
        	// rotated 90 degrees...
        	int temp = screenWidth;
        	screenWidth = screenHeight;
        	screenHeight = temp;
        }

        if ( numColumns > 0 ) {
        	resizeWidth = ((screenWidth - 2*HORIZONTAL_PADDING - SCROLL_WIDTH - (IMAGE_PADDING+SPACING)*numColumns) / numColumns );
        }

        // Build view
        for (int i = 0; i < mItems.size(); i++) {
            SelectChoice sc = mItems.get(i);

            int curHeight = -1;

            // Create an audioHandler iff there is an audio prompt associated with this selection.
            String audioURI =
            		prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_AUDIO);
            if ( audioURI != null) {
            	audioHandlers[i] = new AudioHandler(prompt.getIndex(), sc.getValue(), audioURI);
            } else {
            	audioHandlers[i] = null;
            }
            // Read the image sizes and set maxColumnWidth. This allows us to make sure all of our
            // columns are going to fit
            String imageURI;
            if (mItems.get(i) instanceof ExternalSelectChoice) {
                imageURI = ((ExternalSelectChoice) sc).getImage();
            } else {
                imageURI = prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE);
            }

            String errorMsg = null;
            if (imageURI != null) {
                choices[i] = imageURI;

                String imageFilename;
                try {
                	imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                    final File imageFile = new File(imageFilename);
                    if (imageFile.exists()) {
                        Bitmap b =
                            FileUtils
                                    .getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                        if (b != null) {

                            if (b.getWidth() > maxColumnWidth) {
                                maxColumnWidth = b.getWidth();
                            }

                            ImageView imageView = (ImageView) imageViews[i];

                            imageView.setBackgroundColor(Color.WHITE);

	                        if ( numColumns > 0 ) {
	                        	int resizeHeight = (b.getHeight() * resizeWidth) / b.getWidth();
	                        	b = Bitmap.createScaledBitmap(b, resizeWidth, resizeHeight, false);
	                        }

	                        imageView.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);
	                        imageView.setImageBitmap(b);
	                        imageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
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
                    Log.e("GridMultiWidget", "image invalid reference exception");
                    e.printStackTrace();
                }
            } else {
            	errorMsg = "";
            }

            if (errorMsg != null) {
                choices[i] = prompt.getSelectChoiceText(sc);

                TextView missingImage = new TextView(getContext());
                missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
                missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                missingImage.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);

                if ( choices[i] != null && choices[i].length() != 0 ) {
	                missingImage.setText(choices[i]);
                } else {
	                // errorMsg is only set when an error has occurred
	                Log.e("GridMultiWidget", errorMsg);
	                missingImage.setText(errorMsg);
                }

                if ( numColumns > 0 ) {
                	maxColumnWidth = resizeWidth;
                	// force the max width to find the needed height...
                	missingImage.setMaxWidth(resizeWidth);
                	missingImage.measure(MeasureSpec.makeMeasureSpec(resizeWidth, MeasureSpec.EXACTLY), 0);
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
            if ( curHeight > maxCellHeight ) {
            	maxCellHeight = curHeight;
            	for ( int j = 0 ; j < i ; j++ ) {
            		imageViews[j].setMinimumHeight(maxCellHeight);
            	}
            }
            imageViews[i].setMinimumHeight(maxCellHeight);
        }

        // Read the screen dimensions and fit the grid view to them. It is important that the grid
        // knows how far out it can stretch.

        if ( numColumns > 0 ) {
            // gridview.setNumColumns(numColumns);
            gridview.setNumColumns(GridView.AUTO_FIT);
        } else {
        	resizeWidth = maxColumnWidth;
            gridview.setNumColumns(GridView.AUTO_FIT);
        }

    	gridview.setColumnWidth(resizeWidth);

    	gridview.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING);
        gridview.setHorizontalSpacing(SPACING);
        gridview.setVerticalSpacing(SPACING);
        gridview.setGravity(Gravity.CENTER);
        gridview.setScrollContainer(false);
        gridview.setStretchMode(GridView.NO_STRETCH);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (selected[position]) {
                    selected[position] = false;
                	if ( audioHandlers[position] != null) {
                		audioHandlers[position].stopPlaying();
                	}
                    imageViews[position].setBackgroundColor(Color.WHITE);
                   	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onItemClick.deselect",
                			mItems.get(position).getValue(), mPrompt.getIndex());

                } else {
                    selected[position] = true;
                	if ( audioHandlers[lastClickPosition] != null) {
                		audioHandlers[lastClickPosition].stopPlaying();
                	}
                    imageViews[position].setBackgroundColor(Color.rgb(orangeRedVal, orangeGreenVal,
                        orangeBlueVal));
                   	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onItemClick.select",
                			mItems.get(position).getValue(), mPrompt.getIndex());
                	if ( audioHandlers[position] != null) {
                		audioHandlers[position].playAudio(getContext());
                	}
            		lastClickPosition = position;
                }

            }
        });

        // Fill in answer
        IAnswerData answer = prompt.getAnswerValue();
        List<Selection> ve;
        if ((answer == null) || (answer.getValue() == null)) {
            ve = new ArrayList<Selection>();
        } else {
            ve = (List<Selection>) answer.getValue();
        }

        for (int i = 0; i < choices.length; ++i) {

            String value = mItems.get(i).getValue();
            boolean found = false;
            for (Selection s : ve) {
                if (value.equals(s.getValue())) {
                    found = true;
                    break;
                }
            }

            selected[i] = found;
            if (selected[i]) {
                imageViews[i].setBackgroundColor(Color.rgb(orangeRedVal, orangeGreenVal,
                    orangeBlueVal));
            } else {
                imageViews[i].setBackgroundColor(Color.WHITE);
            }
        }

        // Use the custom image adapter and initialize the grid view
        ImageAdapter ia = new ImageAdapter(getContext(), choices);
        gridview.setAdapter(ia);
        addView(gridview,  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }


    @Override
    public IAnswerData getAnswer() {
        List<Selection> vc = new ArrayList<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            if (selected[i]) {
                SelectChoice sc = mItems.get(i);
                vc.add(new Selection(sc));
            }
        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }
    }


    @Override
    public void clearAnswer() {
        for (int i = 0; i < mItems.size(); ++i) {
            selected[i] = false;
            imageViews[i].setBackgroundColor(Color.WHITE);
        }

    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }

    // Custom image adapter. Most of the code is copied from
    // media layout for using a picture.
    private class ImageAdapter extends BaseAdapter {
        private String[] choices;


        public ImageAdapter(Context c, String[] choices) {
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
           	if ( position < imageViews.length ) {
        		return imageViews[position];
        	} else {
        		return convertView;
        	}
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
}
