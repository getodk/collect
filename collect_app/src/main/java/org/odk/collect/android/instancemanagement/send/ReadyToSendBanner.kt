package org.odk.collect.android.instancemanagement.send

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import org.odk.collect.android.databinding.ReadyToSendBannerBinding
import org.odk.collect.shared.TimeInMs
import org.odk.collect.strings.R

class ReadyToSendBanner(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = ReadyToSendBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun setData(data: ReadyToSendViewModel.Data) {
        if (data.numberOfSentInstances > 0 && data.numberOfInstancesReadyToSend > 0) {
            if (data.lastInstanceSentTimeMillis >= TimeInMs.ONE_DAY) {
                val days: Int = (data.lastInstanceSentTimeMillis / TimeInMs.ONE_DAY).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_days_ago, days, days)
            } else if (data.lastInstanceSentTimeMillis >= TimeInMs.ONE_HOUR) {
                val hours: Int = (data.lastInstanceSentTimeMillis / TimeInMs.ONE_HOUR).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_hours_ago, hours, hours)
            } else if (data.lastInstanceSentTimeMillis >= TimeInMs.ONE_MINUTE) {
                val minutes: Int = (data.lastInstanceSentTimeMillis / TimeInMs.ONE_MINUTE).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_minutes_ago, minutes, minutes)
            } else {
                val seconds: Int = (data.lastInstanceSentTimeMillis / TimeInMs.ONE_SECOND).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_seconds_ago, seconds, seconds)
            }

            binding.subtext.text = context.resources.getQuantityString(R.plurals.forms_ready_to_send, data.numberOfInstancesReadyToSend, data.numberOfInstancesReadyToSend)
            binding.banner.visibility = VISIBLE
        }
    }
}
