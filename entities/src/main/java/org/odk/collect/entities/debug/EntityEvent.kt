package org.odk.collect.entities.debug

import org.odk.collect.entities.javarosa.finalization.FormEntity

sealed class EntityEvent {

    abstract val formEntity: FormEntity

    data class CreateNoLabel(override val formEntity: FormEntity) : EntityEvent()

    data class UpdateNoMatch(override val formEntity: FormEntity) : EntityEvent()

    data class NoId(override val formEntity: FormEntity) : EntityEvent()

    data class InvalidId(override val formEntity: FormEntity) : EntityEvent()
}
