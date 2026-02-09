package org.odk.collect.android.widgets.datetime.pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

public class CustomTimePickerDialog extends DialogFragment {
    private DateTimeViewModel viewModel;
    private TimeChangeListener timeChangeListener;

    public interface TimeChangeListener {
        void onTimeChanged(DateTime selectedTime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof TimeChangeListener) {
            timeChangeListener = (TimeChangeListener) context;
        }

        viewModel = new ViewModelProvider(this).get(DateTimeViewModel.class);
        viewModel.setLocalDateTime((LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.TIME));
        viewModel.setDialogTheme(getArguments().getInt(DateTimeWidgetUtils.DIALOG_THEME));

        viewModel.getSelectedTime().observe(this, dateTime -> {
            timeChangeListener.onTimeChanged(dateTime);
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        TimePickerDialog dialog = new TimePickerDialog(requireContext(), viewModel.getDialogTheme(), viewModel.getTimeSetListener(),
                viewModel.getLocalDateTime().getHourOfDay(), viewModel.getLocalDateTime().getMinuteOfHour(), DateFormat.is24HourFormat(requireContext()));

        dialog.setTitle(requireContext().getString(org.odk.collect.strings.R.string.select_time));
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Needed because the TimePickerDialog doesn't pick up theme colors properly for some reason
        TimePickerDialog dialog = (TimePickerDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(new ThemeUtils(getContext()).getColorPrimary());
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(new ThemeUtils(getContext()).getColorPrimary());
    }
}
