package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.forms.FormMediaFileRepository
import org.odk.collect.shared.TempFiles

class LocalEntitiesInstanceAdapterTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `supportsInstance returns false when there is no entity list with the instance ID`() {
        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository) { null }
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns true for an existing entity list when there is no media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository) { null }
        assertThat(adapter.supportsInstance("people"), equalTo(true))
    }

    @Test
    fun `supportsInstance returns false when there is a CSV media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("jr://file-csv/people.csv"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when there is an XML media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("jr://file/people.xml"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when there is a GeoJSON media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("jr://file/people.geojson"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    private fun mediaFileRepositoryWithFiles(vararg uris: String): FormMediaFileRepository {
        val file = TempFiles.createTempFile()
        return FormMediaFileRepository { uri -> if (uri in uris) file else null }
    }
}
