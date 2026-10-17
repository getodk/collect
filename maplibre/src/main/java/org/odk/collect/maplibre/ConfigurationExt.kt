package org.odk.collect.maplibre

import org.odk.collect.shared.settings.Settings

fun Configuration.basemapStyle(settings: Settings): String? {
    return styleSetting?.let { settings.getString(it) }
}

fun Configuration.basemapUri(style: String?): BasemapUri {
    return if (uri != null) {
        uri
    } else if (styleSetting != null) {
        styleOptions.getValue(style!!).uri
    } else {
        throw IllegalArgumentException("Invalid Configuration!")
    }
}
