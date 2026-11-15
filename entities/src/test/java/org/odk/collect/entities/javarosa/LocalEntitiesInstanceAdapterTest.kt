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
    fun `supportsInstance returns true for an existing entity list when no file is attached`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository) { null }
        assertThat(adapter.supportsInstance("people"), equalTo(true))
    }

    @Test
    fun `supportsInstance returns false when a CSV with the same name is attached`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, attached("jr://file-csv/people.csv"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when an XML file with the same name is attached`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, attached("jr://file/people.xml"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when a GeoJSON file with the same name is attached`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, attached("jr://file/people.geojson"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    private fun attached(attachedUri: String): FormMediaFileRepository {
        val file = TempFiles.createTempFile()
        return FormMediaFileRepository { uri -> if (uri == attachedUri) file else null }
    }
}
