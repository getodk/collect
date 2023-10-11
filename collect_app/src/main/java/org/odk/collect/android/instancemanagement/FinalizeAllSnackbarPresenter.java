package org.odk.collect.android.instancemanagement;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import org.odk.collect.android.formmanagement.FinalizeAllResult;
import org.odk.collect.androidshared.ui.SnackbarUtils;

public class FinalizeAllSnackbarPresenter extends SnackbarUtils.SnackbarPresenterObserver<FinalizeAllResult> {
    private final Context context;

    public FinalizeAllSnackbarPresenter(@NonNull View parentView, Context context) {
        super(parentView);
        this.context = context;
    }

    @NonNull
    @Override
    public SnackbarUtils.SnackbarDetails getSnackbarDetails(FinalizeAllResult result) {
        if (result.getUnsupportedInstances()) {
            return new SnackbarUtils.SnackbarDetails(
                    context.getString(
                            org.odk.collect.strings.R.string.bulk_finalize_unsupported,
                            result.getSuccessCount()
                    )
            );
        } else if (result.getFailureCount() == 0) {
            return new SnackbarUtils.SnackbarDetails(
                    context.getResources().getQuantityString(
                            org.odk.collect.strings.R.plurals.bulk_finalize_success,
                            result.getSuccessCount(),
                            result.getSuccessCount()
                    )
            );
        } else if (result.getSuccessCount() == 0) {
            return new SnackbarUtils.SnackbarDetails(
                    context.getResources().getQuantityString(
                            org.odk.collect.strings.R.plurals.bulk_finalize_failure,
                            result.getFailureCount(),
                            result.getFailureCount()
                    )
            );
        } else {
            return new SnackbarUtils.SnackbarDetails(
                    context.getString(org.odk.collect.strings.R.string.bulk_finalize_partial_success, result.getSuccessCount(), result.getFailureCount())
            );
        }
    }
}
