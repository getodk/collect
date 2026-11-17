package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.forms.FormMediaFileRepository

class LocalEntitiesInstanceAdapterTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `supportsInstance returns false when there is no entity list with the instance ID`() {
        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository) { false }
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns true for an existing entity list when there is no media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository) { false }
        assertThat(adapter.supportsInstance("people"), equalTo(true))
    }

    @Test
    fun `supportsInstance returns false when there is a CSV media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("people.csv"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when there is an XML media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("people.xml"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    @Test
    fun `supportsInstance returns false when there is a GeoJSON media file with the same name`() {
        entitiesRepository.save("people", Entity.New("1", "Shiv Roy"))

        val adapter = LocalEntitiesInstanceAdapter(entitiesRepository, mediaFileRepositoryWithFiles("people.geojson"))
        assertThat(adapter.supportsInstance("people"), equalTo(false))
    }

    private fun mediaFileRepositoryWithFiles(vararg names: String): FormMediaFileRepository {
        return FormMediaFileRepository { it in names }
    }
}
