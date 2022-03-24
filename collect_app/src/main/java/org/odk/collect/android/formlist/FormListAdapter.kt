package org.odk.collect.android.formlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormListItemBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class FormListAdapter(private val formItems: List<FormListItem>) : RecyclerView.Adapter<FormListAdapter.ViewHolder>() {
    private lateinit var binding: FormListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FormListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.apply {
            val formItem = formItems[position]

            formTitle.text = formItem.formName

            formSubtitle.text = binding.root.context.getString(R.string.version_number, formItem.formVersion)
            formSubtitle.visibility = if (formItem.formVersion.isNotBlank()) View.VISIBLE else View.GONE

            formSubtitle2.text = getSubtitle2Text(binding.root.context, formItem.dateOfCreation)

            mapButton.visibility = if (formItem.geometryPath.isNotBlank()) View.VISIBLE else View.GONE
        }
    }

    private fun getSubtitle2Text(context: Context, date: Long): String {
        return try {
            SimpleDateFormat(context.getString(R.string.added_on_date_at_time), Locale.getDefault()).format(date)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            ""
        }
    }

    override fun getItemCount() = formItems.size

    class ViewHolder(binding: FormListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
