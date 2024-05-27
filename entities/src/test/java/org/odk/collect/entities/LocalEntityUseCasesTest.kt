package org.odk.collect.entities

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.javarosa.entities.EntityAction
import org.javarosa.entities.internal.Entities
import org.junit.Test
import org.odk.collect.entities.Entity.State.ONLINE
import org.odk.collect.shared.TempFiles
import java.io.File

class LocalEntityUseCasesTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `updateLocalEntitiesFromForm does not save updated entity that doesn't already exist`() {
        val entity =
            org.javarosa.entities.Entity(EntityAction.UPDATE, "things", "1", "1", 1, emptyList())
        val formEntities = Entities(listOf(entity))
        entitiesRepository.addList("things")

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        assertThat(entitiesRepository.getEntities("things").size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromForm does not save entity that doesn't have an ID`() {
        val entity =
            org.javarosa.entities.Entity(EntityAction.CREATE, "things", null, "1", 1, emptyList())
        val formEntities = Entities(listOf(entity))
        entitiesRepository.addList("things")

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        assertThat(entitiesRepository.getEntities("things").size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer overrides offline version if the online version is newer`() {
        entitiesRepository.save(Entity("songs", "noah", "Noa", 1))
        val csv = createEntityList(Entity("songs", "noah", "Noah", 2))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2, state = ONLINE)))
    }

    @Test
    fun `updateLocalEntitiesFromServer does not override offline version if the online version is older`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 1))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2, state = ONLINE)))
    }

    @Test
    fun `updateLocalEntitiesFromServer does not override offline version if the online version is the same`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 2))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2, state = ONLINE)))
    }

    @Test
    fun `updateLocalEntitiesFromServer ignores properties not in offline version from older online version`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 3))
        val csv =
            createEntityList(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "6:38"))))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "noah", "Noah", 3, state = ONLINE))
        )
    }

    @Test
    fun `updateLocalEntitiesFromServer overrides properties in offline version from newer list version`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 1, listOf(Pair("length", "6:38"))))
        val csv =
            createEntityList(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "4:58"))))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(
                Entity(
                    "songs",
                    "noah",
                    "Noah",
                    2,
                    listOf(Pair("length", "4:58")),
                    state = ONLINE
                )
            )
        )
    }

    @Test
    fun `updateLocalEntitiesFromServer does nothing if version does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("name", "label"),
                listOf("grisaille", "Grisaille")
            )

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getLists().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer does nothing if name does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("label", "__version"),
                listOf("Grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getLists().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer does nothing if label does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("name", "__version"),
                listOf("grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getLists().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer adds online entity when its label is blank`() {
        val csv = createEntityList(Entity("songs", "cathedrals", label = ""))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "cathedrals", label = "", state = ONLINE))
        )
    }

    @Test
    fun `updateLocalEntitiesFromServer does nothing if passed a non-CSV file`() {
        val file = TempFiles.createTempFile(".xml")

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", file, entitiesRepository)
        assertThat(entitiesRepository.getLists().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer accesses entities repo only twice when saving multiple entities`() {
        val csv = createEntityList(
            Entity("songs", "noah", "Noah"),
            Entity("songs", "seven-trumpets", "Seven Trumpets")
        )

        val entitiesRepository = MeasurableEntitiesRepository(entitiesRepository)
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.accesses, equalTo(2))
    }

    @Test
    fun `updateLocalEntitiesFromServer does not remove offline entities that are not in online entities`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah"))
        val csv = createEntityList(Entity("songs", "cathedrals", "Cathedrals"))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(
                Entity("songs", "cathedrals", "Cathedrals", state = ONLINE),
                Entity("songs", "noah", "Noah")
            )
        )
    }

    @Test
    fun `updateLocalEntitiesFromServer removes offline entity that was in online list, but isn't any longer`() {
        entitiesRepository.save(Entity("songs", "cathedrals", "Cathedrals"))

        val firstCsv = createEntityList(Entity("songs", "cathedrals", "Cathedrals"))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", firstCsv, entitiesRepository)

        val secondCsv = createEntityList(Entity("songs", "noah", "Noah"))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", secondCsv, entitiesRepository)

        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", state = ONLINE)))
    }

    @Test
    fun `updateLocalEntitiesFromServer removes offline entity that was updated in online list, but isn't any longer`() {
        entitiesRepository.save(Entity("songs", "cathedrals", "Cathedrals", version = 1))

        val firstCsv =
            createEntityList(Entity("songs", "cathedrals", "Cathedrals (A Song)", version = 2))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", firstCsv, entitiesRepository)

        val secondCsv = createEntityList()
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", secondCsv, entitiesRepository)

        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.isEmpty(), equalTo(true))
    }

    private fun createEntityList(vararg entities: Entity): File {
        if (entities.isNotEmpty()) {
            val header = listOf(
                EntityItemElement.ID,
                EntityItemElement.LABEL,
                EntityItemElement.VERSION
            ) + entities[0].properties.map { it.first }

            val rows = entities.map { entity ->
                listOf(
                    entity.id,
                    entity.label,
                    entity.version.toString()
                ) + entity.properties.map { it.second }
            }.toTypedArray()

            return createCsv(header, *rows)
        } else {
            val header = listOf(
                EntityItemElement.ID,
                EntityItemElement.LABEL,
                EntityItemElement.VERSION
            )

            return createCsv(header)
        }
    }

    private fun createCsv(header: List<String>, vararg rows: List<String?>): File {
        val csv = TempFiles.createTempFile()
        csv.writer().use { it ->
            val csvPrinter = CSVPrinter(it, CSVFormat.DEFAULT)
            csvPrinter.printRecord(header)

            rows.forEach {
                csvPrinter.printRecord(it)
            }
        }

        return csv
    }
}

private class MeasurableEntitiesRepository(private val wrapped: EntitiesRepository) :
    EntitiesRepository {

    var accesses: Int = 0
        private set

    override fun save(vararg entities: Entity) {
        accesses += 1
        wrapped.save(*entities)
    }

    override fun getLists(): Set<String> {
        accesses += 1
        return wrapped.getLists()
    }

    override fun getEntities(list: String): List<Entity> {
        accesses += 1
        return wrapped.getEntities(list)
    }

    override fun clear() {
        accesses += 1
        wrapped.clear()
    }

    override fun addList(list: String) {
        accesses += 1
        wrapped.addList(list)
    }

    override fun delete(id: String) {
        accesses += 1
        wrapped.delete(id)
    }
}
