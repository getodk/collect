package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.entities.support.EntitySameAsMatcher.Companion.sameEntityAs
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

abstract class EntitiesRepositoryTest {

    abstract fun buildSubject(): EntitiesRepository

    @Test
    fun `#getLists returns lists for saved entities`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008")
        val whisky = Entity.New("whiskys", "2", "Lagavulin 16")
        repository.save(wine)
        repository.save(whisky)

        assertThat(repository.getLists(), containsInAnyOrder("wines", "whiskys"))
    }

    @Test
    fun `#getEntities returns entities for list`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008")
        val whisky = Entity.New("whiskys", "2", "Lagavulin 16")
        repository.save(wine)
        repository.save(whisky)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], sameEntityAs(wine))

        val whiskys = repository.getEntities("whiskys")
        assertThat(whiskys.size, equalTo(1))
        assertThat(whiskys[0], sameEntityAs(whisky))
    }

    @Test
    fun `#save updates existing entity with matching id`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008", version = 1)
        repository.save(wine)

        val updatedWine = Entity.New("wines", wine.id, "Léoville Barton 2009", version = 2)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save creates entity with matching id in different list`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008", version = 1)
        repository.save(wine)

        val updatedWine = Entity.New("whisky", wine.id, "Edradour 10", version = 2)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(sameEntityAs(wine)))
        val whiskys = repository.getEntities("whisky")
        assertThat(whiskys, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save updates existing entity with matching id and version`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008", version = 1)
        repository.save(wine)

        val updatedWine = wine.copy(label = "Léoville Barton 2009")
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save updates state on existing entity when it is offline`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008", state = Entity.State.OFFLINE)
        repository.save(wine)

        val updatedWine = wine.copy(state = Entity.State.ONLINE)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save does not update state on existing entity when it is online`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008", state = Entity.State.ONLINE)
        repository.save(wine)

        val updatedWine = wine.copy(state = Entity.State.OFFLINE)
        repository.save(updatedWine)

        val wines = repository.getEntities("wines")
        assertThat(wines, contains(sameEntityAs(wine)))
    }

    @Test
    fun `#save adds new properties`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity.New(
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

        val wine = Entity.New(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity.New(
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

        val wine = Entity.New(
            "wines",
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save(wine)

        val updatedWine = Entity.New(
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
    fun `#save does not clear empty entity lists`() {
        val repository = buildSubject()

        repository.addList("wines")
        repository.addList("blah")
        assertThat(repository.getLists(), containsInAnyOrder("wines", "blah"))

        repository.save(Entity.New("wines", "blah", "Blah"))
        assertThat(repository.getLists(), containsInAnyOrder("wines", "blah"))
    }

    @Test
    fun `#clear deletes all entities`() {
        val repository = buildSubject()

        val wine = Entity.New("wines", "1", "Léoville Barton 2008")
        val whisky = Entity.New("whiskys", "2", "Lagavulin 16")
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

        val wine = Entity.New("wines", "1", "Léoville Barton 2008")
        val whisky = Entity.New("whiskys", "2", "Lagavulin 16")
        repository.save(wine, whisky)

        assertThat(repository.getLists(), containsInAnyOrder("wines", "whiskys"))
    }

    @Test
    fun `#save assigns an index to each entity in insert order when saving multiple entities`() {
        val first = Entity.New("wines", "1", "Léoville Barton 2008")
        val second = Entity.New("wines", "2", "Pontet Canet 2014")

        val repository = buildSubject()
        repository.save(first, second)

        val entities = repository.getEntities("wines")
        assertThat(entities[0].index, equalTo(0))
        assertThat(entities[1].index, equalTo(1))
    }

    @Test
    fun `#save assigns an index to each entity in insert order when saving single entities`() {
        val first = Entity.New("wines", "1", "Léoville Barton 2008")
        val second = Entity.New("wines", "2", "Pontet Canet 2014")

        val repository = buildSubject()
        repository.save(first)
        repository.save(second)

        val entities = repository.getEntities("wines")
        assertThat(entities[0].index, equalTo(0))
        assertThat(entities[1].index, equalTo(1))
    }

    @Test
    fun `#save does not change index when updating an existing entity`() {
        val repository = buildSubject()

        val first = Entity.New("wines", "1", "Léoville Barton 2008")
        val second = Entity.New("wines", "2", "Pontet Canet 2014")
        repository.save(first, second)
        assertThat(repository.getEntities("wines")[0].index, equalTo(0))

        val updatedWine = first.copy(label = "Léoville Barton 2009")
        repository.save(updatedWine)

        assertThat(repository.getEntities("wines")[0].index, equalTo(0))
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

        val leoville = Entity.New("wines", "1", "Léoville Barton 2008")
        val canet = Entity.New("wines", "2", "Pontet-Canet 2014")
        repository.save(leoville, canet)

        repository.delete("1")

        assertThat(repository.getEntities("wines"), containsInAnyOrder(sameEntityAs(canet)))
    }

    @Test
    fun `#delete updates index values so that they are always in sequence and start at 0`() {
        val repository = buildSubject()

        val leoville = Entity.New("wines", "1", "Léoville Barton 2008")
        val canet = Entity.New("wines", "2", "Pontet-Canet 2014")
        val gloria = Entity.New("wines", "3", "Chateau Gloria 2016")
        repository.save(leoville, canet, gloria)

        repository.delete("1")

        var wines = repository.getEntities("wines")
        assertThat(wines[0].index, equalTo(0))
        assertThat(wines[1].index, equalTo(1))

        repository.save(leoville)
        wines = repository.getEntities("wines")
        assertThat(wines[0].index, equalTo(0))
        assertThat(wines[1].index, equalTo(1))
        assertThat(wines[2].index, equalTo(2))
    }

    @Test
    fun `#getAllById returns entities with matching id`() {
        val repository = buildSubject()

        val leoville = Entity.New("wines", "1", "Léoville Barton 2008")
        val canet = Entity.New("wines", "2", "Pontet-Canet 2014")
        repository.save(leoville, canet)

        val wines = repository.getEntities("wines")

        val queriedLeoville = repository.getById("wines", "1")
        assertThat(queriedLeoville, equalTo(wines.first { it.id == "1" }))

        val queriedCanet = repository.getById("wines", "2")
        assertThat(queriedCanet, equalTo(wines.first { it.id == "2" }))
    }

    @Test
    fun `#getAllById returns null when there are no matches`() {
        val repository = buildSubject()

        val leoville = Entity.New("wines", "1", "Léoville Barton 2008")
        val canet = Entity.New("wines", "2", "Pontet-Canet 2014")
        repository.save(leoville, canet)

        assertThat(repository.getById("wines", "3"), equalTo(null))
    }

    @Test
    fun `#getAllById returns null when there is a match in a different list`() {
        val repository = buildSubject()

        val leoville = Entity.New("wines", "1", "Léoville Barton 2008")
        val ardbeg = Entity.New("whisky", "2", "Ardbeg 10")
        repository.save(leoville, ardbeg)

        assertThat(repository.getById("whisky", "1"), equalTo(null))
    }
}
