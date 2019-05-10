/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.views.AudioButton;
import org.odk.collect.android.views.ExpandedHeightGridView;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.io.File;

import timber.log.Timber;

/**
 * GridWidget handles select-one/multiple fields using a grid options. The number of columns
 * is calculated based on items size.
 */
public abstract class BaseGridWidget extends ItemsWidget implements MultiChoiceWidget {

    final int bgOrange = getResources().getColor(R.color.highContrastHighlight);

    static final int HORIZONTAL_PADDING = 7;
    static final int VERTICAL_PADDING = 5;
    static final int SPACING = 2;
    static final int IMAGE_PADDING = 8;

    int maxColumnWidth;

    boolean quickAdvance;
    boolean[] selected;

    String[] choices;
    View[] imageViews;
    ExpandedHeightGridView gridView;
    AudioButton.AudioHandler[] audioHandlers;

    public BaseGridWidget(Context context, FormEntryPrompt prompt, boolean quickAdvance) {
        super(context, prompt);

        this.quickAdvance = quickAdvance;
        selected = new boolean[items.size()];
        choices = new String[items.size()];
        imageViews = new View[items.size()];
        audioHandlers = new AudioButton.AudioHandler[items.size()];
    }

    void setUpView(FormEntryPrompt prompt) {
        int maxCellHeight = -1;
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();

        for (int i = 0; i < items.size(); i++) {
            SelectChoice sc = items.get(i);
            int curHeight = -1;

            // Create an audioHandler if there is an audio prompt associated with this selection.
            String audioURI = prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_AUDIO);

            audioHandlers[i] = audioURI != null ? new AudioButton.AudioHandler(audioURI, getPlayer()) : null;

            String imageURI = getImageUri(i, sc);
            String errorMsg = null;

            if (imageURI != null) {
                choices[i] = imageURI;

                try {
                    final File imageFile = new File(ReferenceManager.instance().DeriveReference(imageURI).getLocalURI());
                    if (imageFile.exists()) {
                        Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                        if (b != null) {
                            if (b.getWidth() > maxColumnWidth) {
                                maxColumnWidth = b.getWidth();
                            }
                            imageViews[i] = setUpImageView(b);
                            curHeight = imageViews[i].getMeasuredHeight();
                        } else {
                            errorMsg = getContext().getString(R.string.file_invalid, imageFile);
                        }
                    } else {
                        errorMsg = getContext().getString(R.string.file_missing, imageFile);
                    }
                } catch (InvalidReferenceException e) {
                    Timber.e("Image invalid reference due to %s ", e.getMessage());
                }
            } else {
                errorMsg = "";
            }

            if (errorMsg != null) {
                choices[i] = prompt.getSelectChoiceText(sc);
                TextView missingImage = setUpLabelView(i, errorMsg);
                int width = missingImage.getMeasuredWidth();
                if (width > maxColumnWidth) {
                    maxColumnWidth = width;
                }
                curHeight = missingImage.getMeasuredHeight();
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

        setUpGridView();
    }

    void initializeGridView() {
        // Use the custom image adapter and initialize the grid view
        ImageAdapter ia = new ImageAdapter(choices);
        gridView.setAdapter(ia);
        addAnswerView(gridView);
    }

    private String getImageUri(int i, SelectChoice sc) {
        return items.get(i) instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) sc).getImage()
                : getFormEntryPrompt().getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE);
    }

    private ImageView setUpImageView(Bitmap b) {
        ImageView imageView = new ImageView(getContext());
        imageView.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);
        imageView.setImageBitmap(b);
        imageView.setLayoutParams(
                new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
                        ListView.LayoutParams.WRAP_CONTENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.measure(0, 0);

        return imageView;
    }

    private TextView setUpLabelView(int i, String errorMsg) {
        TextView missingImage = new TextView(getContext());
        missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        missingImage.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);

        if (choices[i] != null && choices[i].length() != 0) {
            missingImage.setText(choices[i]);
        } else {
            Timber.e(errorMsg);
            missingImage.setText(errorMsg);
        }
        missingImage.measure(0, 0);

        return missingImage;
    }

    private void setUpGridView() {
        gridView = new ExpandedHeightGridView(getContext());
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setColumnWidth(maxColumnWidth);
        gridView.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING);
        gridView.setHorizontalSpacing(SPACING);
        gridView.setVerticalSpacing(SPACING);
        gridView.setGravity(Gravity.CENTER);
        gridView.setScrollContainer(false);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setOnItemClickListener((parent, v, position, id) -> onElementClick(position));
        gridView.setEnabled(!getFormEntryPrompt().isReadOnly());
    }

    abstract void onElementClick(int position);

    @Override
    public void clearAnswer() {
        for (int i = 0; i < items.size(); ++i) {
            selected[i] = false;
            imageViews[i].setBackgroundColor(0);
        }
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        gridView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        gridView.cancelLongPress();
    }

    @Override
    public int getChoiceCount() {
        return selected.length;
    }

    class ImageAdapter extends BaseAdapter {
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
            return position < imageViews.length ? imageViews[position] : convertView;
        }
    }
}
