package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.LayoutInflater;

import bikramsambat.BsException;
import bikramsambat.BsGregorianDate;
import bikramsambat.android.BsDatePicker;

import java.util.Date;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.R;

import timber.log.Timber;

public class BikramSambatDateWidget extends QuestionWidget {
    private BsDatePicker picker;

    public BikramSambatDateWidget(Context ctx, FormEntryPrompt prompt) {
        super(ctx, prompt);

        LayoutInflater i = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(i.inflate(R.layout.bikram_sambat_date_picker, null));

        picker = new BsDatePicker(this);
        picker.init();

        clearAnswer();
    }

    /** reset date to right now */
    @Override public void clearAnswer() {
        setAnswer(new DateTime());
    }

    @Override public IAnswerData getAnswer() {
        DateTime dateTime = getAnswer_DateTime();
        if (dateTime == null) {
            return null;
        } else {
            return new DateData(dateTime.toDate());
        }
    }

    @Override public void setFocus(Context ctx) {}

    @Override public void setOnLongClickListener(OnLongClickListener l) {}

    private DateTime getAnswer_DateTime() {
        try {
            BsGregorianDate greg = picker.getDate_greg();
            if (greg == null) {
                return null;
            } else {
                return new DateTime(greg.year, greg.month, greg.day, 0, 0);
            }
        } catch (BsException ex) {
            Timber.d("getAnswer_DateTime() :: ecxception caught: %s", ex);
            return null;
        }
    }

    private void setAnswer() {
        if (formEntryPrompt.getAnswerValue() != null) {
            DateTime ldt = new DateTime(((Date) ((DateData) formEntryPrompt.getAnswerValue()).getValue()).getTime());
            setAnswer(ldt);
        } else {
            clearAnswer();
        }
    }

    private void setAnswer(DateTime ldt) {
        BsGregorianDate greg = new BsGregorianDate(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfMonth());
        try {
            picker.setDate(greg);
        } catch (BsException ex) {
            Timber.d("setAnswer() :: exception caught for date %s: %s", greg, ex);
        }
    }
}
