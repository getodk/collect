package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.NepaliDatePickerDialog;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Date;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

/**
 * @author Nishon Tandukar (nishon.tan@gmail.com)
 */

public class NepaliDateWidget extends AbstractDateWidget {
    public NepaliDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void createWidget() {
        super.createWidget();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    @Override
    protected void setDateLabel() {
        isNullAnswer = false;
        dateTextView.setText(DateTimeUtils.getDateTimeLabel((Date) getAnswer().getValue(), datePickerDetails, false, getContext()));
    }

    protected void showDatePickerDialog() {
        NepaliDatePickerDialog nepaliDatePickerDialog = NepaliDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
        nepaliDatePickerDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);

    }

}
