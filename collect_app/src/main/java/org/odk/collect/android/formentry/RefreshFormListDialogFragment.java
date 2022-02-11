package org.odk.collect.android.formentry;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.material.MaterialProgressDialogFragmentNew;

public class RefreshFormListDialogFragment extends MaterialProgressDialogFragmentNew {

    protected RefreshFormListDialogFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RefreshFormListDialogFragmentListener) {
            listener = (RefreshFormListDialogFragmentListener) context;
        }
        setTitle(getString(R.string.downloading_data));
        setMessage(getString(R.string.please_wait));
        setCancelable(false);
        setNegativeButtonTitle(getString(R.string.cancel_loading_form));
        setNegativeButtonListener((dialogInterface, i) -> {
            listener.onCancelFormLoading();
            dismiss();
        });
    }

    public interface RefreshFormListDialogFragmentListener {
            void onCancelFormLoading();
    }
}
