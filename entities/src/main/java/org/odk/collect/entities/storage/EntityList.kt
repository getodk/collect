package org.odk.collect.entities.storage

data class EntityList(val name: String, val hash: String? = null, val needsApproval: Boolean = false, val lastUpdated: Long? = null)
