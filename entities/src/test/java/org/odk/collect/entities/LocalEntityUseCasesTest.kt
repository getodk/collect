package org.odk.collect.entities

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.TempFiles
import java.io.File

class LocalEntityUseCasesTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `updateLocalEntities overrides offline version if the online version is newer`() {
        entitiesRepository.save(Entity("songs", "noah", "Noa", 1))
        val csv = createEntityList(Entity("songs", "noah", "Noah", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override offline version if the online version is older`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 1))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override offline version if the online version is the same`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities ignores properties not in offline version from older online version`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 3))
        val csv =
            createEntityList(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "6:38"))))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "noah", "Noah", 3))
        )
    }

    @Test
    fun `updateLocalEntities overrides properties in offline version from newer list version`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 1, listOf(Pair("length", "6:38"))))
        val csv =
            createEntityList(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "4:58"))))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "4:58"))))
        )
    }

    @Test
    fun `updateLocalEntities does nothing if version does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("name", "label"),
                listOf("grisaille", "Grisaille")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities does nothing if name does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("label", "__version"),
                listOf("Grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities does nothing if label does not exist in online entities`() {
        val csv =
            createCsv(
                listOf("name", "__version"),
                listOf("grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities adds online entity when its label is blank`() {
        val csv = createEntityList(Entity("songs", "cathedrals", label = ""))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "cathedrals", label = ""))
        )
    }

    @Test
    fun `updateLocalEntities does nothing if passed a non-CSV file`() {
        val file = TempFiles.createTempFile(".xml")

        LocalEntityUseCases.updateLocalEntities("songs", file, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities accesses entities repo only twice when saving multiple entities`() {
        val csv = createEntityList(
            Entity("songs", "noah", "Noah"),
            Entity("songs", "seven-trumpets", "Seven Trumpets")
        )

        val entitiesRepository = MeasurableEntitiesRepository(entitiesRepository)
        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.accesses, equalTo(2))
    }

    @Test
    fun `updateLocalEntities does not remove offline entities that are not in online entities`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah"))
        val csv = createEntityList(Entity("songs", "cathedrals", "Cathedrals"))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(
                Entity("songs", "cathedrals", "Cathedrals"),
                Entity("songs", "noah", "Noah")
            )
        )
    }

    @Test
    fun `updateLocalEntities accesses entities repo only once when not saving new entities`() {
        val entities = arrayOf(
            Entity("songs", "noah", "Noah"),
            Entity("songs", "seven-trumpets", "Seven Trumpets")
        )

        entitiesRepository.save(*entities)
        val csv = createEntityList(*entities)

        val entitiesRepository = MeasurableEntitiesRepository(entitiesRepository)
        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.accesses, equalTo(1))
    }

    private fun createEntityList(vararg entities: Entity): File {
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

    override fun getDatasets(): Set<String> {
        accesses += 1
        return wrapped.getDatasets()
    }

    override fun getEntities(dataset: String): List<Entity> {
        accesses += 1
        return wrapped.getEntities(dataset)
    }

    override fun clear() {
        accesses += 1
        wrapped.clear()
    }

    override fun addDataset(dataset: String) {
        accesses += 1
        wrapped.addDataset(dataset)
    }
}
