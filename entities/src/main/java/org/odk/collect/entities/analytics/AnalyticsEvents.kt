package org.odk.collect.entities.analytics

object AnalyticsEvents {
    /**
     * Tracks how often an entity update is attempted but no entity with a matching ID is found.
     */
    const val ENTITY_UPDATE_NO_MATCH = "EntityUpdateNoMatch"

    /**
     * Tracks how often an entity creation is attempted but the label is blank.
     */
    const val ENTITY_CREATE_NO_LABEL = "EntityCreateNoLabel"

    /**
     * Tracks how often an entity is defined in a form but has no ID.
     */
    const val ENTITY_WITH_NO_ID = "EntityWithNoId"

    /**
     * Tracks how often an entity is defined in a form but has an invalid ID (not a V4 UUID).
     */
    const val INVALID_ENTITY = "InvalidEntity"
}
