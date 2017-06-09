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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ToastUtils;

public class RangeWidget extends QuestionWidget {

    private int rangeStart;
    private int rangeEnd;
    private int rangeStep;
    private int progress;
    private int actualValue;

    private SeekBar seekBar;

    private TextView currentValue;

    public RangeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setupWidgetParameters();
        View view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.seek_bar_layout, null);
        setupLayoutElements(view);

        if (prompt.isReadOnly()) {
            seekBar.setEnabled(false);
        }

        addAnswerView(view);
    }

    @Override
    public IAnswerData getAnswer() {
        return new IntegerData(actualValue);
    }

    @Override
    public void clearAnswer() {
        if (seekBar.isEnabled()) {
            setupDefaultValues();
            seekBar.setProgress(progress);
            currentValue.setText(String.valueOf(actualValue));
        }
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    private void setupLayoutElements(View view) {
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        TextView minValue = (TextView) view.findViewById(R.id.minValue);
        minValue.setText(String.valueOf(rangeStart));

        TextView maxValue = (TextView) view.findViewById(R.id.maxValue);
        maxValue.setText(String.valueOf(rangeEnd));

        if (isWidgetValid()) {
            if (getPrompt().getAnswerValue() != null) {
                actualValue = (int) getPrompt().getAnswerValue().getValue();
                progress = Math.abs(actualValue - rangeStart) / rangeStep;
            } else {
                setupDefaultValues();
            }

            currentValue = (TextView) view.findViewById(R.id.currentValue);
            currentValue.setText(String.valueOf(actualValue));

            setupSeekBar();
        }
    }

    private void setupDefaultValues() {
        progress = Math.abs(rangeEnd - rangeStart) / (rangeStep * 2);
        if (rangeStart < rangeEnd) {
            actualValue = rangeStart + progress * rangeStep;
        } else {
            actualValue = rangeStart - progress * rangeStep;
        }
    }

    private void setupWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart().intValue();
        rangeEnd = rangeQuestion.getRangeEnd().intValue();
        rangeStep = Math.abs(rangeQuestion.getRangeStep().intValue());
    }

    private void setupSeekBar() {
        int seekBarMax;
        seekBarMax = Math.abs(rangeEnd - rangeStart) / rangeStep;

        seekBar.setMax(seekBarMax);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((FormEntryActivity) getContext()).allowSwiping(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((FormEntryActivity) getContext()).allowSwiping(false);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if (rangeStart < rangeEnd) {
                    actualValue = rangeStart + progress * rangeStep;
                } else {
                    actualValue = rangeStart - progress * rangeStep;
                }
                currentValue.setText(String.valueOf(actualValue));
            }
        });
    }

    private boolean isWidgetValid() {
        boolean result = true;
        if (rangeStep == 0 || (rangeEnd - rangeStart) % rangeStep != 0) {
            ToastUtils.showLongToast(R.string.invalid_range_widget);
            seekBar.setEnabled(false);
            result = false;
        }
        return result;
    }
}