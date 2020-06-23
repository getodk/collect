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
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import androidx.core.content.ContextCompat;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.RatingWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;

@SuppressLint("ViewConstructor")
public class RatingWidget extends QuestionWidget {

    public static final int ASSUMED_TOTAL_MARGIN_AROUND_WIDGET = 40;

    private RatingWidgetAnswerBinding binding;
    private int numberOfStars;

    Integer answer;

    public RatingWidget(Context context, QuestionDetails questionDetails, RangeQuestion rangeQuestion) {
        super(context, questionDetails);

        numberOfStars = rangeQuestion.getRangeEnd().intValue();
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = RatingWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        binding.ratingBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        binding.ratingBar.setNumStars(numberOfStars);
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                widgetValueChanged();
                answer = (int) ratingBar.getRating();
            }
        });
        binding.ratingBar.setEnabled(!prompt.isReadOnly());
        binding.ratingBar.setStepSize(1.0F);

        if (prompt.getAnswerText() != null) {
            answer = Integer.parseInt(prompt.getAnswerText());
            binding.ratingBar.setRating(answer);
        } else {
            binding.ratingBar.setRating(0);
        }
        return answerView;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.ratingBar.setOnLongClickListener(l);
    }

    @Override
    public IAnswerData getAnswer() {
        return answer != null ? new IntegerData(answer) : null;
    }

    @Override
    public void clearAnswer() {
        answer = null;
        binding.ratingBar.setRating(0);
        widgetValueChanged();
    }

    protected RatingWidgetAnswerBinding getBinding() {
        return binding;
    }

    private int calculateColumns() {
        Drawable starDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_star);
        DisplayMetrics dm = getScreenDimensions((Activity) getContext());
        int widthOfStar = starDrawable.getIntrinsicWidth();
        return (dm.widthPixels - ASSUMED_TOTAL_MARGIN_AROUND_WIDGET) / widthOfStar;
    }

    private DisplayMetrics getScreenDimensions(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
