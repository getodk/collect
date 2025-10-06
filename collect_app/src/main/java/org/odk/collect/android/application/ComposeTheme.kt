package org.odk.collect.android.application

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.ComposeThemeProvider

/**
 * Changes to this theme should also be made in `theme.xml`
 */
@Composable
fun CollectTheme(
    context: Context,
    content: @Composable () -> Unit
) {
    val resources = context.resources

    val lightColors = lightColorScheme(
        primary = Color(resources.getColor(R.color.colorPrimaryLight)),
        onPrimary = Color(resources.getColor(R.color.colorOnPrimaryLight)),
        surface = Color(resources.getColor(R.color.colorSurfaceLight)),
        primaryContainer = Color(resources.getColor(R.color.colorPrimaryContainerLight))
    )
    val darkColors = darkColorScheme(
        primary = Color(resources.getColor(R.color.colorPrimaryDark)),
        onPrimary = Color(resources.getColor(R.color.colorOnPrimaryDark)),
        surface = Color(resources.getColor(R.color.colorSurfaceDark)),
        primaryContainer = Color(resources.getColor(R.color.colorPrimaryContainerDark))
    )
    val colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors

    val typography = Typography(
        bodyMedium = MaterialTheme.typography.bodyMedium
    )

    val shapes = Shapes(
        small = MaterialTheme.shapes.small,
        medium = RoundedCornerShape(8.dp),
        large = MaterialTheme.shapes.large
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

interface CollectComposeThemeProvider : ComposeThemeProvider {
    @Composable
    override fun Theme(content: @Composable () -> Unit) {
        CollectTheme(this as Context) { content() }
    }
}
