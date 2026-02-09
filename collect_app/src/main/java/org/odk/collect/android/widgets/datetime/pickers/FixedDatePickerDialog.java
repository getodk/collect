package org.odk.collect.android.widgets.datetime.pickers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.joda.time.LocalDateTime;
import org.odk.collect.android.widgets.datetime.DatePickerDetails;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

public class FixedDatePickerDialog extends DialogFragment {
    private ThemeUtils themeUtils;
    private DateTimeViewModel viewModel;
    private CustomDatePickerDialog.DateChangeListener dateChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        themeUtils = new ThemeUtils(context);

        if (context instanceof CustomDatePickerDialog.DateChangeListener) {
            dateChangeListener = (CustomDatePickerDialog.DateChangeListener) context;
        }

        viewModel = new ViewModelProvider(this).get(DateTimeViewModel.class);
        viewModel.setDialogTheme(getArguments().getInt(DateTimeWidgetUtils.DIALOG_THEME));
        viewModel.setLocalDateTime((LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.DATE));
        viewModel.setDatePickerDetails((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS));

        viewModel.getSelectedDate().observe(this, localDateTime -> {
            if (localDateTime != null) {
                dateChangeListener.onDateChanged(localDateTime);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatePickerDialog dialog = new DatePickerDialog(requireActivity(), viewModel.getDialogTheme(), viewModel.getDateSetListener(),
                viewModel.getLocalDateTime().getYear(), viewModel.getLocalDateTime().getMonthOfYear() - 1, viewModel.getLocalDateTime().getDayOfMonth());

        if (themeUtils.isSpinnerDatePickerDialogTheme(viewModel.getDialogTheme())) {
            dialog.setTitle(requireContext().getString(org.odk.collect.strings.R.string.select_date));
            hidePickersIfNeeded(dialog, viewModel.getLocalDateTime());

            //noinspection deprecation
            dialog.getDatePicker().setCalendarViewShown(false);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Needed because the DatePickerDialog doesn't pick up theme colors properly for some reason
        DatePickerDialog dialog = (DatePickerDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(new ThemeUtils(getContext()).getColorPrimary());
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(new ThemeUtils(getContext()).getColorPrimary());
    }

    private void hidePickersIfNeeded(DatePickerDialog dialog, LocalDateTime date) {
        if (viewModel.getDatePickerDetails().isYearMode()) {
            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);

            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("month", "id", "android"))
                    .setVisibility(View.GONE);
            dialog.getDatePicker().updateDate(date.getYear(), 0, 1);
        } else if (viewModel.getDatePickerDetails().isMonthYearMode()) {
            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
            dialog.getDatePicker().updateDate(date.getYear(), date.getMonthOfYear() - 1, 1);
        }
    }
}
