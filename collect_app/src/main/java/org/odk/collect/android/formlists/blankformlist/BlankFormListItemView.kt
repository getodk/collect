package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.odk.collect.android.databinding.BlankFormListItemBinding
import org.odk.collect.strings.R.string
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class BlankFormListItemView(context: Context) : FrameLayout(context) {

    val binding = BlankFormListItemBinding.inflate(LayoutInflater.from(context), this, true)

    var blankFormListItem: BlankFormListItem? = null
        set(value) {
            field = value

            field?.let {
                binding.formTitle.text = it.formName

                binding.formVersion.text =
                    binding.root.context.getString(
                        string.version_number,
                        it.formVersion
                    )
                binding.formVersion.visibility =
                    if (it.formVersion.isNotBlank()) View.VISIBLE else View.GONE

                binding.formId.text =
                    binding.root.context.getString(
                        string.id_number,
                        it.formId
                    )

                binding.formHistory.text = try {
                    if (it.dateOfLastDetectedAttachmentsUpdate != null) {
                        SimpleDateFormat(
                            binding.root.context.getString(string.updated_on_date_at_time),
                            Locale.getDefault()
                        ).format(it.dateOfLastDetectedAttachmentsUpdate)
                    } else {
                        SimpleDateFormat(
                            binding.root.context.getString(string.added_on_date_at_time),
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
        inflate(context, layoutId, binding.trailingView)
    }
}
