package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity

abstract class EntitiesRepositoryTest {

    abstract fun buildSubject(): EntitiesRepository

    @Test
    fun `getEntities returns entities for dataset`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", emptyList())
        val whisky = Entity("whiskys", "2", "Lagavulin 16", emptyList())
        repository.save(wine)
        repository.save(whisky)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], equalTo(wine))

        val whiskys = repository.getEntities("whiskys")
        assertThat(whiskys.size, equalTo(1))
        assertThat(whiskys[0], equalTo(whisky))
    }

    @Test
    fun `save updates existing entity with matching id`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", emptyList())
        repository.save(wine)

        val updatedWine = Entity("wines", wine.id, "Léoville Barton 2009", emptyList())
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], equalTo(updatedWine))
    }

    @Test
    fun `save does not update existing entity with matching id but not dataset`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", emptyList())
        val whisky = Entity("whiskys", wine.id, "Lagavulin 16", emptyList())
        repository.save(wine)
        repository.save(whisky)

        val updatedWine = Entity("wines", wine.id, "Léoville Barton 2009", emptyList())
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], equalTo(updatedWine))

        val whiskys = repository.getEntities("whiskys")
        assertThat(whiskys.size, equalTo(1))
        assertThat(whiskys[0], equalTo(whisky))
    }

    @Test
    fun `save only changes properties in new entity for existing entity`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", listOf("window" to "2019-2038"))
        repository.save(wine)

        val updatedWine = Entity("wines", wine.id, "Léoville Barton 2008", listOf("score" to "92"))
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2038", "score" to "92"))
    }

    @Test
    fun `save updates existing properties in new entity for existing entity`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", listOf("window" to "2019-2038"))
        repository.save(wine)

        val updatedWine =
            Entity("wines", wine.id, "Léoville Barton 2008", listOf("window" to "2019-2042"))
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2042"))
    }
}
