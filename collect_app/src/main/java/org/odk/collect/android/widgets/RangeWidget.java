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

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import org.javarosa.core.model.RangeQuestion;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

import java.math.BigDecimal;

import timber.log.Timber;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createAnswerTextView;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;

@SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
public abstract class RangeWidget extends QuestionWidget implements ButtonWidget, Slider.OnChangeListener, Slider.OnSliderTouchListener {

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
    public
    TextView currentValue;

    private int progress;
    public Slider slider;
    private LinearLayout view;

    private boolean isPickerAppearance;
    private boolean suppressFlingGesture;

    public Button pickerButton;
    public TextView answerTextView;

    public RangeWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        setUpWidgetParameters();
        setUpAppearance();

        if (questionDetails.getPrompt().isReadOnly() && !isPickerAppearance) {
            slider.setEnabled(false);
        }

        addAnswerView(view, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    public void clearAnswer() {
        setUpNullValue();
        widgetValueChanged();
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
            slider.setValue(rangeStart.floatValue());
            actualValue = null;
            setUpActualValueLabel();
        }
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpSeekBar() {
        slider.setValueFrom(rangeStart.floatValue());
        slider.setValueTo(rangeEnd.floatValue());
        slider.setStepSize(rangeStep.intValue());
        slider.setValue(actualValue == null ? rangeStart.floatValue(): actualValue.floatValue());
        slider.addOnChangeListener(this);

        slider.setOnTouchListener((v, event) -> {
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
        });
    }

    private void disableWidget() {
        ToastUtils.showLongToast(R.string.invalid_range_widget);
        if (isPickerAppearance) {
            pickerButton.setEnabled(false);
        } else {
            slider.setEnabled(false);
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
            pickerButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.select_value), getAnswerFontSize(), this);

            answerTextView = createAnswerTextView(getContext(), getAnswerFontSize());
            isPickerAppearance = true;

            view = new LinearLayout(getContext());
            view.setOrientation(LinearLayout.VERTICAL);
            view.addView(pickerButton);
            view.addView(answerTextView);

        } else {
            @LayoutRes int layoutId = appearance.contains(VERTICAL_APPEARANCE)
                    ? R.layout.range_widget_vertical
                    : R.layout.range_widget_horizontal;

            @IdRes int seekBarId = R.id.seek_bar;

            loadAppearance(layoutId, seekBarId);
        }

        setUpLayoutElements();
    }

    private void loadAppearance(@LayoutRes int layoutId, @IdRes int seekBarId) {
        view = (LinearLayout) getLayoutInflater().inflate(layoutId, this, false);
        slider = view.findViewById(seekBarId);
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

    public Slider getSlider() {
        return slider;
    }

    @Override
    public void onStopTrackingTouch(Slider slider) {
        suppressFlingGesture = false;
    }

    @Override
    public void onStartTrackingTouch(Slider slider) {
        suppressFlingGesture = true;
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = BigDecimal.valueOf(value);
        } else {
            actualValue = BigDecimal.valueOf(value);
        }

        setUpActualValueLabel();
        widgetValueChanged();
    }

    public int getElementCount() {
        return elementCount;
    }
}