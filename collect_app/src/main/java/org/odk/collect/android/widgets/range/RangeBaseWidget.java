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

package org.odk.collect.android.widgets.range;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.views.TrackingTouchSlider;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangeBaseWidget extends QuestionWidget implements Slider.OnChangeListener {
    private final boolean isIntegerType;
    TrackingTouchSlider slider;
    TextView currentValue;

    protected RangeBaseWidget(Context context,
                              QuestionDetails prompt,
                              Dependencies dependencies,
                              boolean isIntegerType) {
        super(context, dependencies, prompt);
        this.isIntegerType = isIntegerType;
        render();
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        RangeWidgetUtils.RangeWidgetLayoutElements layoutElements = RangeWidgetUtils.setUpLayoutElements(context, prompt);
        slider = layoutElements.getSlider();
        currentValue = layoutElements.getCurrentValue();

        BigDecimal set = RangeWidgetUtils.setUpSlider(prompt, slider, isIntegerType);
        setUpActualValueLabel(set == null ? null : set.floatValue());

        if (slider.isEnabled()) {
            slider.setListener(this);
        }
        return layoutElements.getAnswerView();
    }

    @Override
    public IAnswerData getAnswer() {
        String stringAnswer = currentValue.getText().toString();
        return stringAnswer.isEmpty() ? null
                : isIntegerType ? new IntegerData(Integer.parseInt(stringAnswer))
                : new DecimalData(Double.parseDouble(stringAnswer));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public boolean shouldSuppressFlingGesture() {
        return slider.isTrackingTouch();
    }

    @Override
    public void clearAnswer() {
        setUpActualValueLabel(null);
        widgetValueChanged();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser) {
            setUpActualValueLabel(RangeWidgetUtils.getActualValue(getFormEntryPrompt(), value));
            widgetValueChanged();
        }
    }

    private void setUpActualValueLabel(Float actualValue) {
        if (actualValue != null) {
            currentValue.setText(
                    isIntegerType ? String.valueOf(actualValue.intValue())
                            : String.valueOf(actualValue)
            );
        } else {
            currentValue.setText("");
            slider.reset();
        }
    }
}
