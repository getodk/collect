package org.odk.collect.entities

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.blankOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.odk.collect.entities.javarosa.finalization.EntitiesExtra
import org.odk.collect.entities.javarosa.finalization.FormEntity
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.shared.TempFiles
import java.io.File

class LocalEntityUseCasesTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `updateLocalEntitiesFromForm creates a new branchId for new entities`() {
        entitiesRepository.addList("things")

        val formEntity =
            FormEntity(EntityAction.CREATE, "things", "id", "label", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))
        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)

        val entities = entitiesRepository.getEntities("things")
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].branchId, not(blankOrNullString()))
    }

    @Test
    fun `updateLocalEntitiesFromForm increments version on update`() {
        entitiesRepository.save(
            Entity.New(
                "things",
                "id",
                "label",
                version = 1
            )
        )

        val formEntity =
            FormEntity(EntityAction.UPDATE, "things", "id", "label", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        val entities = entitiesRepository.getEntities("things")
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].version, equalTo(2))
    }

    @Test
    fun `updateLocalEntitiesFromForm updates properties on update`() {
        entitiesRepository.save(
            Entity.New(
                "things",
                "id",
                "label",
                version = 1,
                properties = listOf("prop" to "value")
            )
        )

        val formEntity =
            FormEntity(EntityAction.UPDATE, "things", "id", "label", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        val entities = entitiesRepository.getEntities("things")
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].properties.size, equalTo(1))
        assertThat(entities[0].properties[0], equalTo("prop" to "value"))
    }

    @Test
    fun `updateLocalEntitiesFromForm does not override trunk version or branchId on update`() {
        entitiesRepository.save(
            Entity.New(
                "things",
                "id",
                "label",
                version = 1,
                trunkVersion = 1,
                branchId = "branch-1"
            )
        )

        val formEntity =
            FormEntity(EntityAction.UPDATE, "things", "id", "label", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        val entities = entitiesRepository.getEntities("things")
        assertThat(entities.size, equalTo(1))
        assertThat(entities[0].trunkVersion, equalTo(1))
        assertThat(entities[0].branchId, equalTo("branch-1"))
    }

    @Test
    fun `updateLocalEntitiesFromForm does not save updated entity that doesn't already exist`() {
        val formEntity =
            FormEntity(EntityAction.UPDATE, "things", "1", "1", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))
        entitiesRepository.addList("things")

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        assertThat(entitiesRepository.getEntities("things").size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromForm does not save entity that doesn't have an ID`() {
        val formEntity =
            FormEntity(EntityAction.CREATE, "things", null, "1", emptyList())
        val formEntities = EntitiesExtra(listOf(formEntity))
        entitiesRepository.addList("things")

        LocalEntityUseCases.updateLocalEntitiesFromForm(formEntities, entitiesRepository)
        assertThat(entitiesRepository.getEntities("things").size, equalTo(0))
    }

    @Test
    fun `updateLocalEntitiesFromServer overrides offline version if the online version is newer`() {
        val offline = Entity.New("songs", "noah", "Noa", 1)
        entitiesRepository.save(offline)
        val csv = createEntityList(Entity.New("songs", "noah", "Noah", 2))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].label, equalTo("Noah"))
        assertThat(songs[0].version, equalTo(2))
        assertThat(songs[0].state, equalTo(Entity.State.ONLINE))
        assertThat(songs[0].trunkVersion, equalTo(2))
        assertThat(songs[0].branchId, not(equalTo(offline.branchId)))
    }

    @Test
    fun `updateLocalEntitiesFromServer updates trunkVersion and state if the online version is older`() {
        val offline = Entity.New("songs", "noah", "Noah", 2)
        entitiesRepository.save(offline)
        val csv = createEntityList(Entity.New("songs", "noah", "Noa", 1))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].label, equalTo("Noah"))
        assertThat(songs[0].version, equalTo(2))
        assertThat(songs[0].state, equalTo(Entity.State.ONLINE))
        assertThat(songs[0].trunkVersion, equalTo(1))
        assertThat(songs[0].branchId, equalTo(offline.branchId))
    }

    @Test
    fun `updateLocalEntitiesFromServer updates trunkVersion, branchId and state if the online version is the same`() {
        val offline = Entity.New("songs", "noah", "Noah", 2)
        entitiesRepository.save(offline)
        val csv = createEntityList(Entity.New("songs", "noah", "Noa", 2))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].label, equalTo("Noah"))
        assertThat(songs[0].version, equalTo(2))
        assertThat(songs[0].state, equalTo(Entity.State.ONLINE))
        assertThat(songs[0].trunkVersion, equalTo(2))
        assertThat(songs[0].branchId, not(equalTo(offline.branchId)))
    }

    @Test
    fun `updateLocalEntitiesFromServer updates trunkVersion, branchId and state if the online version catches up to an offline branch`() {
        val offline = Entity.New("songs", "noah", "Noah", 2)
        entitiesRepository.save(offline)

        val csv1 = createEntityList(Entity.New("songs", "noah", "Noa", 2))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv1, entitiesRepository)

        val onlineBranched = Entity.New("songs", "noah", "Noah", 3)
        entitiesRepository.save(onlineBranched)
        val csv2 = createEntityList(Entity.New("songs", "noah", "Noa", 3))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv2, entitiesRepository)

        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].label, equalTo("Noah"))
        assertThat(songs[0].version, equalTo(3))
        assertThat(songs[0].state, equalTo(Entity.State.ONLINE))
        assertThat(songs[0].trunkVersion, equalTo(3))
        assertThat(songs[0].branchId, not(equalTo(onlineBranched.branchId)))
    }

    @Test
    fun `updateLocalEntitiesFromServer ignores properties not in offline version from older online version`() {
        entitiesRepository.save(Entity.New("songs", "noah", "Noah", 3))
        val csv =
            createEntityList(Entity.New("songs", "noah", "Noah", 2, listOf(Pair("length", "6:38"))))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].properties, equalTo(emptyList()))
    }

    @Test
    fun `updateLocalEntitiesFromServer overrides properties in offline version from newer list version`() {
        entitiesRepository.save(
            Entity.New("songs", "noah", "Noah", 1, listOf(Pair("length", "6:38")))
        )
        val csv =
            createEntityList(Entity.New("songs", "noah", "Noah", 2, listOf(Pair("length", "4:58"))))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].version, equalTo(2))
        assertThat(songs[0].properties, equalTo(listOf(Pair("length", "4:58"))))
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
        val csv = createEntityList(Entity.New("songs", "cathedrals", label = ""))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].label, equalTo(""))
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
            Entity.New("songs", "noah", "Noah"),
            Entity.New("songs", "seven-trumpets", "Seven Trumpets")
        )

        val entitiesRepository = MeasurableEntitiesRepository(entitiesRepository)
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.accesses, equalTo(2))
    }

    @Test
    fun `updateLocalEntitiesFromServer does not remove offline entities that are not in online entities`() {
        entitiesRepository.save(Entity.New("songs", "noah", "Noah"))
        val csv = createEntityList(Entity.New("songs", "cathedrals", "Cathedrals"))

        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(2))
    }

    @Test
    fun `updateLocalEntitiesFromServer removes offline entity that was in online list, but isn't any longer`() {
        entitiesRepository.save(Entity.New("songs", "cathedrals", "Cathedrals"))

        val firstCsv = createEntityList(Entity.New("songs", "cathedrals", "Cathedrals"))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", firstCsv, entitiesRepository)

        val secondCsv = createEntityList(Entity.New("songs", "noah", "Noah"))
        LocalEntityUseCases.updateLocalEntitiesFromServer("songs", secondCsv, entitiesRepository)

        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs.size, equalTo(1))
        assertThat(songs[0].id, equalTo("noah"))
    }

    @Test
    fun `updateLocalEntitiesFromServer removes offline entity that was updated in online list, but isn't any longer`() {
        entitiesRepository.save(Entity.New("songs", "cathedrals", "Cathedrals", version = 1))

        val firstCsv =
            createEntityList(Entity.New("songs", "cathedrals", "Cathedrals (A Song)", version = 2))
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

    override fun getEntities(list: String): List<Entity.Saved> {
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

    override fun getById(list: String, id: String): Entity.Saved? {
        accesses += 1
        return wrapped.getById(list, id)
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        accesses += 1
        return wrapped.getAllByProperty(list, property, value)
    }
}
