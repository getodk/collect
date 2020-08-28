package org.odk.collect.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.android.injection.DaggerUtils;

import javax.inject.Inject;

import static org.odk.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.SAVING;

public class SaveFormProgressDialogFragment extends ProgressDialogFragment {

    @Inject
    Analytics analytics;

    private FormSaveViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        FormSaveViewModel.Factory factory = new FormSaveViewModel.Factory(requireActivity(), null, analytics);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(FormSaveViewModel.class);

        setCancelable(false);
        setTitle(getString(R.string.saving_form));

        viewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.getState() == SAVING && result.getMessage() != null) {
                setMessage(getString(R.string.please_wait) + "\n\n" + result.getMessage());
            } else {
                setMessage(getString(R.string.please_wait));
            }
        });
    }

    @Override
    protected Cancellable getCancellable() {
        return viewModel;
    }
}
