package org.odk.collect.entities.debug

import org.odk.collect.analytics.Analytics
import org.odk.collect.entities.BuildConfig
import org.odk.collect.entities.analytics.AnalyticsEvents
import org.odk.collect.shared.debug.DebugLogger
import java.io.File
import java.time.LocalDateTime

/**
 * A [DebugLogger] that writes each [EntityEvent] both as a line in a debug log file (in debug
 * builds) and as an analytics event.
 */
class EntitiesDebugLogger(private val file: File) : DebugLogger<EntityEvent> {

    override fun log(event: EntityEvent) {
        if (BuildConfig.DEBUG) {
            val line = "${LocalDateTime.now()} Entities \"${getLogMessage(event)}\"\n"
            file.appendText(line)
        }

        Analytics.log(getAnalyticsEvent(event), "form")
    }

    private fun getLogMessage(event: EntityEvent): String {
        val action = when (event) {
            is EntityEvent.CreateNoLabel -> "create"
            is EntityEvent.UpdateNoMatch -> "update"
            is EntityEvent.NoId, is EntityEvent.InvalidId -> "create/update"
        }

        val formEntity = event.formEntity
        return "Failed to $action dataset=${formEntity.dataset}, id=${formEntity.id}, label=${formEntity.label}"
    }

    private fun getAnalyticsEvent(event: EntityEvent): String {
        return when (event) {
            is EntityEvent.CreateNoLabel -> AnalyticsEvents.ENTITY_CREATE_NO_LABEL
            is EntityEvent.UpdateNoMatch -> AnalyticsEvents.ENTITY_UPDATE_NO_MATCH
            is EntityEvent.NoId -> AnalyticsEvents.ENTITY_WITH_NO_ID
            is EntityEvent.InvalidId -> AnalyticsEvents.ENTITY_WITH_INVALID_ID
        }
    }
}
