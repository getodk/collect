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
    override fun buildSubject(): EntitiesRepository {
        return JsonFileEntitiesRepository(TempFiles.createTempDir())
    }

    @Test
    fun `two repositories with the same directory have the same data`() {
        val directory = File(TempFiles.getPathInTempDir())
        val one = JsonFileEntitiesRepository(directory)
        val two = JsonFileEntitiesRepository(directory)
        val three = JsonFileEntitiesRepository(File(TempFiles.getPathInTempDir()))

        val entity = Entity("stuff", "1", "A thing")
        one.save(entity)
        assertThat(two.getDatasets(), contains("stuff"))
        assertThat(two.getEntities("stuff"), contains(entity))
        assertThat(three.getDatasets().size, equalTo(0))

        val anotherEntity = Entity("otherStuff", "2", "Another thing")
        two.save(anotherEntity)
        assertThat(one.getDatasets(), contains("stuff", "otherStuff"))
        assertThat(two.getEntities("otherStuff"), contains(anotherEntity))
    }
}
