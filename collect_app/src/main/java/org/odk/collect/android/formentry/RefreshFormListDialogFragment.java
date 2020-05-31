package org.odk.collect.android.formentry;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;

public class RefreshFormListDialogFragment extends ProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setTitle(getString(R.string.downloading_data));
        setMessage(Collect.getInstance().getString(R.string.please_wait));
    }
}
