package org.odk.collect.entities.browser

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Visibility
import org.odk.collect.entities.storage.Entity
import org.odk.collect.material.Pill

@Composable
fun EntityItem(modifier: Modifier = Modifier, entity: Entity.Saved) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (offlinePill, label, id, properties) = createRefs()

        val marginStandard =
            dimensionResource(org.odk.collect.androidshared.R.dimen.margin_standard)
        val marginSmall = dimensionResource(org.odk.collect.androidshared.R.dimen.margin_small)
        val marginExtraSmall =
            dimensionResource(org.odk.collect.androidshared.R.dimen.margin_extra_small)

        val guidelineStart = createGuidelineFromStart(marginStandard)

        val offline = entity.state == Entity.State.OFFLINE
        Pill(
            modifier = Modifier
                .constrainAs(offlinePill) {
                    top.linkTo(parent.top, margin = marginStandard)
                    start.linkTo(guidelineStart)
                    visibility = if (offline) Visibility.Visible else Visibility.Gone
                },
            text = stringResource(org.odk.collect.strings.R.string.offline),
            icon = org.odk.collect.icons.R.drawable.ic_baseline_wifi_off_24,
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )

        Text(
            modifier = Modifier
                .constrainAs(label) {
                    top.linkTo(
                        offlinePill.bottom,
                        margin = marginSmall,
                        goneMargin = marginStandard
                    )
                    start.linkTo(guidelineStart)
                },
            text = entity.label ?: "",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            modifier = Modifier.constrainAs(id) {
                top.linkTo(label.bottom, margin = marginExtraSmall)
                start.linkTo(guidelineStart)
            },
            text = "${entity.id} (${entity.version})",
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            modifier = Modifier.constrainAs(properties) {
                top.linkTo(id.bottom, margin = marginExtraSmall)
                start.linkTo(guidelineStart)
                bottom.linkTo(parent.bottom, margin = marginStandard)
            },
            text = entity.properties
                .sortedBy { it.first }
                .joinToString(separator = "\n") { "${it.first}: ${it.second}" },
            style = MaterialTheme.typography.bodyMedium
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
