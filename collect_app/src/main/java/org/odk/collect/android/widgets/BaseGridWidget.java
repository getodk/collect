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
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.AudioButton;
import org.odk.collect.android.views.ExpandedHeightGridView;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * GridWidget handles select-one/multiple fields using a grid options. The number of columns
 * is calculated based on items size.
 */
public abstract class BaseGridWidget extends ItemsWidget implements MultiChoiceWidget {

    final int bgOrange = getResources().getColor(R.color.highContrastHighlight);

    private static final int PADDING = 7;
    private static final int SPACING = 2;

    private int maxColumnWidth;
    private int maxCellHeight;

    boolean noButtonsMode;
    boolean quickAdvance;

    List<Integer> selectedItems = new ArrayList<>();
    View[] itemViews;
    AudioButton.AudioHandler[] audioHandlers;

    public BaseGridWidget(Context context, FormEntryPrompt prompt, boolean quickAdvance) {
        super(context, prompt);

        this.quickAdvance = quickAdvance;
        noButtonsMode = WidgetAppearanceUtils.isCompactAppearance(prompt) || WidgetAppearanceUtils.isNoButtonsAppearance(prompt);
        itemViews = new View[items.size()];
        audioHandlers = new AudioButton.AudioHandler[items.size()];

        setUpItems();
        setUpGridView();
        fillInAnswer();
    }

    private void setUpItems() {
        for (int i = 0; i < items.size(); i++) {
            setUpAudioHandler(i);
            View view = measureItem(noButtonsMode ? setUpNoButtonsItem(i) : setUpButtonsItem(i), i);
            int index = i;
            view.setOnClickListener(v -> onItemClick(index));
            view.setEnabled(!getFormEntryPrompt().isReadOnly());
            itemViews[i] = view;
        }
    }

    private View measureItem(View item, int index) {
        item.measure(0, 0);
        if (!(item instanceof ImageView)) {
            if (item.getMeasuredWidth() > maxColumnWidth) {
                maxColumnWidth = item.getMeasuredWidth();
            }
        }
        if (item.getMeasuredHeight() > maxCellHeight) {
            maxCellHeight = item.getMeasuredHeight();
            for (int j = 0; j < index; j++) {
                itemViews[j].setMinimumHeight(maxCellHeight);
            }
        }
        item.setMinimumHeight(maxCellHeight);
        return item;
    }

    private void setUpAudioHandler(int index) {
        // Create an audioHandler if there is an audio prompt associated with this selection.
        String audioURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), FormEntryCaption.TEXT_FORM_AUDIO);
        audioHandlers[index] = audioURI != null ? new AudioButton.AudioHandler(audioURI, getPlayer()) : null;
    }

    private View setUpNoButtonsItem(int index) {
        View item = null;
        String imageURI = getImageUri(index);
        String errorMsg = null;
        if (imageURI != null) {
            try {
                final File imageFile = new File(ReferenceManager.instance().DeriveReference(imageURI).getLocalURI());
                if (imageFile.exists()) {
                    Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
                    if (b != null) {
                        maxColumnWidth = b.getWidth() > maxColumnWidth ? b.getWidth() : maxColumnWidth;
                        item = setUpImageItem(b);
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

        return errorMsg == null ? item : setUpLabelItem(errorMsg, index);
    }

    private TextView setUpButtonsItem(int index) {
        TextView item = this instanceof GridWidget
                ? new AppCompatRadioButton(getContext())
                : new AppCompatCheckBox(getContext());

        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        item.setText(FormEntryPromptUtils.getItemText(getFormEntryPrompt(), items.get(index)));
        item.setTag(items.indexOf(items.get(index)));
        item.setGravity(isRTL() ? Gravity.END : Gravity.START);
        return item;
    }

    private String getImageUri(int index) {
        return items.get(index) instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) items.get(index)).getImage()
                : getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), FormEntryCaption.TEXT_FORM_IMAGE);
    }

    private ImageView setUpImageItem(Bitmap image) {
        ImageView item = new ImageView(getContext());
        item.setPadding(PADDING, PADDING, PADDING, PADDING);
        item.setImageBitmap(image);
        item.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
        item.setScaleType(ImageView.ScaleType.FIT_XY);
        return item;
    }

    private TextView setUpLabelItem(String errorMsg, int index) {
        CharSequence choice = FormEntryPromptUtils.getItemText(getFormEntryPrompt(), items.get(index));
        TextView item = new TextView(getContext());
        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        item.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        item.setPadding(PADDING, PADDING, PADDING, PADDING);
        item.setText(choice != null && choice.length() > 0 ? choice : errorMsg);
        return item;
    }

    private void setUpGridView() {
        ExpandedHeightGridView gridView = new ExpandedHeightGridView(getContext());
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setColumnWidth(maxColumnWidth);
        gridView.setPadding(PADDING, PADDING, PADDING, PADDING);
        gridView.setHorizontalSpacing(SPACING);
        gridView.setVerticalSpacing(SPACING);
        gridView.setGravity(Gravity.CENTER);
        gridView.setScrollContainer(false);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setAdapter(new ImageAdapter());
        addAnswerView(gridView);
    }

    void selectItem(int index) {
        selectedItems.add(index);
        if (noButtonsMode) {
            itemViews[index].setBackgroundColor(bgOrange);
        } else {
            ((CompoundButton) itemViews[index]).setChecked(true);
        }
    }

    void unselectItem(int index) {
        selectedItems.remove(Integer.valueOf(index));
        if (noButtonsMode) {
            itemViews[index].setBackgroundColor(0);
        } else {
            ((CompoundButton) itemViews[index]).setChecked(false);
        }
    }

    protected abstract void fillInAnswer();

    protected abstract void onItemClick(int position);

    @Override
    public void clearAnswer() {
        for (int selectedItem : selectedItems) {
            if (noButtonsMode) {
                itemViews[selectedItem].setBackgroundColor(0);
            } else {
                ((CompoundButton) itemViews[selectedItem]).setChecked(false);
            }
        }
        selectedItems.clear();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (View item : itemViews) {
            item.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (View item : itemViews) {
            item.cancelLongPress();
        }
    }

    @Override
    public int getChoiceCount() {
        return selectedItems.size();
    }

    class ImageAdapter extends BaseAdapter {
        public int getCount() {
            return itemViews.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return position < itemViews.length ? itemViews[position] : convertView;
        }
    }
}
