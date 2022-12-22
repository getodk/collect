package org.odk.collect.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.settings.SettingsProvider;

import javax.inject.Inject;

public class QuitFormDialogFragment extends DialogFragment {

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    private FormSaveViewModel formSaveViewModel;
    private FormEntryViewModel formEntryViewModel;
    private QuitFormDialog.Listener listener;

    private final ViewModelProvider.Factory viewModelFactory;

    public QuitFormDialogFragment(ViewModelProvider.Factory viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity(), viewModelFactory);
        formSaveViewModel = viewModelProvider.get(FormSaveViewModel.class);
        formEntryViewModel = viewModelProvider.get(FormEntryViewModel.class);

        if (context instanceof QuitFormDialog.Listener) {
            listener = (QuitFormDialog.Listener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        return QuitFormDialog.create(requireActivity(), formSaveViewModel, formEntryViewModel, settingsProvider, currentProjectProvider, listener);
    }
}
