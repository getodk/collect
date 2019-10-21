package org.odk.collect.android.widgets;

import android.content.Context;

import org.odk.collect.android.formentry.questions.QuestionDetails;

public abstract class BaseGeoWidget extends QuestionWidget {
    public BaseGeoWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }
}
