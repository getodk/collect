package org.odk.collect.formstest

import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import java.io.File

class InMemSavepointsRepository : SavepointsRepository {
    private val savepoints = mutableListOf<Savepoint>()

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        return savepoints.find { savepoint -> savepoint.formDbId == formDbId && savepoint.instanceDbId == instanceDbId }
    }

    override fun getAll(): List<Savepoint> {
        return savepoints
    }

    override fun save(savepoint: Savepoint) {
        if (savepoints.any { it.formDbId == savepoint.formDbId && it.instanceDbId == savepoint.instanceDbId }) {
            return
        }
        savepoints.add(savepoint)
        savepoints.indexOf(savepoint).toLong()
    }

    override fun delete(formDbId: Long, instanceDbId: Long?) {
        val savepoint = get(formDbId, instanceDbId)
        if (savepoint != null) {
            File(savepoint.savepointFilePath).delete()
            savepoints.remove(get(formDbId, instanceDbId))
        }
    }

    override fun deleteAll() {
        savepoints.forEach {
            File(it.savepointFilePath).delete()
        }
        savepoints.clear()
    }
}
