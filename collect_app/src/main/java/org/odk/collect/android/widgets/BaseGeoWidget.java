package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

public abstract class BaseGeoWidget extends QuestionWidget implements BinaryWidget {
    protected Button startGeoButton;
    protected TextView answerDisplay;

    public BaseGeoWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        answerDisplay = getCenteredAnswerTextView();
    }

    public void onButtonClick(int buttonId) {
        getPermissionUtils().requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitForData();
                startGeoActivity();
            }

            @Override
            public void denied() {
            }
        });
    }

    protected abstract void startGeoActivity();
}
