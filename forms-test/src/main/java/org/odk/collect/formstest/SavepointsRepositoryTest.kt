package org.odk.collect.formstest

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.shared.TempFiles
import java.io.File

abstract class SavepointsRepositoryTest {
    private val cacheDirPath = TempFiles.createTempDir().absolutePath
    private val instancesDirPath = TempFiles.createTempDir().absolutePath

    abstract fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository

    private fun getSavepointFile(relativeFilePath: String): File {
        return File(cacheDirPath, relativeFilePath)
    }

    private fun getInstanceFile(relativeFilePath: String): File {
        return File(instancesDirPath, relativeFilePath)
    }

    @Test
    fun `get returns null if the database is empty`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        assertThat(savepointsRepository.get(1, null), equalTo(null))
    }

    @Test
    fun `get returns null if there is no savepoint for give formDbId and instanceDbId`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        savepointsRepository.save(Savepoint(1, 1, getSavepointFile("foo").absolutePath, getInstanceFile("foo").absolutePath))

        assertThat(savepointsRepository.get(1, 2), equalTo(null))
    }

    @Test
    fun `get returns savepoint if one with given formDbId and instanceDbId exists`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepoint1 = Savepoint(1, 1, getSavepointFile("foo").absolutePath, getInstanceFile("foo").absolutePath)
        val savepoint2 = Savepoint(1, 2, getSavepointFile("bar").absolutePath, getInstanceFile("bar").absolutePath)
        savepointsRepository.save(savepoint1)
        savepointsRepository.save(savepoint2)

        assertThat(savepointsRepository.get(1, 1), equalTo(savepoint1))
    }

    @Test
    fun `getAll returns an empty list if the database is empty`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        assertThat(savepointsRepository.getAll().isEmpty(), equalTo(true))
    }

    @Test
    fun `getAll returns all savepoints stored in the database`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepoint1 = Savepoint(1, null, getSavepointFile("foo").absolutePath, getInstanceFile("foo").absolutePath)
        val savepoint2 = Savepoint(1, 1, getSavepointFile("bar").absolutePath, getInstanceFile("bar").absolutePath)
        savepointsRepository.save(savepoint1)
        savepointsRepository.save(savepoint2)

        assertThat(savepointsRepository.getAll(), contains(savepoint1, savepoint2))
    }

    @Test
    fun `save does not save two savepoints with the same formDbId and instanceDbId`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepoint1 = Savepoint(1, null, getSavepointFile("foo").absolutePath, getInstanceFile("foo").absolutePath)
        savepointsRepository.save(savepoint1)
        savepointsRepository.save(Savepoint(1, null, getSavepointFile("bar2").absolutePath, getInstanceFile("bar2").absolutePath))

        assertThat(savepointsRepository.getAll(), contains(savepoint1))
    }

    @Test
    fun `delete removes savepoint from the database and its savepoint file for given formDbId and instanceDbId if instanceDbId is null`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepointFile1 = getSavepointFile("foo")
        savepointFile1.createNewFile()
        val savepoint1 = Savepoint(1, null, savepointFile1.absolutePath, getInstanceFile("foo").absolutePath)

        val savepointFile2 = getSavepointFile("bar")
        savepointFile2.createNewFile()
        val savepoint2 = Savepoint(2, null, savepointFile2.absolutePath, getInstanceFile("bar").absolutePath)

        savepointsRepository.save(savepoint1)
        savepointsRepository.save(savepoint2)

        savepointsRepository.delete(savepoint1.formDbId, savepoint1.instanceDbId)

        assertThat(savepointsRepository.getAll(), contains(savepoint2))
        assertThat(savepointFile1.exists(), equalTo(false))
        assertThat(savepointFile2.exists(), equalTo(true))
    }

    @Test
    fun `delete removes savepoint from the database and its savepoint file for given formDbId and instanceDbId if instanceDbId is not null`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepointFile1 = getSavepointFile("foo")
        savepointFile1.createNewFile()
        val savepoint1 = Savepoint(1, 1, savepointFile1.absolutePath, getInstanceFile("foo").absolutePath)

        val savepointFile2 = getSavepointFile("bar")
        savepointFile2.createNewFile()
        val savepoint2 = Savepoint(2, 1, savepointFile2.absolutePath, getInstanceFile("bar").absolutePath)

        savepointsRepository.save(savepoint1)
        savepointsRepository.save(savepoint2)

        savepointsRepository.delete(savepoint1.formDbId, savepoint1.instanceDbId)

        assertThat(savepointsRepository.getAll().size, equalTo(1))
        assertThat(savepointsRepository.getAll()[0].formDbId, equalTo(savepoint2.formDbId))
        assertThat(savepointFile1.exists(), equalTo(false))
        assertThat(savepointFile2.exists(), equalTo(true))
    }

    @Test
    fun `deleteAll removes all savepoints and all savepoint files`() {
        val savepointsRepository = buildSubject(cacheDirPath, instancesDirPath)

        val savepointFile1 = getSavepointFile("foo")
        savepointFile1.createNewFile()
        val savepoint1 = Savepoint(1, null, savepointFile1.absolutePath, "")

        val savepointFile2 = getSavepointFile("bar")
        savepointFile2.createNewFile()
        val savepoint2 = Savepoint(2, 1, savepointFile2.absolutePath, "")

        savepointsRepository.save(savepoint1)
        savepointsRepository.save(savepoint2)

        savepointsRepository.deleteAll()

        assertThat(savepointsRepository.getAll().isEmpty(), equalTo(true))
        assertThat(savepointFile1.exists(), equalTo(false))
        assertThat(savepointFile2.exists(), equalTo(false))
    }
}
