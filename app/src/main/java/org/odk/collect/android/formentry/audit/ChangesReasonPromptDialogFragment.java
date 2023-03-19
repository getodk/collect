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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.material.MaterialFullScreenDialogFragment;

public class ChangesReasonPromptDialogFragment extends MaterialFullScreenDialogFragment {

    private FormSaveViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changes_reason_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = getToolbar();
        toolbar.setTitle(viewModel.getFormName());
        toolbar.inflateMenu(R.menu.changes_reason_dialog);

        EditText reasonField = view.findViewById(R.id.reason);
        reasonField.setText(viewModel.getReason());
        reasonField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setReason(editable.toString());
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            viewModel.resumeSave();
            return true;
        });

        reasonField.requestFocus();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity()).get(FormSaveViewModel.class);
    }

    @Override
    protected void onBackPressed() {
        dismiss();
    }

    @Override
    protected Toolbar getToolbar() {
        return getView().findViewById(R.id.toolbar);
    }

    @Override
    protected void onCloseClicked() {
        dismiss();
    }

    @Override
    protected boolean shouldShowSoftKeyboard() {
        return true;
    }
}
