package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;

public class RangePickerDecimalWidget extends QuestionWidget {

    protected String[] displayedValuesForNumberPicker;
    WidgetAnswerBinding binding;

    public RangePickerDecimalWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {

    }

    public void setNumberPickerValue(float value) {
    }
}
