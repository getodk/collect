package org.odk.collect.android.formentry.saving;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveAnswerFileErrorDialogFragment extends DialogFragment {

    private final ViewModelProvider.Factory viewModelFactory;
    private FormSaveViewModel formSaveViewModel;

    public SaveAnswerFileErrorDialogFragment(ViewModelProvider.Factory viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity(), viewModelFactory);
        formSaveViewModel = viewModelProvider.get(FormSaveViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(org.odk.collect.strings.R.string.error_occured)
                .setMessage(getString(org.odk.collect.strings.R.string.answer_file_copy_failed_message, formSaveViewModel.getAnswerFileError().getValue()))
                .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
                .create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        formSaveViewModel.answerFileErrorDisplayed();
    }
}
