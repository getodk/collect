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
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.RangeWidgetHorizontalBinding;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ToastUtils;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangeDecimalWidget extends QuestionWidget implements Slider.OnChangeListener, Slider.OnSliderTouchListener {

    private static final String VERTICAL_APPEARANCE = "vertical";

    private Slider slider;
    private TextView currentValue;
    private TextView minValue;
    private TextView maxValue;

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private BigDecimal actualValue;

    private boolean suppressFlingGesture;

    public RangeDecimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        String appearance = prompt.getQuestion().getAppearanceAttr();

        View answerView;
        if (appearance != null && appearance.contains(VERTICAL_APPEARANCE)) {
            RangeWidgetVerticalBinding rangeWidgetVerticalBinding = RangeWidgetVerticalBinding
                    .inflate(((Activity) context).getLayoutInflater());
            answerView = rangeWidgetVerticalBinding.getRoot();

            slider = rangeWidgetVerticalBinding.seekBar;
            currentValue = rangeWidgetVerticalBinding.currentValue;
            minValue = rangeWidgetVerticalBinding.minValue;
            maxValue = rangeWidgetVerticalBinding.maxValue;
        } else {
            RangeWidgetHorizontalBinding rangeWidgetHorizontalBinding = RangeWidgetHorizontalBinding
                    .inflate(((Activity) context).getLayoutInflater());
            answerView = rangeWidgetHorizontalBinding.getRoot();

            slider = rangeWidgetHorizontalBinding.seekBar;
            currentValue = rangeWidgetHorizontalBinding.currentValue;
            minValue = rangeWidgetHorizontalBinding.minValue;
            maxValue = rangeWidgetHorizontalBinding.maxValue;
        }

        setUpWidgetParameters();
        if (prompt.isReadOnly()) {
            slider.setEnabled(false);
        }

        if (isWidgetValid()) {
            if (getFormEntryPrompt().getAnswerValue() != null) {
                actualValue = new BigDecimal(getFormEntryPrompt().getAnswerValue().getValue().toString());
            } else {
                setUpNullValue();
            }
            setUpActualValueLabel();
            setUpSeekBar();
        }
        return answerView;
    }

    @Override
    public IAnswerData getAnswer() {
        return actualValue != null
                ? new DecimalData(actualValue.doubleValue())
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return suppressFlingGesture;
    }

    @Override
    public void clearAnswer() {
        setUpNullValue();
        widgetValueChanged();
    }

    private void setUpActualValueLabel() {
        String value = actualValue != null
                ? String.valueOf(actualValue.doubleValue())
                : "";
        currentValue.setText(value);
    }

    private void setUpNullValue() {
        slider.setValue(rangeStart.floatValue());
        actualValue = null;
        setUpActualValueLabel();
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();

        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();

        minValue.setText(String.valueOf(rangeStart));
        maxValue.setText(String.valueOf(rangeEnd));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpSeekBar() {
        slider.setValueFrom(rangeStart.floatValue());
        slider.setValueTo(rangeEnd.floatValue());
        slider.setStepSize(rangeStep.floatValue() != 0 ? rangeStep.floatValue() : 0.5F);
        slider.setValue(actualValue == null ? rangeStart.floatValue() : actualValue.floatValue());
        slider.addOnChangeListener(this);
        slider.addOnSliderTouchListener(this);

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
        slider.setEnabled(false);
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

    //for testing purposes
    protected Slider getSlider() {
        return slider;
    }

    protected TextView getCurrentValue() {
        return currentValue;
    }

    protected TextView getMinValue() {
        return minValue;
    }

    protected TextView getMaxValue() {
        return maxValue;
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
        actualValue = BigDecimal.valueOf(value);
        setUpActualValueLabel();
        widgetValueChanged();
    }
}
