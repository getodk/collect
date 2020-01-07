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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

public class ChangesReasonPromptDialogFragment extends MaterialFullScreenDialogFragment {

    public static final String TAG = "ChangesReasonPromptDialogFragment";
    private static final String ARG_FORM_NAME = "ArgFormName";
    private ChangesReasonPromptViewModel viewModel;

    public ViewModelProvider.Factory viewModelFactory = new ChangesReasonPromptViewModel.Factory();

    public void show(String formName, FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            Bundle bundle = new Bundle();
            bundle.putString(ChangesReasonPromptDialogFragment.ARG_FORM_NAME, formName);
            setArguments(bundle);
            show(fragmentManager.beginTransaction(), TAG);
        }
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

        reasonField.requestFocus();

        toolbar.setOnMenuItemClickListener(item -> {
            viewModel.saveReason(System.currentTimeMillis());
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(ChangesReasonPromptViewModel.class);
        viewModel.requiresReasonToContinue().observe(this, requiresReason -> {
            if (!requiresReason) {
                dismiss();
            }
        });
    }

    @Override
    protected void onBackPressed() {
        viewModel.promptDismissed();
    }

    @Override
    protected void onCloseClicked() {
        viewModel.promptDismissed();
    }
}
