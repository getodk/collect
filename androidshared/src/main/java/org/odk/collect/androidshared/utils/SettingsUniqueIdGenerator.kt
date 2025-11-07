package org.odk.collect.androidshared.utils

import org.odk.collect.shared.settings.Settings

class SettingsUniqueIdGenerator(private val settings: Settings) : UniqueIdGenerator {

    override fun getInt(identifier: String): Int {
        return synchronized(this) {
            val identifierKey = identifierKey(identifier)

            if (settings.contains(identifierKey)) {
                settings.getInt(identifierKey)
            } else {
                val next = getNextInt()
                settings.save(identifierKey, next)
                next
            }
        }
    }

    private fun getNextInt(): Int {
        val next = if (settings.contains(NEXT_ID_KEY)) {
            settings.getInt(NEXT_ID_KEY)
        } else {
            1
        }

        settings.save(NEXT_ID_KEY, next + 1)
        return next
    }

    companion object {
        private const val KEY_PREFIX = "uniqueId"
        private const val NEXT_ID_KEY = "$KEY_PREFIX:next"
        private fun identifierKey(identifier: String) = "$KEY_PREFIX:identifier:$identifier"
    }
}
