package odk.hedera.collect.widgets;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import odk.hedera.collect.formentry.questions.QuestionDetails;
import odk.hedera.collect.formentry.questions.WidgetViewUtils;
import odk.hedera.collect.listeners.PermissionListener;
import odk.hedera.collect.widgets.interfaces.GeoWidget;

import static odk.hedera.collect.formentry.questions.WidgetViewUtils.createSimpleButton;
import static odk.hedera.collect.formentry.questions.WidgetViewUtils.getCenteredAnswerTextView;

public abstract class BaseGeoWidget extends QuestionWidget implements GeoWidget {
    public Button startGeoButton;
    public TextView answerDisplay;
    protected boolean readOnly;

    public BaseGeoWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        answerDisplay = getCenteredAnswerTextView(getContext(), getAnswerFontSize());
        startGeoButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getDefaultButtonLabel(), getAnswerFontSize(), this);
        readOnly = questionDetails.getPrompt().isReadOnly();
        setUpLayout(context, questionDetails.getPrompt().getAnswerText());
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

    @Override
    public void setBinaryData(Object answer) {
        String answerText = answer.toString();
        answerDisplay.setText(getAnswerToDisplay(answerText));
        updateButtonLabelsAndVisibility(!answerText.isEmpty());
        widgetValueChanged();
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

    protected void setUpLayout(Context context, String answerText) {
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(startGeoButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));

        boolean dataAvailable = false;
        if (answerText != null && !answerText.isEmpty()) {
            dataAvailable = true;
            setBinaryData(answerText);
        }

        updateButtonLabelsAndVisibility(dataAvailable);
    }
}
