package org.odk.collect.entities.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.odk.collect.entities.storage.Entity
import org.odk.collect.material.Pill

@Composable
fun EntityItem(modifier: Modifier = Modifier, entity: Entity.Saved) {
    val marginStandard =
        dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)

    Column(
        modifier
            .fillMaxWidth()
            .padding(marginStandard)
    ) {
        val marginSmall = dimensionResource(org.odk.collect.androidshared.R.dimen.margin_small)
        val marginExtraSmall =
            dimensionResource(org.odk.collect.androidshared.R.dimen.margin_extra_small)

        val offline = entity.state == Entity.State.OFFLINE
        if (offline) {
            Pill(
                text = stringResource(org.odk.collect.strings.R.string.offline),
                icon = org.odk.collect.icons.R.drawable.ic_baseline_wifi_off_24,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }

        Text(
            text = entity.label ?: "",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = if (offline) marginSmall else 0.dp)
        )

        Text(
            text = "${entity.id} (${entity.version})",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = marginExtraSmall)
        )

        Text(
            text = entity.properties
                .sortedBy { it.first }
                .joinToString(separator = "\n") { "${it.first}: ${it.second}" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = marginExtraSmall)
        )
    }
}

@Preview
@Composable
private fun OfflinePreview() {
    MaterialTheme {
        EntityItem(
            entity = Entity.Saved(
                id = "1",
                label = "Entity 1",
                version = 1,
                state = Entity.State.OFFLINE,
                properties = listOf("Property 1" to "Value 1", "Property 2" to "Value 2"),
                index = 0
            )
        )
    }
}

@Preview
@Composable
private fun OnlinePreview() {
    MaterialTheme {
        EntityItem(
            entity = Entity.Saved(
                id = "1",
                label = "Entity 1",
                version = 1,
                state = Entity.State.ONLINE,
                properties = listOf("Property 1" to "Value 1", "Property 2" to "Value 2"),
                index = 0
            )
        )
    }
}
