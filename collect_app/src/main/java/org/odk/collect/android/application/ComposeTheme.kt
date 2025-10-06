package org.odk.collect.android.application

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.ComposeThemeProvider

/**
 * Changes to this theme should also be made in `theme.xml`
 */
@Composable
fun CollectTheme(content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = colorResource(R.color.colorPrimaryLight),
        onPrimary = colorResource(R.color.colorOnPrimaryLight),
        surface = colorResource(R.color.colorSurfaceLight),
        primaryContainer = colorResource(R.color.colorPrimaryContainerLight),
        onPrimaryContainer = colorResource(R.color.colorOnPrimaryContainerLight)
    )
    val darkColors = darkColorScheme(
        primary = colorResource(R.color.colorPrimaryDark),
        onPrimary = colorResource(R.color.colorOnPrimaryDark),
        surface = colorResource(R.color.colorSurfaceDark),
        primaryContainer = colorResource(R.color.colorPrimaryContainerDark),
        onPrimaryContainer = colorResource(R.color.colorOnPrimaryContainerDark)
    )
    val colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors

    val typography = Typography(
        bodyMedium = MaterialTheme.typography.bodyMedium
    )

    val shapes = Shapes(
        small = MaterialTheme.shapes.small,
        medium = RoundedCornerShape(dimensionResource(R.dimen.mediumCornerSize)),
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
        CollectTheme { content() }
    }
}
