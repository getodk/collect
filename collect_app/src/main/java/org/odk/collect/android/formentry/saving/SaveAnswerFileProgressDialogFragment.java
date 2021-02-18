package org.odk.collect.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;

public class SaveAnswerFileProgressDialogFragment extends ProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setMessage(getString(R.string.saving_file));
    }
}
