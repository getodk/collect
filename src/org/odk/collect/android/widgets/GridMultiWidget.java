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
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.AudioButton.AudioHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
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
    Vector<SelectChoice> mItems;

    // The possible select choices
    String[] choices;

    // The Gridview that will hol the icons
    GridView gridview;

    // Defines which icon is selected
    boolean[] selected;

    // The image views for each of the icons
    ImageView[] imageViews;
    AudioHandler[] audioHandlers;

    // need to remember the last click position for audio treatment
    int lastClickPosition = 0;
    
    // The number of columns in the grid, can be user defined
    int numColumns;

    // The max width of an icon in a given column. Used to line
    // up the columns and automatically fit the columns in when
    // they are chosen automatically
    int maxColumnWidth;

    // The RGB value for the orange background
    public static final int orangeRedVal = 255;
    public static final int orangeGreenVal = 140;
    public static final int orangeBlueVal = 0;


    @SuppressWarnings("unchecked")
    public GridMultiWidget(Context context, FormEntryPrompt prompt, int numColumns) {
        super(context, prompt);
        mItems = prompt.getSelectChoices();
        mPrompt = prompt;

        selected = new boolean[mItems.size()];
        choices = new String[mItems.size()];
        gridview = new GridView(context);
        imageViews = new ImageView[mItems.size()];
        audioHandlers = new AudioHandler[mItems.size()];
        maxColumnWidth = -1;
        this.numColumns = numColumns;
        for (int i = 0; i < mItems.size(); i++) {
            imageViews[i] = new ImageView(getContext());
        }

        // Build view
        for (int i = 0; i < mItems.size(); i++) {
            SelectChoice sc = mItems.get(i);
            
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
            String imageURI =
                prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE);

            if (imageURI != null) {
                choices[i] = imageURI;

                String imageFilename;
                try {
                    imageFilename = ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                    final File imageFile = new File(imageFilename);
                    if (imageFile.exists()) {
                        Display display =
                            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay();
                        int screenWidth = display.getWidth();
                        int screenHeight = display.getHeight();
                        Bitmap b =
                            FileUtils
                                    .getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                        if (b != null) {

                            if (b.getWidth() > maxColumnWidth) {
                                maxColumnWidth = b.getWidth();
                            }

                        }
                    }
                } catch (InvalidReferenceException e) {
                    Log.e("GridWidget", "image invalid reference exception");
                    e.printStackTrace();
                }

            } else {
                choices[i] = prompt.getSelectChoiceText(sc);
            }

        }

        // Use the custom image adapter and initialize the grid view
        ImageAdapter ia = new ImageAdapter(getContext(), choices);
        gridview.setAdapter(ia);
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

        // Read the screen dimensions and fit the grid view to them. It is important that the grid
        // view
        // knows how far out it can stretch.
        Display display =
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        GridView.LayoutParams params = new GridView.LayoutParams(screenWidth - 5, screenHeight - 5);
        gridview.setLayoutParams(params);

        // Use the user's choice for num columns, otherwise automatically decide.
        if (numColumns > 0) {
            gridview.setNumColumns(numColumns);
        } else {
            gridview.setNumColumns(GridView.AUTO_FIT);
        }

        gridview.setColumnWidth(maxColumnWidth);
        gridview.setHorizontalSpacing(2);
        gridview.setVerticalSpacing(2);
        gridview.setGravity(Gravity.LEFT);
        gridview.setStretchMode(GridView.NO_STRETCH);

        // Fill in answer
        IAnswerData answer = prompt.getAnswerValue();
        Vector<Selection> ve;
        if ((answer == null) || (answer.getValue() == null)) {
            ve = new Vector<Selection>();
        } else {
            ve = (Vector<Selection>) answer.getValue();
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

        addView(gridview);
    }


    @Override
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
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
            String imageURI = choices[position];

            // It is possible that an imageview already exists and has been updated
            // by updateViewAfterAnswer
            ImageView mImageView = null;
            if (imageViews[position] != null) {
                mImageView = imageViews[position];
            }
            TextView mMissingImage = null;

            String errorMsg = null;
            if (imageURI != null) {
                try {
                    String imageFilename =
                        ReferenceManager._().DeriveReference(imageURI).getLocalURI();
                    final File imageFile = new File(imageFilename);
                    if (imageFile.exists()) {
                        Display display =
                            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay();
                        int screenWidth = display.getWidth();
                        int screenHeight = display.getHeight();
                        Bitmap b =
                            FileUtils
                                    .getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                        if (b != null) {

                            if (mImageView == null) {
                                mImageView = new ImageView(getContext());
                                mImageView.setBackgroundColor(Color.WHITE);
                            }

                            mImageView.setPadding(3, 3, 3, 3);
                            mImageView.setImageBitmap(b);

                            imageViews[position] = mImageView;

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
                        Log.e("GridWidget", errorMsg);
                        mMissingImage = new TextView(getContext());
                        mMissingImage.setText(errorMsg);
                        mMissingImage.setPadding(10, 10, 10, 10);
                    }
                } catch (InvalidReferenceException e) {
                    Log.e("GridWidget", "image invalid reference exception");
                    e.printStackTrace();
                }
            } else {
                // There's no imageURI listed, so just ignore it.
            }

            if (mImageView != null) {
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return mImageView;
            } else {
                return mMissingImage;
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
