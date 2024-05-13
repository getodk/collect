package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity
import org.odk.collect.shared.TempFiles
import java.io.File

class JsonFileEntitiesRepositoryTest : EntitiesRepositoryTest() {

    private val directory = TempFiles.createTempDir()

    override fun buildSubject(): EntitiesRepository {
        return JsonFileEntitiesRepository(directory)
    }

    @Test
    fun `two repositories with the same directory have the same data`() {
        val directory = File(TempFiles.getPathInTempDir())
        val one = JsonFileEntitiesRepository(directory)
        val two = JsonFileEntitiesRepository(directory)
        val three = JsonFileEntitiesRepository(File(TempFiles.getPathInTempDir()))

        val entity = Entity("stuff", "1", "A thing")
        one.save(entity)
        assertThat(two.getLists(), contains("stuff"))
        assertThat(two.getEntities("stuff"), contains(entity))
        assertThat(three.getLists().size, equalTo(0))
    }

    @Test
    fun `deletes existing backing file if it can't be parsed by current code`() {
        val repository = buildSubject()
        repository.getEntities("blah")

        val filesInDir = directory.listFiles()
        assertThat(filesInDir!!.size, equalTo(1))

        val backingFile = filesInDir[0]
        backingFile.writeText("blah")

        assertThat(repository.getEntities("blah").size, equalTo(0))
    }
}
