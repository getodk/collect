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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.SelectWidget;
import org.odk.collect.android.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.odk.collect.android.widgets.QuestionWidget.isRTL;

public abstract class AbstractSelectListAdapter extends RecyclerView.Adapter<AbstractSelectListAdapter.ViewHolder>
        implements Filterable {

    SelectWidget widget;
    List<SelectChoice> items;
    List<SelectChoice> filteredItems;
    boolean noButtonsMode;
    private final int numColumns;
    private final Context context;

    AbstractSelectListAdapter(List<SelectChoice> items, SelectWidget widget, int numColumns) {
        context = widget.getContext();
        this.items = items;
        this.widget = widget;
        filteredItems = items;
        this.numColumns = numColumns;
        noButtonsMode = WidgetAppearanceUtils.isCompactAppearance(widget.getFormEntryPrompt())
                || WidgetAppearanceUtils.isNoButtonsAppearance(widget.getFormEntryPrompt());
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
                    FormEntryPrompt formEntryPrompt = widget.getFormEntryPrompt();
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

    abstract CompoundButton setUpButton(int index);

    void adjustButton(TextView button, int index) {
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        button.setText(FormEntryPromptUtils.getItemText(widget.getFormEntryPrompt(), filteredItems.get(index)));
        button.setTag(items.indexOf(filteredItems.get(index)));
        button.setGravity(isRTL() ? Gravity.END : Gravity.START);
        button.setOnLongClickListener((ODKView) widget.getParent().getParent().getParent());
    }

    View setUpNoButtonsView(int index) {
        View view = new View(context);
        int itemPadding = context.getResources().getDimensionPixelSize(R.dimen.select_item_border);

        SelectChoice selectChoice = filteredItems.get(index);

        String imageURI = selectChoice instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) selectChoice).getImage()
                : widget.getFormEntryPrompt().getSpecialFormSelectChoiceText(selectChoice, FormEntryCaption.TEXT_FORM_IMAGE);

        String errorMsg = null;
        if (imageURI != null) {
            try {
                final File imageFile = new File(ReferenceManager.instance().DeriveReference(imageURI).getLocalURI());
                if (imageFile.exists()) {
                    Bitmap bitmap = FileUtils.getBitmap(imageFile.getPath(), new BitmapFactory.Options());

                    if (bitmap != null) {
                        ImageView imageView = new ImageView(context);
                        imageView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
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
            missingImage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, widget.getAnswerFontSize());
            missingImage.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            missingImage.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);

            String choiceText = FormEntryPromptUtils.getItemText(widget.getFormEntryPrompt(), selectChoice).toString();

            if (!choiceText.isEmpty()) {
                missingImage.setText(choiceText);
            } else {
                Timber.e(errorMsg);
                missingImage.setText(errorMsg);
            }

            view = missingImage;
        }

        view.setOnClickListener(v -> onItemClick(selectChoice.selection(), v));
        view.setEnabled(!widget.getFormEntryPrompt().isReadOnly());
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

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        MediaLayout mediaLayout;
        FrameLayout view;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final int index) {
            if (noButtonsMode) {
                view.removeAllViews();
                view.addView(setUpNoButtonsView(index));
            } else {
                widget.addMediaFromChoice(mediaLayout, index, setUpButton(index), filteredItems);
                mediaLayout.setEnabled(!widget.getFormEntryPrompt().isReadOnly());
            }
        }
    }
}
