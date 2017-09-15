package org.odk.collect.android.widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;

public abstract class DateWidgetAbstract extends QuestionWidget {
    protected Button dateButton;
    protected TextView dateTextView;

    protected int year;
    protected int month;
    protected int day;

    protected boolean nullAnswer;
    protected boolean hideDay;
    protected boolean hideMonth;

    public DateWidgetAbstract(Context context, FormEntryPrompt p) {
        super(context, p);

        setGravity(Gravity.START);

        createWidget();
        addViews();
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        if (nullAnswer) {
            return null;
        } else {
            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(hideMonth ? 1 : month)
                    .withDayOfMonth((hideMonth || hideDay) ? 1 : day)
                    .withHourOfDay(0)
                    .withMinuteOfHour(0);
            return new DateData(ldt.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        nullAnswer = true;
        dateTextView.setText(R.string.no_date_selected);
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        dateButton.setOnLongClickListener(l);
        dateTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        dateButton.cancelLongPress();
        dateTextView.cancelLongPress();
    }

    protected void createWidget() {
        createDateButton();
        createDateTextView();
    }

    private void createDateButton() {
        dateButton = getSimpleButton(getContext().getString(R.string.select_date));
        dateButton.setEnabled(!formEntryPrompt.isReadOnly());
    }

    private void createDateTextView() {
        dateTextView = new TextView(getContext());
        dateTextView.setId(QuestionWidget.newUniqueId());
        dateTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.primaryTextColor));
        dateTextView.setPadding(20, 20, 20, 20);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        addAnswerView(linearLayout);
    }
}
