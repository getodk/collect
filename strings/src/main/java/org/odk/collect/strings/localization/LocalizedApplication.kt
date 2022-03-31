package org.odk.collect.strings.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
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

    val newConfig = Configuration(resources.configuration).apply {
        setLocale(locale)
    }

    return createConfigurationContext(newConfig)
        .resources
        .getString(stringId, *formatArgs)
}
