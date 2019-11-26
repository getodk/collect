package org.odk.collect.android.formentry.audit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

public class ChangesReasonPromptDialogFragment extends MaterialFullScreenDialogFragment {

    public static final String TAG = "ChangesReasonPromptDialogFragment";
    private static final String ARG_FORM_NAME = "ArgFormName";
    private ChangesReasonPromptViewModel viewModel;

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

        Toolbar toolbar = getToolbar();
        toolbar.setTitle(getArguments().getString(ARG_FORM_NAME));
        toolbar.inflateMenu(R.menu.changes_reason_dialog);

        toolbar.setOnMenuItemClickListener(item -> {
            String reason = view.<EditText>findViewById(R.id.reason).getText().toString();
            viewModel.setReason(reason);
            viewModel.save(System.currentTimeMillis());
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        viewModel = ViewModelProviders.of(requireActivity()).get(ChangesReasonPromptViewModel.class);
        viewModel.requiresReasonToContinue().observe(this, requiresIdentity -> {
            if (!requiresIdentity) {
                dismiss();
            }
        });
    }
}
