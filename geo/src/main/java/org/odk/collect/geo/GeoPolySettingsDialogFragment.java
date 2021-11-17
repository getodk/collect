package org.odk.collect.geo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GeoPolySettingsDialogFragment extends DialogFragment {

    private static final int[] INTERVAL_OPTIONS = {
            1, 5, 10, 20, 30, 60, 300, 600, 1200, 1800
    };

    private static final int[] ACCURACY_THRESHOLD_OPTIONS = {
            0, 3, 5, 10, 15, 20
    };

    private View autoOptions;
    private RadioGroup radioGroup;
    protected SettingsDialogCallback callback;

    private int checkedRadioButtonId = -1;
    private int intervalIndex = -1;
    private int accuracyThresholdIndex = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof SettingsDialogCallback) {
            callback = (SettingsDialogCallback) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View settingsView = getActivity().getLayoutInflater().inflate(R.layout.geopoly_dialog, null);
        radioGroup = settingsView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            checkedRadioButtonId = checkedId;
            autoOptions.setVisibility(checkedId == R.id.automatic_mode ? View.VISIBLE : View.GONE);
        });

        autoOptions = settingsView.findViewById(R.id.auto_options);
        Spinner autoInterval = settingsView.findViewById(R.id.auto_interval);
        autoInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                intervalIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] options = new String[INTERVAL_OPTIONS.length];
        for (int i = 0; i < INTERVAL_OPTIONS.length; i++) {
            options[i] = formatInterval(INTERVAL_OPTIONS[i]);
        }
        populateSpinner(autoInterval, options);

        Spinner accuracyThreshold = settingsView.findViewById(R.id.accuracy_threshold);
        accuracyThreshold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                accuracyThresholdIndex = position;
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        options = new String[ACCURACY_THRESHOLD_OPTIONS.length];
        for (int i = 0; i < ACCURACY_THRESHOLD_OPTIONS.length; i++) {
            options[i] = formatAccuracyThreshold(ACCURACY_THRESHOLD_OPTIONS[i]);
        }
        populateSpinner(accuracyThreshold, options);

        if (checkedRadioButtonId == -1) {
            checkedRadioButtonId = callback.getCheckedId();
            intervalIndex = callback.getIntervalIndex();
            accuracyThresholdIndex = callback.getAccuracyThresholdIndex();

            radioGroup.check(checkedRadioButtonId);
            autoInterval.setSelection(intervalIndex);
            accuracyThreshold.setSelection(accuracyThresholdIndex);
        }

        return new MaterialAlertDialogBuilder(getActivity())
                .setTitle(getString(R.string.input_method))
                .setView(settingsView)
                .setPositiveButton(getString(R.string.start), (dialog, id) -> {
                    callback.updateRecordingMode(radioGroup.getCheckedRadioButtonId());
                    callback.setIntervalIndex(intervalIndex);
                    callback.setAccuracyThresholdIndex(accuracyThresholdIndex);
                    callback.startInput();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dismiss();
                })
                .create();
    }

    /** Formats a time interval as a whole number of seconds or minutes. */
    private String formatInterval(int seconds) {
        int minutes = seconds / 60;
        return minutes > 0
                ? getResources().getQuantityString(R.plurals.number_of_minutes, minutes, minutes)
                : getResources().getQuantityString(R.plurals.number_of_seconds, seconds, seconds);
    }

    /** Populates a Spinner with the option labels in the given array. */
    private void populateSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /** Formats an entry in the accuracy threshold dropdown. */
    private String formatAccuracyThreshold(int meters) {
        return meters > 0
                ? getResources().getQuantityString(R.plurals.number_of_meters, meters, meters)
                : getString(R.string.none);
    }

    public interface SettingsDialogCallback {
        void startInput();
        void updateRecordingMode(int checkedId);

        int getCheckedId();
        int getIntervalIndex();
        int getAccuracyThresholdIndex();

        void setIntervalIndex(int intervalIndex);
        void setAccuracyThresholdIndex(int accuracyThresholdIndex);
    }
}
