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
    fun `updateLocalEntities overrides local version if the list version is newer`() {
        entitiesRepository.save(Entity("songs", "noah", "Noa", 1))
        val csv = createEntityList(Entity("songs", "noah", "Noah", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override local version if the list version is older`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 1))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override local version if the list version is the same`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv = createEntityList(Entity("songs", "noah", "Noa", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities adds properties not in local version from older list version`() {
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))
        val csv =
            createEntityList(Entity("songs", "noah", "Noa", 1, listOf(Pair("length", "6:38"))))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "noah", "Noah", 2, listOf(Pair("length", "6:38"))))
        )
    }

    @Test
    fun `updateLocalEntities does nothing if version does not exist in list`() {
        val csv =
            createCsv(
                listOf("name", "label"),
                listOf("grisaille", "Grisaille")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities does nothing if name does not exist in list`() {
        val csv =
            createCsv(
                listOf("label", "__version"),
                listOf("Grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities does nothing if label does not exist in list`() {
        val csv =
            createCsv(
                listOf("name", "__version"),
                listOf("grisaille", "2")
            )

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    @Test
    fun `updateLocalEntities adds list entity when its label is blank`() {
        val csv = createEntityList(Entity("songs", "cathedrals", ""))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(
            songs,
            containsInAnyOrder(Entity("songs", "cathedrals", ""))
        )
    }

    @Test
    fun `updateLocalEntities does nothing if passed a non-CSV file`() {
        val file = TempFiles.createTempFile(".xml")

        LocalEntityUseCases.updateLocalEntities("songs", file, entitiesRepository)
        assertThat(entitiesRepository.getDatasets().size, equalTo(0))
    }

    private fun createEntityList(entity: Entity): File {
        val header = listOf(
            EntityItemElement.ID,
            EntityItemElement.LABEL,
            EntityItemElement.VERSION
        ) + entity.properties.map { it.first }

        val row: List<String?> = listOf(
            entity.id,
            entity.label,
            entity.version.toString()
        ) + entity.properties.map { it.second }

        return createCsv(header, row)
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
