package org.odk.collect.entities.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import org.odk.collect.entities.databinding.EntityItemLayoutBinding
import org.odk.collect.entities.storage.Entity

class EntityItemView(context: Context) : FrameLayout(context) {

    val binding = EntityItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("SetTextI18n")
    fun setEntity(entity: Entity.Saved) {
        binding.label.text = entity.label
        binding.id.text = "${entity.id} (${entity.version})"
        binding.properties.text = entity.properties
            .sortedBy { it.first }
            .joinToString(separator = "\n") { "${it.first}: ${it.second}" }
        binding.offlinePill.isVisible = entity.state == Entity.State.OFFLINE
    }
}
