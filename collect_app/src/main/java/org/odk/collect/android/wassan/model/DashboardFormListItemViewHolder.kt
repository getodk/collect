package org.odk.collect.android.wassan.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.databinding.DashboardFormListItemBinding

import org.odk.collect.android.formlists.blankformlist.BlankFormListItem
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFormListItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    DashboardFormListItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ).root
) {

    val binding = DashboardFormListItemBinding.bind(this.itemView)

    var dashboardFormListItem: BlankFormListItem? = null
        set(value) {
            field = value

            field?.let {
                binding.formTitle.text = it.formName

                binding.formVersion.text =
                    binding.root.context.getString(org.odk.collect.strings.R.string.version_number, it.formVersion)
                binding.formVersion.visibility =
                    if (it.formVersion.isNotBlank()) View.VISIBLE else View.GONE

                binding.formId.text =
                    binding.root.context.getString(org.odk.collect.strings.R.string.id_number, it.formId)

                binding.formHistory.text = try {
                    if (it.dateOfLastDetectedAttachmentsUpdate != null) {
                        SimpleDateFormat(
                            binding.root.context.getString(org.odk.collect.strings.R.string.updated_on_date_at_time),
                            Locale.getDefault()
                        ).format(it.dateOfLastDetectedAttachmentsUpdate)
                    } else {
                        SimpleDateFormat(
                            binding.root.context.getString(org.odk.collect.strings.R.string.added_on_date_at_time),
                            Locale.getDefault()
                        ).format(it.dateOfCreation)
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.e(e)
                    ""
                }
            }
        }

    fun setTrailingView(layoutId: Int) {
        FrameLayout.inflate(itemView.context, layoutId, binding.trailingView)
    }
}
