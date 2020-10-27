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
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.views.TrackingTouchSlider;
import org.odk.collect.android.widgets.utilities.RangeWidgetDataRequester;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangeDecimalWidget extends QuestionWidget implements Slider.OnChangeListener {
    TrackingTouchSlider slider;
    TextView currentValue;
    TextView minValue;
    TextView maxValue;

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;

    private int visibleThumbRadius;

    public RangeDecimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();

        RangeWidgetDataRequester.RangeWidgetLayoutElements layoutElements = RangeWidgetDataRequester
                .getLayoutElements(context, rangeQuestion.getAppearanceAttr());
        slider = layoutElements.getSlider();
        currentValue = layoutElements.getCurrentValue();
        minValue = layoutElements.getMinValue();
        maxValue = layoutElements.getMaxValue();

        setUpWidgetParameters(rangeQuestion);
        minValue.setText(String.valueOf(rangeStart));
        maxValue.setText(String.valueOf(rangeEnd));

        visibleThumbRadius = slider.getThumbRadius();

        BigDecimal actualValue = null;
        if (prompt.getAnswerValue() != null && !prompt.getAnswerText().isEmpty()) {
            actualValue = new BigDecimal(prompt.getAnswerText());
        }

        if (prompt.isReadOnly() || !RangeWidgetDataRequester.isWidgetValid(rangeQuestion)) {
            slider.setEnabled(false);
        } else  {
            setUpSlider(rangeQuestion.getAppearanceAttr(), actualValue);
            slider.addOnChangeListener(this);
        }
        setUpActualValueLabel(actualValue);


        return layoutElements.getAnswerView();
    }

    @Override
    public IAnswerData getAnswer() {
        String stringAnswer = currentValue.getText().toString();
        return stringAnswer.isEmpty() ? null : new DecimalData(Double.parseDouble(stringAnswer));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return slider.isTrackingTouch();
    }

    @Override
    public void clearAnswer() {
        setUpActualValueLabel(null);
        widgetValueChanged();
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser) {
            BigDecimal actualValue = RangeWidgetDataRequester.getActualValue(getFormEntryPrompt(), slider,
                    rangeStart, rangeEnd, rangeStep, BigDecimal.valueOf(value));
            setUpActualValueLabel(actualValue);
            widgetValueChanged();
        }
    }

    private void setUpWidgetParameters(RangeQuestion rangeQuestion) {
        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs() == null ? BigDecimal.valueOf(0.5) : rangeQuestion.getRangeStep().abs();
    }

    private void setUpActualValueLabel(BigDecimal actualValue) {
        if (actualValue != null) {
            currentValue.setText(String.valueOf(actualValue.floatValue()));
            slider.setThumbRadius(visibleThumbRadius);
        } else {
            slider.setValue(slider.getValueFrom());
            slider.setThumbRadius(0);
            currentValue.setText("");
        }
    }

    private void setUpSlider(String appearance, BigDecimal actualValue) {
        if (rangeEnd.compareTo(rangeStart) > -1) {
            slider.setValueFrom(rangeStart.floatValue());
            slider.setValueTo(rangeEnd.floatValue());
        } else {
            slider.setValueFrom(rangeEnd.floatValue());
            slider.setValueTo(rangeStart.floatValue());
        }
        if (!RangeWidgetDataRequester.hasNoTicksAppearance(appearance)) {
            slider.setStepSize(rangeStep.floatValue());
        }

        if (actualValue != null) {
            if (rangeEnd.compareTo(rangeStart) > -1) {
                slider.setValue(actualValue.floatValue());
            } else {
                slider.setValue(rangeStart.add(rangeEnd).subtract(actualValue).floatValue());
            }
        }
    }
}
