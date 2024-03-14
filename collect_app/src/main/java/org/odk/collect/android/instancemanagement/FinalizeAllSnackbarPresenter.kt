package org.odk.collect.android.instancemanagement

import android.content.Context
import android.view.View
import org.odk.collect.androidshared.ui.SnackbarUtils.SnackbarDetails
import org.odk.collect.androidshared.ui.SnackbarUtils.SnackbarPresenterObserver
import org.odk.collect.strings.R

class FinalizeAllSnackbarPresenter(parentView: View, private val context: Context) :
    SnackbarPresenterObserver<FinalizeAllResult>(parentView) {

    override fun getSnackbarDetails(value: FinalizeAllResult): SnackbarDetails {
        return if (value.unsupportedInstances) {
            SnackbarDetails(
                context.getString(
                    R.string.bulk_finalize_unsupported,
                    value.successCount
                )
            )
        } else if (value.failureCount == 0) {
            SnackbarDetails(
                context.resources.getQuantityString(
                    R.plurals.bulk_finalize_success,
                    value.successCount,
                    value.successCount
                )
            )
        } else if (value.successCount == 0) {
            SnackbarDetails(
                context.resources.getQuantityString(
                    R.plurals.bulk_finalize_failure,
                    value.failureCount,
                    value.failureCount
                )
            )
        } else {
            SnackbarDetails(
                context.getString(
                    R.string.bulk_finalize_partial_success,
                    value.successCount,
                    value.failureCount
                )
            )
        }
    }
}
