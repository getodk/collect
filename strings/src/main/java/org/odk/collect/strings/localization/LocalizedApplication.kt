package org.odk.collect.strings.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.view.View
import java.util.Locale

interface LocalizedApplication {

    val locale: Locale
}

fun Context.getLocalizedString(stringId: Int, vararg formatArgs: Any): String {
    val locale = when (applicationContext) {
        is LocalizedApplication -> (applicationContext as LocalizedApplication).locale

        // Don't explode if the application doesn't implement LocalizedApplication. Useful
        // when testing modules in isolation
        else -> if (Build.VERSION.SDK_INT >= 24) resources.configuration.locales[0] else resources.configuration.locale
    }

    return getLocalizedResources(locale).getString(stringId, *formatArgs)
}

fun Context.getLocalizedResources(locale: Locale): Resources {
    val newConfig = Configuration(resources.configuration).apply {
        setLocale(locale)
    }

    return createConfigurationContext(newConfig).resources
}

fun Context.getLocalizedQuantityString(stringId: Int, quantity: Int, vararg formatArgs: Any): String {
    val locale = when (applicationContext) {
        is LocalizedApplication -> (applicationContext as LocalizedApplication).locale

        // Don't explode if the application doesn't implement LocalizedApplication. Useful
        // when testing modules in isolation
        else -> if (Build.VERSION.SDK_INT >= 24) resources.configuration.locales[0] else resources.configuration.locale
    }

    val newConfig = Configuration(resources.configuration).apply {
        setLocale(locale)
    }

    return createConfigurationContext(newConfig)
        .resources
        .getQuantityString(stringId, quantity, *formatArgs)
}

fun Context.isLTR(): Boolean {
    return resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR
}
