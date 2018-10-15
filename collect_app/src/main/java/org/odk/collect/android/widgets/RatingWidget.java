package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.ImageButton;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

public class RatingWidget extends QuestionWidget {

    private final GridLayout gridLayout;
    private int answer;

    public RatingWidget(Context context, FormEntryPrompt prompt) {

        super(context, prompt);

        Drawable d = getResources().getDrawable(R.drawable.ic_star);
        int widthOfStar = d.getIntrinsicWidth();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int maxStarsPerRow = (dm.widthPixels - 20) / widthOfStar;

        gridLayout = new GridLayout(context);
        gridLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();
        gridLayout.setColumnCount(maxStarsPerRow);
        int numberOfStarsToShow = rangeQuestion.getRangeEnd().intValue();
        int rows = (int) Math.ceil((double) numberOfStarsToShow / maxStarsPerRow);
        gridLayout.setRowCount(rows);

        for (int column = 0,
            total = 0; total < numberOfStarsToShow; total++, column++) {

            column = column == maxStarsPerRow ? 0 : column;
            ImageButton imageButton = new ImageButton(context);
            imageButton.setImageResource(R.drawable.ic_star_border);
            imageButton.setId(total);
            imageButton.setPadding(0, 0, 0, 0);
            imageButton.setBackground(null);
            imageButton.setOnClickListener(view -> {
                int position = view.getId();
                for (int i = 0; i <= position; i++) {
                    ImageButton button = (ImageButton) gridLayout.getChildAt(i);
                    button.setImageResource(R.drawable.ic_star);
                    answer = position + 1;
                }

                for (int i = position + 1; i < numberOfStarsToShow; i++) {
                    ImageButton button = (ImageButton) gridLayout.getChildAt(i);
                    button.setImageResource(R.drawable.ic_star_border);
                }
            });
            gridLayout.addView(imageButton);
        }

        String s = prompt.getAnswerText();
            if (s != null) {
                answer = Integer.parseInt(s);
                for (int i = 0; i < answer; i++) {
                    ImageButton button = (ImageButton) gridLayout.getChildAt(i);
                    button.setImageResource(R.drawable.ic_star);
                }
            }
        addAnswerView(gridLayout);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            gridLayout.getChildAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public IAnswerData getAnswer() {
        String s = String.valueOf(answer);
        return new StringData(s);
    }

    @Override
    public void clearAnswer() {
        for (int i = 0; i < answer; i++) {
            ((ImageButton) gridLayout.getChildAt(i)).setImageResource(R.drawable.ic_star_border);
        }
        answer = 0;
    }
}
