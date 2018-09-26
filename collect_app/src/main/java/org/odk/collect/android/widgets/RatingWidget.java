package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ViewIds;

public class RatingWidget extends QuestionWidget {

    private final RatingBar ratingBar;
    private int answer;

    public RatingWidget(Context context, FormEntryPrompt prompt) {

        super(context, prompt);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        ratingBar = new RatingBar(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 0);
        ratingBar.setLayoutParams(layoutParams);

        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();
        int maxStars = rangeQuestion.getRangeEnd().intValue();
        ratingBar.setMax(maxStars);

        ratingBar.setNumStars(maxStars);
        ratingBar.setId(ViewIds.generateViewId());
        ratingBar.setStepSize((float) 1.0);
        ratingBar.setRating((float) 0);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                answer = (int) v;
            }
        });

        linearLayout.addView(ratingBar);
        addAnswerView(linearLayout);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }

    @Override
    public IAnswerData getAnswer() {
        String s = String.valueOf(answer);
        return new StringData(s);
    }

    @Override
    public void clearAnswer() {
        answer = 0;
        ratingBar.setRating(0);

    }
}
