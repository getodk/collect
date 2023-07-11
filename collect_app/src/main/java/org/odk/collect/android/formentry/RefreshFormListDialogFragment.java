package org.odk.collect.android.formentry;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.material.MaterialProgressDialogFragment;

public class RefreshFormListDialogFragment extends MaterialProgressDialogFragment {

    protected RefreshFormListDialogFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RefreshFormListDialogFragmentListener) {
            listener = (RefreshFormListDialogFragmentListener) context;
        }
        setTitle(getString(org.odk.collect.strings.R.string.downloading_data));
        setMessage(getString(org.odk.collect.strings.R.string.please_wait));
        setCancelable(false);
    }

    @Override
    protected String getCancelButtonText() {
        return getString(org.odk.collect.strings.R.string.cancel_loading_form);
    }

    @Override
    protected OnCancelCallback getOnCancelCallback() {
        return () -> {
            listener.onCancelFormLoading();
            dismiss();
            return true;
        };
    }

    public interface RefreshFormListDialogFragmentListener {
            void onCancelFormLoading();
    }
}
