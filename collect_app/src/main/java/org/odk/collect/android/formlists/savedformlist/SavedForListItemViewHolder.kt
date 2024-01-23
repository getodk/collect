package org.odk.collect.android.formlists.savedformlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.android.instancemanagement.getStatusDescription
import org.odk.collect.androidshared.ui.multiselect.MultiSelectAdapter
import org.odk.collect.forms.instances.Instance
import java.util.Date

class SavedForListItemViewHolder(parent: ViewGroup) : MultiSelectAdapter.ViewHolder<Instance>(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.form_chooser_list_item_multiple_choice, parent, false)
) {
    private val formTitle: TextView = itemView.findViewById(R.id.form_title)
    private val formSubtitle: TextView = itemView.findViewById(R.id.form_subtitle)
    private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    private val statusIcon: ImageView = itemView.findViewById(R.id.image)
    private val selectView: FrameLayout = itemView.findViewById(R.id.selectView)

    override fun setItem(item: Instance) {
        val lastStatusChangeDate = item.lastStatusChangeDate
        val status = item.status
        formTitle.text = item.displayName
        formSubtitle.text =
            getStatusDescription(formTitle.context, status, Date(lastStatusChangeDate))
        when (status) {
            Instance.STATUS_SUBMISSION_FAILED -> statusIcon.setImageResource(R.drawable.ic_form_state_submission_failed)
            Instance.STATUS_SUBMITTED -> statusIcon.setImageResource(R.drawable.ic_form_state_submitted)
            else -> statusIcon.setImageResource(R.drawable.ic_form_state_finalized)
        }
    }

    override fun getCheckbox(): CheckBox {
        return checkbox
    }

    override fun getSelectArea(): View {
        return selectView
    }
}
