package org.odk.collect.entities.debug

import org.odk.collect.entities.analytics.AnalyticsEvents
import org.odk.collect.entities.javarosa.finalization.FormEntity
import org.odk.collect.shared.debug.DebugEvent

sealed class EntityEvent(override val analyticsEvent: String, private val action: String) : DebugEvent {

    abstract val formEntity: FormEntity

    override val message: String
        get() = "Failed to $action dataset=${formEntity.dataset}, id=${formEntity.id}, label=${formEntity.label}"

    data class CreateNoLabel(override val formEntity: FormEntity) :
        EntityEvent(AnalyticsEvents.ENTITY_CREATE_NO_LABEL, "create")

    data class UpdateNoMatch(override val formEntity: FormEntity) :
        EntityEvent(AnalyticsEvents.ENTITY_UPDATE_NO_MATCH, "update")

    data class NoId(override val formEntity: FormEntity) :
        EntityEvent(AnalyticsEvents.ENTITY_WITH_NO_ID, "create/update")

    data class InvalidId(override val formEntity: FormEntity) :
        EntityEvent(AnalyticsEvents.ENTITY_WITH_INVALID_ID, "create/update")
}
