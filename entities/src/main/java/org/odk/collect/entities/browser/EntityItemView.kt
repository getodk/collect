package org.odk.collect.entities.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import androidx.core.view.isVisible
import org.odk.collect.entities.databinding.EntityItemLayoutBinding
import org.odk.collect.entities.storage.Entity
import org.odk.collect.material.Pill

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

        binding.offlinePill.setContent {
            Pill(
                text = stringResource(org.odk.collect.strings.R.string.offline),
                icon = org.odk.collect.icons.R.drawable.ic_baseline_wifi_off_24,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }
    }
}
