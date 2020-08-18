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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

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

    protected ChoicesRecyclerViewAdapterProps props;

    AbstractSelectListAdapter(ChoicesRecyclerViewAdapterProps props) {
        this.props = props;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        holder.bind(index);
    }

    @Override
    public int getItemCount() {
        return props.getFilteredItems().size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchStr = charSequence.toString().toLowerCase(Locale.US);
                FilterResults filterResults = new FilterResults();
                if (searchStr.isEmpty()) {
                    filterResults.values = props.getItems();
                    filterResults.count = props.getItems().size();
                } else {
                    List<SelectChoice> filteredList = new ArrayList<>();
                    for (SelectChoice item : props.getItems()) {
                        if (props.getPrompt().getSelectChoiceText(item).toLowerCase(Locale.US).contains(searchStr)) {
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
                props.setFilteredItems((List<SelectChoice>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    abstract CompoundButton createButton(int index, ViewGroup parent);

    void setUpButton(TextView button, int index) {
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
        button.setText(StringUtils.textToHtml(props.getPrompt().getSelectChoiceText(props.getFilteredItems().get(index))));
        button.setTag(props.getItems().indexOf(props.getFilteredItems().get(index)));
        button.setGravity(isRTL() ? Gravity.END : Gravity.START);
        button.setTextAlignment(isRTL() ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);
    }

    View setUpNoButtonsView(int index) {
        View view = new View(props.getContext());

        SelectChoice selectChoice = props.getFilteredItems().get(index);

        String imageURI = selectChoice instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) selectChoice).getImage()
                : props.getPrompt().getSpecialFormSelectChoiceText(selectChoice, FormEntryCaption.TEXT_FORM_IMAGE);

        String errorMsg = null;
        if (imageURI != null) {
            try {
                final File imageFile = new File(ReferenceManager.instance().deriveReference(imageURI).getLocalURI());
                if (imageFile.exists()) {
                    Bitmap bitmap = FileUtils.getBitmap(imageFile.getPath(), new BitmapFactory.Options());

                    if (bitmap != null) {
                        ImageView imageView = new ImageView(props.getContext());
                        if (!WidgetAppearanceUtils.isFlexAppearance(props.getPrompt())) {
                            bitmap = ImageConverter.scaleImageToNewWidth(bitmap, props.getContext().getResources().getDisplayMetrics().widthPixels / props.getNumColumns());
                        }
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                        view = imageView;
                    } else {
                        errorMsg = props.getContext().getString(R.string.file_invalid, imageFile);
                    }
                } else {
                    errorMsg = props.getContext().getString(R.string.file_missing, imageFile);
                }
            } catch (InvalidReferenceException e) {
                Timber.e("Image invalid reference due to %s ", e.getMessage());
            }
        } else {
            errorMsg = "";
        }

        if (errorMsg != null) {
            TextView missingImage = new TextView(props.getContext());
            missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            String choiceText = StringUtils.textToHtml(props.getPrompt().getSelectChoiceText(selectChoice)).toString();

            if (!choiceText.isEmpty()) {
                missingImage.setText(choiceText);
            } else {
                Timber.e(errorMsg);
                missingImage.setText(errorMsg);
            }

            missingImage.setId(R.id.text_label);
            view = missingImage;
        }

        int itemPadding = props.getContext().getResources().getDimensionPixelSize(R.dimen.select_item_border);
        int paddingStart = props.getContext().getResources().getDimensionPixelSize(R.dimen.margin_standard);

        view.setPadding(paddingStart, itemPadding, itemPadding, itemPadding);

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

    abstract void onItemClick(Selection selection, View view);

    public abstract List<Selection> getSelectedItems();

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        AudioVideoImageTextLabel audioVideoImageTextLabel;
        FrameLayout view;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final int index) {
            if (props.isNoButtonsMode()) {
                view.removeAllViews();
                view.addView(setUpNoButtonsView(index));
                view.setOnClickListener(v -> onItemClick(props.getFilteredItems().get(index).selection(), v));
                view.setEnabled(!props.getPrompt().isReadOnly());
                view.setLongClickable(true);
            } else {
                addMediaFromChoice(audioVideoImageTextLabel, index, createButton(index, audioVideoImageTextLabel), props.getFilteredItems());
                audioVideoImageTextLabel.setEnabled(!props.getPrompt().isReadOnly());
                audioVideoImageTextLabel.setLongClickable(true);
            }
        }

        /**
         * Pull media from the current item and add it to the media layout.
         */
        public void addMediaFromChoice(AudioVideoImageTextLabel audioVideoImageTextLabel, int index, TextView textView, List<SelectChoice> items) {
            SelectChoice item = items.get(index);

            audioVideoImageTextLabel.setTag(getClipID(props.getPrompt(), item));
            audioVideoImageTextLabel.setTextView(textView);

            String imageURI = getImageURI(index, items);
            String videoURI = props.getPrompt().getSpecialFormSelectChoiceText(item, "video");
            String bigImageURI = props.getPrompt().getSpecialFormSelectChoiceText(item, "big-image");
            String audioURI = getPlayableAudioURI(props.getPrompt(), item, props.getReferenceManager());
            try {
                if (imageURI != null) {
                    audioVideoImageTextLabel.setImage(new File(props.getReferenceManager().deriveReference(imageURI).getLocalURI()));
                }
                if (bigImageURI != null) {
                    audioVideoImageTextLabel.setBigImage(new File(props.getReferenceManager().deriveReference(bigImageURI).getLocalURI()));
                }
                if (videoURI != null) {
                    audioVideoImageTextLabel.setVideo(new File(props.getReferenceManager().deriveReference(videoURI).getLocalURI()));
                }
                if (audioURI != null) {
                    audioVideoImageTextLabel.setAudio(audioURI, props.getAudioHelper());
                }
            } catch (InvalidReferenceException e) {
                Timber.d(e, "Invalid media reference due to %s ", e.getMessage());
            }

            textView.setGravity(Gravity.CENTER_VERTICAL);
        }

        private String getImageURI(int index, List<SelectChoice> items) {
            String imageURI;
            if (items.get(index) instanceof ExternalSelectChoice) {
                imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
            } else {
                imageURI = props.getPrompt().getSpecialFormSelectChoiceText(items.get(index),
                        FormEntryCaption.TEXT_FORM_IMAGE);
            }
            return imageURI;
        }

        void adjustAudioVideoImageTextLabelParams() {
            if (WidgetAppearanceUtils.isFlexAppearance(props.getPrompt())) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                audioVideoImageTextLabel.findViewById(R.id.audio_video_image_text_label_container).setLayoutParams(params);
                audioVideoImageTextLabel.findViewById(R.id.image_text_label_container).setLayoutParams(params);
            }
        }
    }

    public int getNumColumns() {
        return props.getNumColumns();
    }

    public abstract void clearAnswer();

    // Just for tests
    public ChoicesRecyclerViewAdapterProps getProps() {
        return props;
    }
}
