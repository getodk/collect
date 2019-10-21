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

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        startGeoButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        startGeoButton.cancelLongPress();
        answerDisplay.cancelLongPress();
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

    protected abstract void updateButtonLabelsAndVisibility(boolean dataAvailable);
}
