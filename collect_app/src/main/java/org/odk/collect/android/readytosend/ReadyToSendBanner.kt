package org.odk.collect.android.readytosend

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import org.odk.collect.android.databinding.ReadyToSendBannerBinding
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.strings.R

private const val ONE_SECOND = 1000
private const val ONE_MINUTE = 60000
private const val ONE_HOUR = 3600000
private const val ONE_DAY = 86400000

class ReadyToSendBanner(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    constructor(context: Context) : this(context, null)

    private val binding = ReadyToSendBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun init(instancesRepository: InstancesRepository) {
        val sentInstances: List<Instance> = instancesRepository.getAllByStatus(Instance.STATUS_SUBMITTED)
        val numberOfInstancesReadyToSend = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        if (sentInstances.isNotEmpty() && numberOfInstancesReadyToSend > 0) {
            val lastSentInstance = sentInstances.maxBy { instance -> instance.lastStatusChangeDate }
            val millisecondsAgo = System.currentTimeMillis() - lastSentInstance.lastStatusChangeDate
            if (millisecondsAgo >= ONE_DAY) {
                binding.title.text = context.getString(R.string.last_form_sent_days_ago, millisecondsAgo / ONE_DAY)
            } else if (millisecondsAgo >= ONE_HOUR) {
                binding.title.text = context.getString(R.string.last_form_sent_hours_ago, millisecondsAgo / ONE_HOUR)
            } else if (millisecondsAgo >= ONE_MINUTE) {
                binding.title.text = context.getString(R.string.last_form_sent_minutes_ago, millisecondsAgo / ONE_MINUTE)
            } else {
                binding.title.text = context.getString(R.string.last_form_sent_seconds_ago, millisecondsAgo / ONE_SECOND)
            }

            binding.subtext.text = context.getString(R.string.forms_ready_to_send, numberOfInstancesReadyToSend)
            binding.banner.visibility = VISIBLE
        }
    }
}
