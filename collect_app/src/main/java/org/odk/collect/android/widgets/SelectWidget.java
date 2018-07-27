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
import android.media.MediaPlayer;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.views.MediaLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectWidget extends QuestionWidget {
    protected List<SelectChoice> items;
    protected ArrayList<MediaLayout> playList;
    protected LinearLayout answerLayout;
    private int playcounter;

    public SelectWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        answerLayout = new LinearLayout(context);
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        playList = new ArrayList<>();
    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {
    }

    @Override
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
        getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                resetQuestionTextColor();
                mediaPlayer.reset();
                playNextSelectItem();
            }

        });
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
                getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        resetQuestionTextColor();
                        mediaPlayer.reset();
                        playNextSelectItem();
                    }
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

    protected MediaLayout createMediaLayout(int index, TextView textView) {
        String audioURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), FormEntryCaption.TEXT_FORM_AUDIO);

        String imageURI;
        if (items.get(index) instanceof ExternalSelectChoice) {
            imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
        } else {
            imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }

        String videoURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "video");
        String bigImageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index), "big-image");

        MediaLayout mediaLayout = new MediaLayout(getContext());
        mediaLayout.setAVT(getFormEntryPrompt().getIndex(), "." + Integer.toString(index), textView, audioURI,
                imageURI, videoURI, bigImageURI, getPlayer());
        mediaLayout.setAudioListener(this);
        mediaLayout.setPlayTextColor(getPlayColor());
        playList.add(mediaLayout);

        if (index != items.size() - 1) {
            mediaLayout.addDivider();
        }

        return mediaLayout;
    }
}
