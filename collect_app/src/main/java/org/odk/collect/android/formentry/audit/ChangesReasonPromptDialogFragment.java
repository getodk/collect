package org.odk.collect.android.formentry.audit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

public class ChangesReasonPromptDialogFragment extends MaterialFullScreenDialogFragment {

    public static final String TAG = "ChangesReasonPromptDialogFragment";
    private static final String ARG_FORM_NAME = "ArgFormName";

    public static ChangesReasonPromptDialogFragment create(String formName) {
        ChangesReasonPromptDialogFragment dialog = new ChangesReasonPromptDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ChangesReasonPromptDialogFragment.ARG_FORM_NAME, formName);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changes_reason_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().setTitle(getArguments().getString(ARG_FORM_NAME));
    }
}
