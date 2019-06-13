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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.ImageButton;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

public class RatingWidget extends QuestionWidget {

    public static final int ASSUMED_TOTAL_MARGIN_AROUND_WIDGET = 40;

    private final GridLayout gridLayout;
    private Integer answer;

    public RatingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();
        int numberOfStars = rangeQuestion.getRangeEnd().intValue();
        int columns = calculateColumns();
        int rows = (int) Math.ceil((double) numberOfStars / columns);

        gridLayout = new GridLayout(context);
        gridLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        renderGrid(context, numberOfStars, columns, rows);

        String answerText = prompt.getAnswerText();
        if (answerText != null) {
            answer = Integer.parseInt(answerText);
            for (int i = 0; i < answer; i++) {
                ImageButton button = (ImageButton) gridLayout.getChildAt(i);
                button.setImageResource(R.drawable.ic_star);
            }
        }

        addAnswerView(gridLayout);
    }

    private void renderGrid(Context context, int numberOfStars, int columns, int rows) {
        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);

        for (int column = 0,
             starId = 0;
             starId < numberOfStars;
             column++, starId++) {
            column = column == columns ? 0 : column;

            ImageButton imageButton = createImageButton(context, numberOfStars, starId);
            gridLayout.addView(imageButton);
        }
    }

    private ImageButton createImageButton(Context context, int numberOfStars, int total) {
        ImageButton imageButton = new ImageButton(context);
        imageButton.setImageResource(R.drawable.ic_star_border);
        imageButton.setId(total);
        imageButton.setPadding(0, 0, 0, 0);
        imageButton.setBackground(null);
        imageButton.setEnabled(!getFormEntryPrompt().isReadOnly());
        imageButton.setOnClickListener(view -> {
            int position = view.getId();
            for (int i = 0; i < numberOfStars; i++) {
                ImageButton button = (ImageButton) gridLayout.getChildAt(i);
                button.setImageResource(i <= position ? R.drawable.ic_star : R.drawable.ic_star_border);
            }
            answer = position + 1;

            widgetValueChanged();
        });
        return imageButton;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            gridLayout.getChildAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public IAnswerData getAnswer() {
        return answer != null ? new IntegerData(answer) : null;
    }

    @Override
    public void clearAnswer() {
        if (answer != null) {
            for (int i = 0; i < answer; i++) {
                ((ImageButton) gridLayout.getChildAt(i)).setImageResource(R.drawable.ic_star_border);
            }
        }

        answer = null;
        widgetValueChanged();
    }

    private int calculateColumns() {
        Drawable starDrawable = getResources().getDrawable(R.drawable.ic_star);
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
