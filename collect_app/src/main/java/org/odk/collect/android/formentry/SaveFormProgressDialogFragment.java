package org.odk.collect.android.formentry;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;

public class SaveFormProgressDialogFragment extends ProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        setCancelable(false);
        setTitle(getString(R.string.saving_form));
        setMessage(getString(R.string.please_wait));
    }

    @Override
    protected Cancellable getCancellable() {
        return ViewModelProviders
                .of(getActivity(), new FormSaveViewModel.Factory())
                .get(FormSaveViewModel.class);
    }
}
