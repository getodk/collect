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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.views.MediaLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectWidget extends QuestionWidget {

    /**
     * A list of choices can have thousands of items. To increase loading and scrolling performance,
     * a RecyclerView is used. Because it is nested inside a ScrollView, by default, all of
     * the RecyclerView's items are loaded and there is no performance benefit over a ListView.
     * This constant is used to bound the number of items loaded. The value 40 was chosen because
     * it is around the maximum number of elements that can be shown on a large tablet.
     */
    private static final int MAX_ITEMS_WITHOUT_SCREEN_BOUND = 40;

    protected List<SelectChoice> items;
    protected ArrayList<MediaLayout> playList;
    protected LinearLayout answerLayout;
    private int playcounter;
    private boolean isAutoPlayEnabled;

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
    protected void mediaPrepared() {
        super.mediaPrepared();

        for (MediaLayout mediaLayout : playList) {
            if (!mediaLayout.isAudioPlaying()) {
                mediaLayout.resetUI();
            }
        }
    }

    @Override
    protected void mediaCompleted() {
        super.mediaCompleted();

        for (MediaLayout mediaLayout : playList) {
            if (mediaLayout.isAudioPlaying()) {
                mediaLayout.resetUI();
            }
        }

        if (isAutoPlayEnabled && playcounter < playList.size()) {
            playNextSelectItem();
        }
    }

    @Override
    public void playAllPromptText() {
        // set up to play the items when the question text is finished
        isAutoPlayEnabled = true;

        // plays the question text
        super.playAllPromptText();
    }

    protected void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }

    private void playNextSelectItem() {
        if (isShown()) {
            // if there's more, set up to play the next item
            if (playcounter < playList.size()) {
                // play the current item
                playList.get(playcounter).playAudio();
                playcounter++;
            } else {
                playcounter = 0;
            }
        }
    }

    public void initMediaLayoutSetUp(MediaLayout mediaLayout) {
        mediaLayout.setPlayTextColor(getPlayColor());
        playList.add(mediaLayout);
    }

    /**
     * Pull media from the current item and add it to the media layout.
     */
    public void addMediaFromChoice(MediaLayout mediaLayout, int index, TextView textView) {
        String audioUri = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), FormEntryCaption.TEXT_FORM_AUDIO);

        String imageUri;
        if (items.get(index) instanceof ExternalSelectChoice) {
            imageUri = ((ExternalSelectChoice) items.get(index)).getImage();
        } else {
            imageUri = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }

        textView.setGravity(Gravity.CENTER_VERTICAL);

        String videoUri = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "video");
        String bigImageUri = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "big-image");

        mediaLayout.setAVT(textView, audioUri, imageUri, videoUri, bigImageUri);
    }

    protected RecyclerView setUpRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.recycler_view, null); // keep in an xml file to enable the vertical scrollbar
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

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
