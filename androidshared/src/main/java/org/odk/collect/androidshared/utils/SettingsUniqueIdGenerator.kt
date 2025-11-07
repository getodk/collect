package org.odk.collect.androidshared.utils

import org.odk.collect.shared.settings.Settings

class SettingsUniqueIdGenerator(private val settings: Settings) : UniqueIdGenerator {

    override fun getInt(identifier: String): Int {
        return synchronized(this) {
            if (settings.contains(identifier)) {
                settings.getInt(identifier)
            } else {
                val next = getNextInt()
                settings.save(identifier, next)
                next
            }
        }
    }

    private fun getNextInt(): Int {
        val next = if (settings.contains("next")) {
            settings.getInt("next")
        } else {
            1
        }

        settings.save("next", next + 1)
        return next
    }
}
