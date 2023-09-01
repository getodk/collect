package org.odk.collect.android.instancemanagement.send

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.android.databinding.ReadyToSendBannerBinding
import org.odk.collect.shared.TimeInMs
import org.odk.collect.strings.R

class ReadyToSendBanner(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = ReadyToSendBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun init(viewModel: ReadyToSendViewModel, owner: LifecycleOwner) {
        viewModel.data.observe(owner) {
            if (it.numberOfSentInstances > 0 && it.numberOfInstancesReadyToSend > 0) {
                if (it.lastInstanceSentTimeMillis >= TimeInMs.ONE_DAY) {
                    val days: Int = (it.lastInstanceSentTimeMillis / TimeInMs.ONE_DAY).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_days_ago, days, days)
                } else if (it.lastInstanceSentTimeMillis >= TimeInMs.ONE_HOUR) {
                    val hours: Int = (it.lastInstanceSentTimeMillis / TimeInMs.ONE_HOUR).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_hours_ago, hours, hours)
                } else if (it.lastInstanceSentTimeMillis >= TimeInMs.ONE_MINUTE) {
                    val minutes: Int = (it.lastInstanceSentTimeMillis / TimeInMs.ONE_MINUTE).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_minutes_ago, minutes, minutes)
                } else {
                    val seconds: Int = (it.lastInstanceSentTimeMillis / TimeInMs.ONE_SECOND).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_seconds_ago, seconds, seconds)
                }

                binding.subtext.text = context.resources.getQuantityString(R.plurals.forms_ready_to_send, it.numberOfInstancesReadyToSend, it.numberOfInstancesReadyToSend)
                binding.banner.visibility = VISIBLE
            }
        }

        viewModel.init()
    }
}
