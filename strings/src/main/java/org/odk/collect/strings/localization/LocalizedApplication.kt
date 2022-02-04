package org.odk.collect.strings.localization

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

interface LocalizedApplication {

    val locale: Locale
}

fun Context.getLocalizedString(stringId: Int, vararg formatArgs: Any): String {
    return when (applicationContext) {
        is LocalizedApplication -> {
            val localizedApplication = applicationContext as LocalizedApplication

            val newConfig = Configuration(resources.configuration).apply {
                setLocale(localizedApplication.locale)
            }

            val localizedContext = createConfigurationContext(newConfig)
            localizedContext.resources.getString(stringId, *formatArgs)
        }

        else -> {
            // Don't explode if the application doesn't implement LocalizedApplication. Useful
            // when testing modules in isolation
            getString(stringId, formatArgs)
        }
    }
}
