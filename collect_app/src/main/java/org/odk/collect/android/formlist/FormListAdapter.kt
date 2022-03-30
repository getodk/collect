package org.odk.collect.android.formlist

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormListItemBinding
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class FormListAdapter : RecyclerView.Adapter<FormListAdapter.ViewHolder>() {
    private var formItems = emptyList<FormListItem>()
    lateinit var listener: OnFormItemClickListener

    private lateinit var binding: FormListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FormListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(formItems[position])
    }

    override fun getItemCount() = formItems.size

    inner class ViewHolder(private val binding: FormListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FormListItem) {
            binding.apply {
                formTitle.text = item.formName

                formSubtitle.text = binding.root.context.getString(R.string.version_number, item.formVersion)
                formSubtitle.visibility = if (item.formVersion.isNotBlank()) View.VISIBLE else View.GONE

                formSubtitle2.text = getSubtitle2Text(binding.root.context, item.dateOfCreation)

                mapButton.visibility = if (item.geometryPath.isNotBlank()) View.VISIBLE else View.GONE

                root.setOnClickListener {
                    if (MultiClickGuard.allowClick(javaClass.name)) {
                        listener.onFormClick(item.contentUri)
                    }
                }

                mapButton.setOnClickListener {
                    if (MultiClickGuard.allowClick(javaClass.name)) {
                        listener.onMapButtonClick(item.databaseId)
                    }
                }
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
    }

    fun setData(formItems: List<FormListItem>) {
        this.formItems = formItems.toList()
        notifyDataSetChanged()
    }
}

interface OnFormItemClickListener {
    fun onFormClick(formUri: Uri)

    fun onMapButtonClick(id: Long)
}
