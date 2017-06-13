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
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ToastUtils;

import java.math.BigDecimal;

public abstract class RangeWidget extends QuestionWidget {

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    protected BigDecimal actualValue;

    private int progress;

    private SeekBar seekBar;

    protected TextView currentValue;

    public RangeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setUpWidgetParameters();
        View view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.seek_bar_layout, null);
        setUpLayoutElements(view);

        if (prompt.isReadOnly()) {
            seekBar.setEnabled(false);
        }

        addAnswerView(view);
    }

    @Override
    public void clearAnswer() {
        if (seekBar.isEnabled()) {
            setUpDefaultValues();
            seekBar.setProgress(progress);
            setUpActualValueLabel();
        }
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    private void setUpLayoutElements(View view) {
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        TextView minValue = (TextView) view.findViewById(R.id.minValue);
        minValue.setText(String.valueOf(rangeStart));

        TextView maxValue = (TextView) view.findViewById(R.id.maxValue);
        maxValue.setText(String.valueOf(rangeEnd));

        if (isWidgetValid()) {
            if (getPrompt().getAnswerValue() != null) {
                actualValue = new BigDecimal(getPrompt().getAnswerValue().getValue().toString());
                progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
            } else {
                setUpDefaultValues();
            }

            currentValue = (TextView) view.findViewById(R.id.currentValue);
            setUpActualValueLabel();
            setUpSeekBar();
        }
    }

    private void setUpDefaultValues() {
        progress = rangeEnd.subtract(rangeStart).abs().divide(rangeStep.multiply(new BigDecimal(2))).intValue();
        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = rangeStart.add(new BigDecimal(progress).multiply(rangeStep));
        } else {
            actualValue = rangeStart.subtract(new BigDecimal(progress).multiply(rangeStep));
        }
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();
    }

    private void setUpSeekBar() {
        int seekBarMax;
        seekBarMax = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();

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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (rangeStart.compareTo(rangeEnd) == -1) {
                    actualValue = rangeStart.add(new BigDecimal(progress).multiply(rangeStep));
                } else {
                    actualValue = rangeStart.subtract(new BigDecimal(progress).multiply(rangeStep));
                }
                setUpActualValueLabel();
            }
        });
    }

    private void disableSeekBar() {
        ToastUtils.showLongToast(R.string.invalid_range_widget);
        seekBar.setEnabled(false);
    }

    private boolean isWidgetValid() {
        boolean result = true;
        if (rangeStep.compareTo(new BigDecimal(0)) == 0 || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(new BigDecimal(0)) != 0) {
            disableSeekBar();
            result = false;
        }
        return result;
    }

    protected abstract void setUpActualValueLabel();
}