/*
 * Copyright 2017 Nafundi
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.MediaLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectWidget extends ItemsWidget {

    /**
     * A list of choices can have thousands of items. To increase loading and scrolling performance,
     * a RecyclerView is used. Because it is nested inside a ScrollView, by default, all of
     * the RecyclerView's items are loaded and there is no performance benefit over a ListView.
     * This constant is used to bound the number of items loaded. The value 40 was chosen because
     * it is around the maximum number of elements that can be shown on a large tablet.
     */
    private static final int MAX_ITEMS_WITHOUT_SCREEN_BOUND = 40;

    protected ArrayList<MediaLayout> playList;
    protected LinearLayout answerLayout;
    protected int numColumns = 1;
    private int playcounter;

    public SelectWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        answerLayout = new LinearLayout(context);
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        playList = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public void resetQuestionTextColor() {
        super.resetQuestionTextColor();
        for (MediaLayout layout : playList) {
            layout.resetTextFormatting();
        }
    }

    @Override
    public void resetAudioButtonImage() {
        super.resetAudioButtonImage();
        for (MediaLayout layout : playList) {
            layout.resetAudioButtonBitmap();
        }
    }

    @Override
    public void playAllPromptText() {
        // set up to play the items when the
        // question text is finished
        getPlayer().setOnCompletionListener(mediaPlayer -> {
            resetQuestionTextColor();
            mediaPlayer.reset();
            playNextSelectItem();
        });
        // plays the question text
        super.playAllPromptText();
    }

    private void playNextSelectItem() {
        if (isShown()) {
            // if there's more, set up to play the next item
            if (playcounter < playList.size()) {
                getPlayer().setOnCompletionListener(mediaPlayer -> {
                    resetQuestionTextColor();
                    mediaPlayer.reset();
                    playNextSelectItem();
                });
                // play the current item
                playList.get(playcounter).playAudio();
                playcounter++;

            } else {
                playcounter = 0;
                getPlayer().setOnCompletionListener(null);
                getPlayer().reset();
            }
        }
    }

    public void initMediaLayoutSetUp(MediaLayout mediaLayout) {
        mediaLayout.setAudioListener(this);
        mediaLayout.setPlayTextColor(getPlayColor());
        playList.add(mediaLayout);
    }

    /**
     * Pull media from the current item and add it to the media layout.
     */
    public void addMediaFromChoice(MediaLayout mediaLayout, int index, TextView textView, List<SelectChoice> items) {
        String audioURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), FormEntryCaption.TEXT_FORM_AUDIO);

        String imageURI;
        if (items.get(index) instanceof ExternalSelectChoice) {
            imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
        } else {
            imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }

        textView.setGravity(Gravity.CENTER_VERTICAL);

        String videoURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "video");
        String bigImageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "big-image");

        mediaLayout.setAVT(textView, audioURI, imageURI, videoURI, bigImageURI, getPlayer());
    }

    protected RecyclerView setUpRecyclerView() {
        numColumns = WidgetAppearanceUtils.getNumberOfColumns(getFormEntryPrompt(), getContext());

        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.recycler_view, null); // keep in an xml file to enable the vertical scrollbar
        if (numColumns == 1) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numColumns));

        return recyclerView;
    }

    void adjustRecyclerViewSize(AbstractSelectListAdapter adapter, RecyclerView recyclerView) {
        if (adapter.getItemCount() > MAX_ITEMS_WITHOUT_SCREEN_BOUND) {
            // Only let the RecyclerView take up 90% of the screen height in order to speed up loading if there are many items
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((FormEntryActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            recyclerView.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.9);
        } else {
            recyclerView.setNestedScrollingEnabled(false);
        }
    }
}
