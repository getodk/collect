/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.SelectWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getClipID;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;
import static org.odk.collect.android.widgets.QuestionWidget.isRTL;

public abstract class AbstractSelectListAdapter extends RecyclerView.Adapter<AbstractSelectListAdapter.ViewHolder>
        implements Filterable {

    private final FormEntryPrompt prompt;
    private final ReferenceManager referenceManager;
    private final int numColumns;
    private final Context context;
    private final int answerFontSize;
    private final AudioHelper audioHelper;
    List<SelectChoice> items;
    List<SelectChoice> filteredItems;
    boolean noButtonsMode;

    /**
     * This creates a circular dependency between this class and {@link SelectWidget}. Dependencies
     * for this class should be passed in at the constructor or setter level. Method calls back to
     * {@link SelectWidget} can be replaced with listeners.
     */
    @Deprecated
    SelectWidget widget;

    AbstractSelectListAdapter(List<SelectChoice> items, SelectWidget widget, int numColumns, FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager, int answerFontSize, AudioHelper audioHelper, Context context) {
        this.context = context;
        this.items = items;
        this.widget = widget;
        this.prompt = formEntryPrompt;
        this.referenceManager = referenceManager;
        this.answerFontSize = answerFontSize;
        this.audioHelper = audioHelper;
        filteredItems = items;
        this.numColumns = numColumns;
        noButtonsMode = WidgetAppearanceUtils.isCompactAppearance(getFormEntryPrompt())
                || WidgetAppearanceUtils.isNoButtonsAppearance(getFormEntryPrompt());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        holder.bind(index);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchStr = charSequence.toString().toLowerCase(Locale.US);
                FilterResults filterResults = new FilterResults();
                if (searchStr.isEmpty()) {
                    filterResults.values = items;
                    filterResults.count = items.size();
                } else {
                    List<SelectChoice> filteredList = new ArrayList<>();
                    FormEntryPrompt formEntryPrompt = getFormEntryPrompt();
                    for (SelectChoice item : items) {
                        if (formEntryPrompt.getSelectChoiceText(item).toLowerCase(Locale.US).contains(searchStr)) {
                            filteredList.add(item);
                        }
                    }
                    filterResults.values = filteredList;
                    filterResults.count = filteredList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItems = (List<SelectChoice>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    abstract CompoundButton createButton(int index, ViewGroup parent);

    void setUpButton(TextView button, int index) {
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        button.setText(FormEntryPromptUtils.getItemText(getFormEntryPrompt(), filteredItems.get(index)));
        button.setTag(items.indexOf(filteredItems.get(index)));
        button.setGravity(isRTL() ? Gravity.END : Gravity.START);
        button.setOnLongClickListener(getODKViewParent(widget));
    }

    View setUpNoButtonsView(int index) {
        View view = new View(context);

        SelectChoice selectChoice = filteredItems.get(index);

        String imageURI = selectChoice instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) selectChoice).getImage()
                : getFormEntryPrompt().getSpecialFormSelectChoiceText(selectChoice, FormEntryCaption.TEXT_FORM_IMAGE);

        String errorMsg = null;
        if (imageURI != null) {
            try {
                final File imageFile = new File(ReferenceManager.instance().deriveReference(imageURI).getLocalURI());
                if (imageFile.exists()) {
                    Bitmap bitmap = FileUtils.getBitmap(imageFile.getPath(), new BitmapFactory.Options());

                    if (bitmap != null) {
                        ImageView imageView = new ImageView(context);
                        imageView.setImageBitmap(ImageConverter.scaleImageToNewWidth(bitmap, context.getResources().getDisplayMetrics().widthPixels / numColumns));
                        imageView.setAdjustViewBounds(true);
                        view = imageView;
                    } else {
                        errorMsg = context.getString(R.string.file_invalid, imageFile);
                    }
                } else {
                    errorMsg = context.getString(R.string.file_missing, imageFile);
                }
            } catch (InvalidReferenceException e) {
                Timber.e("Image invalid reference due to %s ", e.getMessage());
            }
        } else {
            errorMsg = "";
        }

        if (errorMsg != null) {
            TextView missingImage = new TextView(context);
            missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
            missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

            String choiceText = FormEntryPromptUtils.getItemText(getFormEntryPrompt(), selectChoice).toString();

            if (!choiceText.isEmpty()) {
                missingImage.setText(choiceText);
            } else {
                Timber.e(errorMsg);
                missingImage.setText(errorMsg);
            }

            view = missingImage;
        }

        int itemPadding = context.getResources().getDimensionPixelSize(R.dimen.select_item_border);
        int paddingStartEnd = context.getResources().getDimensionPixelSize(R.dimen.margin_standard);
        view.setPadding(paddingStartEnd, itemPadding, paddingStartEnd, itemPadding);

        return view;
    }

    boolean isItemSelected(List<Selection> selectedItems, @NonNull Selection item) {
        for (Selection selectedItem : selectedItems) {
            if (item.getValue().equalsIgnoreCase(selectedItem.getValue())) {
                return true;
            }
        }
        return false;
    }

    private ODKView getODKViewParent(ViewParent view) {
        ViewParent parent = view.getParent();

        if (parent != null) {
            return getODKViewParent(parent);
        } else {
            return null;
        }
    }

    abstract void onItemClick(Selection selection, View view);

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        AudioVideoImageTextLabel audioVideoImageTextLabel;
        FrameLayout view;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final int index) {
            if (noButtonsMode) {
                view.removeAllViews();
                view.addView(setUpNoButtonsView(index));
                view.setOnClickListener(v -> onItemClick(filteredItems.get(index).selection(), v));
                view.setEnabled(!getFormEntryPrompt().isReadOnly());
            } else {
                addMediaFromChoice(audioVideoImageTextLabel, index, createButton(index, audioVideoImageTextLabel), filteredItems);
                audioVideoImageTextLabel.setEnabled(!getFormEntryPrompt().isReadOnly());
            }
        }

        /**
         * Pull media from the current item and add it to the media layout.
         */
        public void addMediaFromChoice(AudioVideoImageTextLabel audioVideoImageTextLabel, int index, TextView textView, List<SelectChoice> items) {
            SelectChoice item = items.get(index);

            audioVideoImageTextLabel.setTag(getClipID(getFormEntryPrompt(), item));
            audioVideoImageTextLabel.setTextView(textView);

            String imageURI = getImageURI(index, items);
            String videoURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(item, "video");
            String bigImageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(item, "big-image");
            audioVideoImageTextLabel.setImageVideo(imageURI, videoURI, bigImageURI, getReferenceManager());

            String audioURI = getPlayableAudioURI(getFormEntryPrompt(), item, getReferenceManager());
            if (audioURI != null) {
                audioVideoImageTextLabel.setAudio(audioURI, getAudioHelper());
            }

            textView.setGravity(Gravity.CENTER_VERTICAL);
        }

        private String getImageURI(int index, List<SelectChoice> items) {
            String imageURI;
            if (items.get(index) instanceof ExternalSelectChoice) {
                imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
            } else {
                imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                        FormEntryCaption.TEXT_FORM_IMAGE);
            }
            return imageURI;
        }
    }

    private AudioHelper getAudioHelper() {
        return audioHelper;
    }

    private ReferenceManager getReferenceManager() {
        return referenceManager;
    }

    private FormEntryPrompt getFormEntryPrompt() {
        return prompt;
    }

    private int getAnswerFontSize() {
        return answerFontSize;
    }
}
