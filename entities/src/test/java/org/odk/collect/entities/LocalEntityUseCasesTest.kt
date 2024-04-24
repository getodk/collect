package org.odk.collect.entities

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Test
import org.odk.collect.shared.TempFiles
import java.io.File

class LocalEntityUseCasesTest {

    @Test
    fun `updateLocalEntities overrides local version if the list version is newer`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save(Entity("songs", "noah", "Noa", 1))

        val csv = createEntityList(Entity("songs", "noah", "Noah", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override local version if the list version is older`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))

        val csv = createEntityList(Entity("songs", "noah", "Noa", 1))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities does not override local version if the list version is the same`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save(Entity("songs", "noah", "Noah", 2))

        val csv = createEntityList(Entity("songs", "noah", "Noa", 2))

        LocalEntityUseCases.updateLocalEntities("songs", csv, entitiesRepository)
        val songs = entitiesRepository.getEntities("songs")
        assertThat(songs, containsInAnyOrder(Entity("songs", "noah", "Noah", 2)))
    }

    @Test
    fun `updateLocalEntities adds properties not in local version from older list version`() {
        val entitiesRepository = InMemEntitiesRepository()
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

    private fun createEntityList(entity: Entity): File {
        val csv = TempFiles.createTempFile()
        csv.writer().use { it ->
            val csvPrinter = CSVPrinter(it, CSVFormat.DEFAULT)

            val header = listOf(
                EntityItemElement.ID,
                EntityItemElement.LABEL,
                EntityItemElement.VERSION
            ) + entity.properties.map { it.first }
            csvPrinter.printRecord(header)

            val row = listOf(
                entity.id,
                entity.label,
                entity.version
            ) + entity.properties.map { it.second }
            csvPrinter.printRecord(row)
        }

        return csv
    }
}
