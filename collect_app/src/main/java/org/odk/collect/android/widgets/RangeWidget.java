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
import android.os.Build;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.ToastUtils;

import java.math.BigDecimal;

import timber.log.Timber;

public abstract class RangeWidget extends QuestionWidget {

    private static final String VERTICAL_APPEARANCE = "vertical";
    private static final String NO_TICKS_APPEARANCE = "no-ticks";
    private static final String PICKER_APPEARANCE = "picker";

    protected BigDecimal rangeStart;
    protected BigDecimal rangeEnd;
    protected BigDecimal rangeStep;
    protected BigDecimal actualValue;

    protected String[] displayedValuesForNumberPicker;

    private int progress;
    protected int elementCount;

    private SeekBar seekBar;

    protected TextView currentValue;

    private View view;

    private boolean isPickerAppearance;
    private boolean suppressFlingGesture;

    private Button pickerButton;
    private TextView answerTextView;

    public RangeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setUpWidgetParameters();
        setUpAppearance();

        if (prompt.isReadOnly()) {
            if (isPickerAppearance) {
                pickerButton.setEnabled(false);
            } else {
                seekBar.setEnabled(false);
            }
        }

        addAnswerView(view);
    }

    @Override
    public void clearAnswer() {
        setUpNullValue();
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (isPickerAppearance) {
            pickerButton.setOnLongClickListener(l);
            answerTextView.setOnLongClickListener(l);
        }
    }

    @Override
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return suppressFlingGesture;
    }

    private void setUpLayoutElements() {
        if (!isPickerAppearance) {
            TextView minValue = (TextView) view.findViewById(R.id.min_value);
            minValue.setText(String.valueOf(rangeStart));

            TextView maxValue = (TextView) view.findViewById(R.id.max_value);
            maxValue.setText(String.valueOf(rangeEnd));

            currentValue = (TextView) view.findViewById(R.id.current_value);
        }

        if (isWidgetValid()) {
            elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
            if (getPrompt().getAnswerValue() != null) {
                actualValue = new BigDecimal(getPrompt().getAnswerValue().getValue().toString());
                progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
            } else {
                setUpNullValue();
            }

            if (!isPickerAppearance) {
                setUpActualValueLabel();
                setUpSeekBar();
            } else {
                setUpDisplayedValuesForNumberPicker();
                answerTextView.setText(getPrompt().getAnswerValue() != null ? String.valueOf(actualValue) : getContext().getString(R.string.no_value_selected));
                pickerButton.setText(getPrompt().getAnswerValue() != null ? getContext().getString(R.string.edit_value) : getContext().getString(R.string.select_value));
            }
        }
    }

    private void setUpNullValue() {
        progress = 0;
        if (isPickerAppearance) {
            actualValue = null;
            answerTextView.setText(R.string.no_value_selected);
            pickerButton.setText(R.string.select_value);
        } else {
            seekBar.setProgress(progress);
            actualValue = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                seekBar.setSplitTrack(false);
            }
            seekBar.getThumb().mutate().setAlpha(0);
            setUpActualValueLabel();
        }
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();
    }

    private void setUpSeekBar() {
        seekBar.setMax(elementCount);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                suppressFlingGesture = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                suppressFlingGesture = true;
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
        seekBar.setOnTouchListener(new SeekBar.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                seekBar.getThumb().mutate().setAlpha(255);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    private void disableWidget() {
        ToastUtils.showLongToast(R.string.invalid_range_widget);
        if (isPickerAppearance) {
            pickerButton.setEnabled(false);
        } else {
            seekBar.setEnabled(false);
        }
    }

    private boolean isWidgetValid() {
        boolean result = true;
        if (rangeStep.compareTo(new BigDecimal(0)) == 0 || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(new BigDecimal(0)) != 0) {
            disableWidget();
            result = false;
        }
        return result;
    }

    private void setUpAppearance() {
        String appearance = getPrompt().getQuestion().getAppearanceAttr();

        if (appearance != null && appearance.contains(PICKER_APPEARANCE)) {
            view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.button_textview_layout, null);
            pickerButton = (Button) view.findViewById(R.id.trigger_button);
            pickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNumberPickerDialog();
                }
            });
            pickerButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
            pickerButton.setText(getContext().getString(R.string.select_value));
            pickerButton.setPadding(20, 20, 20, 20);
            answerTextView = (TextView) view.findViewById(R.id.answer_text_view);
            answerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
            answerTextView.setPadding(20, 20, 20, 20);
            isPickerAppearance = true;
        } else if (appearance != null && appearance.contains(NO_TICKS_APPEARANCE)) {
            if (appearance.contains(VERTICAL_APPEARANCE)) {
                view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.range_widget_vertical, null);
                seekBar = (SeekBar) view.findViewById(R.id.seek_bar_no_ticks);
                view.findViewById(R.id.seek_bar).setVisibility(GONE);
            } else {
                view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.range_widget_horizontal, null);
                seekBar = (SeekBar) view.findViewById(R.id.seek_bar_no_ticks);
                view.findViewById(R.id.seek_bar).setVisibility(GONE);
            }
        } else {
            if (appearance != null && appearance.contains(VERTICAL_APPEARANCE)) {
                view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.range_widget_vertical, null);
                seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
                view.findViewById(R.id.seek_bar_no_ticks).setVisibility(GONE);
            } else {
                view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.range_widget_horizontal, null);
                seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
                view.findViewById(R.id.seek_bar_no_ticks).setVisibility(GONE);
            }
        }

        setUpLayoutElements();
    }

    public void setNumberPickerValue(int value) {
        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = rangeStart.add(new BigDecimal(elementCount - value).multiply(rangeStep));
        } else {
            actualValue = rangeStart.subtract(new BigDecimal(elementCount - value).multiply(rangeStep));
        }

        progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();

        answerTextView.setText(String.valueOf(actualValue));
        pickerButton.setText(R.string.edit_value);
    }

    private void showNumberPickerDialog() {
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(getId(), displayedValuesForNumberPicker, progress);

        try {
            dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);
        } catch (ClassCastException e) {
            Timber.i(e);
        }
    }

    protected abstract void setUpActualValueLabel();

    protected abstract void setUpDisplayedValuesForNumberPicker();
}