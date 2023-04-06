package org.odk.collect.android.formlists.blankformlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.BlankFormListItemBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class BlankFormListItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    BlankFormListItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ).root
) {

    val binding = BlankFormListItemBinding.bind(this.itemView)

    var blankFormListItem: BlankFormListItem? = null
        set(value) {
            field = value

            field?.let {
                binding.formTitle.text = it.formName

                binding.formSubtitle.text =
                    binding.root.context.getString(R.string.version_number, it.formVersion)
                binding.formSubtitle.visibility =
                    if (it.formVersion.isNotBlank()) View.VISIBLE else View.GONE

                binding.formSubtitle2.text = try {
                    if (it.dateOfLastDetectedAttachmentsUpdate != null) {
                        SimpleDateFormat(
                            binding.root.context.getString(R.string.updated_on_date_at_time),
                            Locale.getDefault()
                        ).format(it.dateOfLastDetectedAttachmentsUpdate)
                    } else {
                        SimpleDateFormat(
                            binding.root.context.getString(R.string.added_on_date_at_time),
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
