package org.odk.collect.forms.savepoints

interface SavepointsRepository {
    fun get(formDbId: Long, instanceDbId: Long?): Savepoint?

    fun getAll(): List<Savepoint>

    fun save(savepoint: Savepoint)

    fun delete(formDbId: Long, instanceDbId: Long?)

    fun deleteAll()
}
