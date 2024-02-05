package org.odk.collect.formstest

import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository

class InMemSavepointsRepository : SavepointsRepository {
    private val savepoints = mutableListOf<Savepoint>()

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        return savepoints.find { savepoint -> savepoint.formDbId == formDbId && savepoint.instanceDbId == instanceDbId }
    }

    override fun getAll(): List<Savepoint> {
        return savepoints
    }

    override fun save(savepoint: Savepoint) {
        savepoints.add(savepoint)
        savepoints.indexOf(savepoint).toLong()
    }

    override fun delete(formDbId: Long, instanceDbId: Long?) {
        savepoints.remove(get(formDbId, instanceDbId))
    }

    override fun deleteAll() {
        savepoints.clear()
    }
}
