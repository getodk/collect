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
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

import java.math.BigDecimal;

import timber.log.Timber;

@SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
public abstract class RangeWidget extends QuestionWidget implements ButtonWidget, SeekBar.OnSeekBarChangeListener {

    private static final String VERTICAL_APPEARANCE = "vertical";
    private static final String NO_TICKS_APPEARANCE = "no-ticks";
    private static final String PICKER_APPEARANCE = "picker";

    protected BigDecimal rangeStart;
    protected BigDecimal rangeEnd;
    protected BigDecimal rangeStep;
    protected BigDecimal actualValue;

    protected String[] displayedValuesForNumberPicker;
    protected int elementCount;

    @Nullable
    protected TextView currentValue;

    private int progress;
    private SeekBar seekBar;
    private LinearLayout view;

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
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {
        setUpNullValue();
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

    @Override
    public void onButtonClick(int buttonId) {
        showNumberPickerDialog();
    }

    private void setUpLayoutElements() {
        if (!isPickerAppearance) {
            TextView minValue = view.findViewById(R.id.min_value);
            minValue.setText(String.valueOf(rangeStart));

            TextView maxValue = view.findViewById(R.id.max_value);
            maxValue.setText(String.valueOf(rangeEnd));

            currentValue = view.findViewById(R.id.current_value);
        }

        if (isWidgetValid()) {
            elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
            if (getFormEntryPrompt().getAnswerValue() != null) {
                actualValue = new BigDecimal(getFormEntryPrompt().getAnswerValue().getValue().toString());
                progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
            } else {
                setUpNullValue();
            }

            if (!isPickerAppearance) {
                setUpActualValueLabel();
                setUpSeekBar();
            } else {
                setUpDisplayedValuesForNumberPicker();
                answerTextView.setText(getFormEntryPrompt().getAnswerValue() != null ? String.valueOf(actualValue) : getContext().getString(R.string.no_value_selected));
                pickerButton.setText(getFormEntryPrompt().getAnswerValue() != null ? getContext().getString(R.string.edit_value) : getContext().getString(R.string.select_value));
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
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();
    }

    private void setUpSeekBar() {
        seekBar.setMax(elementCount);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(this);
        if (isRTL()) {
            float rotate = seekBar.getRotation();
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
                seekBar.setRotation(180 - rotate);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
                seekBar.setRotation(360 - rotate);
            } else {
                seekBar.setRotation(rotate);
            }
        }

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
                        if (actualValue == null) {
                            actualValue = rangeStart;
                            setUpActualValueLabel();
                        }
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
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            disableWidget();
            result = false;
        }
        return result;
    }

    private void setUpAppearance() {
        String appearance = getFormEntryPrompt().getQuestion().getAppearanceAttr();

        if (appearance == null) {
            loadAppearance(R.layout.range_widget_horizontal, R.id.seek_bar);

        } else if (appearance.contains(PICKER_APPEARANCE)) {
            pickerButton = getSimpleButton(getContext().getString(R.string.select_value));

            answerTextView = getAnswerTextView();
            isPickerAppearance = true;

            view = new LinearLayout(getContext());
            view.setOrientation(LinearLayout.VERTICAL);
            view.addView(pickerButton);
            view.addView(answerTextView);

        } else {
            @LayoutRes int layoutId = appearance.contains(VERTICAL_APPEARANCE)
                    ? R.layout.range_widget_vertical
                    : R.layout.range_widget_horizontal;

            @IdRes int seekBarId = appearance.contains(NO_TICKS_APPEARANCE)
                    ? R.id.seek_bar_no_ticks
                    : R.id.seek_bar;

            loadAppearance(layoutId, seekBarId);
        }

        setUpLayoutElements();
    }

    private void loadAppearance(@LayoutRes int layoutId, @IdRes int seekBarId) {
        view = (LinearLayout) getLayoutInflater().inflate(layoutId, this, false);
        seekBar = view.findViewById(seekBarId);

        @IdRes int hiddenSeekBarId;
        if (seekBarId == R.id.seek_bar) {
            hiddenSeekBarId = R.id.seek_bar_no_ticks;

        } else if (seekBarId == R.id.seek_bar_no_ticks) {
            hiddenSeekBarId = R.id.seek_bar;

        } else {
            Timber.w("Unknown SeekBar ID.");
            return;
        }

        view.findViewById(hiddenSeekBarId).setVisibility(GONE);
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

    private LayoutInflater layoutInflater;

    // For testing purposes only:
    void setLayoutInflater(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    private LayoutInflater getLayoutInflater() {

        // Only for testing purposes, this shouldn't actually be cached:
        if (this.layoutInflater != null) {
            return layoutInflater;
        }

        return LayoutInflater.from(getContext());
    }

    protected abstract void setUpActualValueLabel();

    protected abstract void setUpDisplayedValuesForNumberPicker();

    public SeekBar getSeekBar() {
        return seekBar;
    }

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

    public int getElementCount() {
        return elementCount;
    }
}