package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity

abstract class EntitiesRepositoryTest {

    abstract fun buildSubject(): EntitiesRepository

    @Test
    fun `#getLists returns lists for saved entities`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008")
        val whisky = Entity("whiskys", "2", "Lagavulin 16")
        repository.save(wine)
        repository.save(whisky)

        assertThat(repository.getLists(), containsInAnyOrder("wines", "whiskys"))
    }

    @Test
    fun `#getEntities returns entities for list`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008")
        val whisky = Entity("whiskys", "2", "Lagavulin 16")
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
    fun `#save updates existing entity with matching id`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", version = 1)
        repository.save(wine)

        val updatedWine = Entity("wines", wine.id, "Léoville Barton 2009", version = 2)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(updatedWine))
    }

    @Test
    fun `#save updates existing entity with matching id and version`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", version = 1)
        repository.save(wine)

        val updatedWine = wine.copy(label = "Léoville Barton 2009")
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(updatedWine))
    }

    @Test
    fun `#save does not update existing entity with matching id but not list`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008")
        val whisky = Entity("whiskys", wine.id, "Lagavulin 16")
        repository.save(wine)
        repository.save(whisky)

        val updatedWine = Entity("wines", wine.id, "Léoville Barton 2009")
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(updatedWine))

        val whiskys = repository.getEntities("whiskys")
        assertThat(whiskys, contains(whisky))
    }

    @Test
    fun `#save updates offline on existing entity when it is false`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", offline = true)
        repository.save(wine)

        val updatedWine = wine.copy(offline = false)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(updatedWine))
    }

    @Test
    fun `#save does not update offline on existing entity when it is true`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008", offline = false)
        repository.save(wine)

        val updatedWine = wine.copy(offline = true)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(wine))
    }

    @Test
    fun `#save adds new properties`() {
        val repository = buildSubject()

        val wine = Entity(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity(
            "wines",
            wine.id,
            "Léoville Barton 2008",
            properties = listOf("score" to "92"),
            version = 2
        )
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2038", "score" to "92"))
    }

    @Test
    fun `#save updates existing properties`() {
        val repository = buildSubject()

        val wine = Entity(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity(
            "wines",
            wine.id,
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2042"),
            version = 2
        )
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2042"))
    }

    @Test
    fun `#save does not update existing label if new one is null`() {
        val repository = buildSubject()

        val wine = Entity(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity(
            "wines",
            wine.id,
            null,
            properties = listOf("window" to "2019-2042"),
            version = 2
        )
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].label, equalTo(wine.label))
        assertThat(wines[0].properties, equalTo(updatedWine.properties))
    }

    @Test
    fun `#clear deletes all entities`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008")
        val whisky = Entity("whiskys", "2", "Lagavulin 16")
        repository.save(wine)
        repository.save(whisky)

        repository.clear()
        assertThat(repository.getLists().size, equalTo(0))
        assertThat(repository.getEntities("wines").size, equalTo(0))
        assertThat(repository.getEntities("whiskys").size, equalTo(0))
    }

    @Test
    fun `#save can save multiple entities`() {
        val repository = buildSubject()

        val wine = Entity("wines", "1", "Léoville Barton 2008")
        val whisky = Entity("whiskys", "2", "Lagavulin 16")
        repository.save(wine, whisky)

        assertThat(repository.getLists(), containsInAnyOrder("wines", "whiskys"))
    }

    @Test
    fun `#addList adds a list with no entities`() {
        val repository = buildSubject()

        repository.addList("wine")
        assertThat(repository.getLists(), containsInAnyOrder("wine"))
        assertThat(repository.getEntities("wine").size, equalTo(0))
    }

    @Test
    fun `#delete removes an entity`() {
        val repository = buildSubject()

        val leoville = Entity("wines", "1", "Léoville Barton 2008")
        val canet = Entity("wines", "2", "Pontet-Canet 2014")
        repository.save(leoville, canet)

        repository.delete("1")

        assertThat(repository.getEntities("wines"), containsInAnyOrder(canet))
    }
}
