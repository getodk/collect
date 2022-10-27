package org.odk.collect.android.formentry.audit;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.material.MaterialFullScreenDialogFragment;

public class IdentifyUserPromptDialogFragment extends MaterialFullScreenDialogFragment {

    private IdentityPromptViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.identify_user_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getToolbar().setTitle(viewModel.getFormTitle());

        EditText identityField = view.findViewById(R.id.identity);
        identityField.setText(viewModel.getUser());

        identityField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setIdentity(editable.toString());
            }
        });

        identityField.setOnEditorActionListener((textView, i, keyEvent) -> {
            viewModel.done();
            return true;
        });

        identityField.requestFocus();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        viewModel = new ViewModelProvider(requireActivity()).get(IdentityPromptViewModel.class);
        viewModel.requiresIdentityToContinue().observe(this, requiresIdentity -> {
            if (!requiresIdentity) {
                dismiss();
            }
        });
    }

    @Override
    protected void onCloseClicked() {
        dismiss();
        viewModel.promptDismissed();
    }

    @Override
    protected void onBackPressed() {
        dismiss();
        viewModel.promptDismissed();
    }

    @Override
    protected Toolbar getToolbar() {
        return getView().findViewById(R.id.toolbar);
    }

    @Override
    protected boolean shouldShowSoftKeyboard() {
        return true;
    }
}
