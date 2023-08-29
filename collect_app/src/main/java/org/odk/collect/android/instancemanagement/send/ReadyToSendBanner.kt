package org.odk.collect.android.instancemanagement.send

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.android.databinding.ReadyToSendBannerBinding
import org.odk.collect.strings.R

const val ONE_SECOND = 1000L
const val ONE_MINUTE = 60000L
const val ONE_HOUR = 3600000L
const val ONE_DAY = 86400000L

class ReadyToSendBanner(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = ReadyToSendBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun init(viewModel: ReadyToSendViewModel, owner: LifecycleOwner) {
        viewModel.data.observe(owner) {
            if (it.numberOfSentInstances > 0 && it.numberOfInstancesReadyToSend > 0) {
                if (it.lastInstanceSentTimeMillis >= ONE_DAY) {
                    val days: Int = (it.lastInstanceSentTimeMillis / ONE_DAY).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_days_ago, days, days)
                } else if (it.lastInstanceSentTimeMillis >= ONE_HOUR) {
                    val hours: Int = (it.lastInstanceSentTimeMillis / ONE_HOUR).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_hours_ago, hours, hours)
                } else if (it.lastInstanceSentTimeMillis >= ONE_MINUTE) {
                    val minutes: Int = (it.lastInstanceSentTimeMillis / ONE_MINUTE).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_minutes_ago, minutes, minutes)
                } else {
                    val seconds: Int = (it.lastInstanceSentTimeMillis / ONE_SECOND).toInt()
                    binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_seconds_ago, seconds, seconds)
                }

                binding.subtext.text = context.resources.getQuantityString(R.plurals.forms_ready_to_send, it.numberOfInstancesReadyToSend, it.numberOfInstancesReadyToSend)
                binding.banner.visibility = VISIBLE
            }
        }

        viewModel.init()
    }
}
