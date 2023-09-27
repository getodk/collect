package org.odk.collect.android.instancemanagement

import android.content.Context
import android.content.res.Resources
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.R
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Instance.getStatusDescription(resources: Resources): String {
    return getStatusDescription(resources, status, Date(lastStatusChangeDate))
}

fun getStatusDescription(context: Context, state: String?, date: Date): String {
    return getStatusDescription(context.resources, state, date)
}

private fun getStatusDescription(resources: Resources, state: String?, date: Date): String {
    return try {
        if (state == null) {
            SimpleDateFormat(
                resources.getString(R.string.added_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (Instance.STATUS_INCOMPLETE.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.saved_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (Instance.STATUS_COMPLETE.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.finalized_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (Instance.STATUS_SUBMITTED.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.sent_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (Instance.STATUS_SUBMISSION_FAILED.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.sending_failed_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (Instance.STATUS_INVALID.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.saved_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else {
            SimpleDateFormat(
                resources.getString(R.string.added_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        }
    } catch (e: IllegalArgumentException) {
        Timber.e(e, "Current locale: %s", Locale.getDefault())
        ""
    }
}
