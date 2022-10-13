package org.odk.collect.entities

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.entities.databinding.EntityItemLayoutBinding

class EntityItemView(context: Context) : FrameLayout(context) {

    private val binding = EntityItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun setEntity(entity: Entity) {
        binding.properties.text = entity.properties
            .sortedBy { it.first }
            .joinToString(separator = ", ") { "${it.first}: ${it.second}" }
    }
}
