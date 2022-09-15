package org.odk.collect.android.geo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;

public class CompoundDialogFragment extends DialogFragment {

    private View autoOptions;
    private RadioGroup radioGroup;
    protected SettingsDialogCallback callback;

    private int checkedRadioButtonId = -1;
    private int intervalIndex = -1;
    private int accuracyThresholdIndex = -1;

    public static final String PIT_KEY = "pit_name";
    public static final String FAULT_KEY = "fault_name";
    public static final String FEATUREID_KEY = "feature_id";
    public static final String LABEL_KEY = "label";
    public static final String VALUE_KEY = "label";

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

        Bundle args = getArguments();
        String pitName = args.getString(PIT_KEY);
        String faultName = args.getString(FAULT_KEY);
        int featureId = args.getInt(FEATUREID_KEY);
        String label = args.getString(LABEL_KEY);
        String value = args.getString(VALUE_KEY);
        if(label == null || label.equals("")) {
            label = getString(R.string.smap_set_marker);
        }
        View settingsView = getActivity().getLayoutInflater().inflate(R.layout.geocompound_dialog, null);
        RadioButton rb_pit = settingsView.findViewById(R.id.gc_marker_pit);
        RadioButton rb_fault = settingsView.findViewById(R.id.gc_marker_fault);
        RadioButton rb_none = settingsView.findViewById(R.id.gc_marker_none);
        rb_pit.setText(pitName);
        rb_fault.setText(faultName);
        rb_none.setChecked(true);
        if(value != null) {
            if(value.equals("pit")) {
                rb_pit.setChecked(true);
            } else  if(value.equals("fault")) {
                rb_fault.setChecked(true);
            }
        }

        radioGroup = settingsView.findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            checkedRadioButtonId = checkedId;
        });

        if (checkedRadioButtonId == -1) {
            //checkedRadioButtonId = callback.getCheckedId();

            radioGroup.check(checkedRadioButtonId);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(label)
                .setView(settingsView)
                .setPositiveButton(getString(R.string.smap_apply), (dialog, id) -> {
                    callback.updateMarker(featureId, getMarkerType(radioGroup.getCheckedRadioButtonId()));
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dismiss();
                })
                .create();
    }

    public interface SettingsDialogCallback {
        void updateMarker(int id, String marker);
    }

    private String getMarkerType(int checkedId) {
        if(checkedId == R.id.gc_marker_pit) {
            return "pit";
        } else if(checkedId == R.id.gc_marker_fault) {
            return "fault";
        } else {
            return "none";
        }
    }
}