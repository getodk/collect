/*
 * Copyright (C) 2018 Akshay Patel
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RatingBar;

import androidx.core.content.ContextCompat;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.RatingWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.UiUtils;

@SuppressLint("ViewConstructor")
public class RatingWidget extends QuestionWidget {

    public static final int ASSUMED_TOTAL_MARGIN_AROUND_WIDGET = 40;

    private RatingBar ratingBar;

    public RatingWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        RatingWidgetAnswerBinding binding = RatingWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();


        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();
        int numberOfStars = rangeQuestion.getRangeEnd().intValue();
        int columns = calculateColumns(48);

        int rows = (int) Math.ceil((double) numberOfStars / columns);

        if (rows == 1) {
            binding.ratingBar.setVisibility(VISIBLE);
            this.ratingBar = binding.ratingBar;
        } else {
            binding.ratingBar.setVisibility(GONE);
            columns = calculateColumns(36);
            rows = (int) Math.ceil((double) numberOfStars / columns);
            if (rows == 1) {
                binding.ratingBar2.setVisibility(VISIBLE);
                this.ratingBar = binding.ratingBar2;
            } else {
                binding.ratingBar3.setVisibility(VISIBLE);
                this.ratingBar = binding.ratingBar3;
            }
        }

        ratingBar.setNumStars(rangeQuestion.getRangeEnd().intValue());
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> widgetValueChanged());
        ratingBar.setEnabled(!prompt.isReadOnly());

        if (prompt.getAnswerText() != null) {
            ratingBar.setRating(Integer.parseInt(prompt.getAnswerText()));
        }
        return answerView;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        ratingBar.setOnLongClickListener(l);
    }

    @Override
    public IAnswerData getAnswer() {
        return ratingBar.getRating() == 0.0F ? null : new IntegerData((int) ratingBar.getRating());
    }

    @Override
    public void clearAnswer() {
        ratingBar.setRating(0.0F);
    }

    protected RatingBar getRatingBar() {
        return ratingBar;
    }

    private int calculateColumns(int widthOfStar) {
        return (ScreenUtils.getScreenWidth() - ASSUMED_TOTAL_MARGIN_AROUND_WIDGET) /
                ((int) UiUtils.convertDpToPixel(widthOfStar, getContext()));
    }
}
