package org.odk.collect.android.instancemanagement.send

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import org.odk.collect.android.databinding.ReadyToSendBannerBinding
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.strings.R
import java.util.function.Supplier

const val ONE_SECOND = 1000L
const val ONE_MINUTE = 60000L
const val ONE_HOUR = 3600000L
const val ONE_DAY = 86400000L

class ReadyToSendBanner(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = ReadyToSendBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun init(instancesRepository: InstancesRepository, clock: Supplier<Long>) {
        val sentInstances: List<Instance> = instancesRepository.getAllByStatus(Instance.STATUS_SUBMITTED)
        val numberOfInstancesReadyToSend = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        if (sentInstances.isNotEmpty() && numberOfInstancesReadyToSend > 0) {
            val lastSentInstance = sentInstances.maxBy { instance -> instance.lastStatusChangeDate }
            val millisecondsAgo = clock.get() - lastSentInstance.lastStatusChangeDate
            if (millisecondsAgo >= ONE_DAY) {
                val days: Int = (millisecondsAgo / ONE_DAY).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_days_ago, days, days)
            } else if (millisecondsAgo >= ONE_HOUR) {
                val hours: Int = (millisecondsAgo / ONE_HOUR).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_hours_ago, hours, hours)
            } else if (millisecondsAgo >= ONE_MINUTE) {
                val minutes: Int = (millisecondsAgo / ONE_MINUTE).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_minutes_ago, minutes, minutes)
            } else {
                val seconds: Int = (millisecondsAgo / ONE_SECOND).toInt()
                binding.title.text = context.resources.getQuantityString(R.plurals.last_form_sent_seconds_ago, seconds, seconds)
            }

            binding.subtext.text = context.resources.getQuantityString(R.plurals.forms_ready_to_send, numberOfInstancesReadyToSend, numberOfInstancesReadyToSend)
            binding.banner.visibility = VISIBLE
        }
    }
}
