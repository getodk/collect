package org.odk.collect.android.formentry;

import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;

public class SaveFormProgressDialogFragment extends ProgressDialogFragment {

    @Override
    protected Cancellable getCancellable() {
        return ViewModelProviders
                .of(getActivity(), new FormSaveViewModel.Factory())
                .get(FormSaveViewModel.class);
    }
}
