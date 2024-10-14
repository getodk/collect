package org.odk.collect.entities.javarosa.instance

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository

class LocalEntitiesInstanceAdapterTest {

    @Test
    fun `#queryEq supports label`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1"))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2"))

        val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)
        val results = instanceAdapter.queryEq("things", "label", "Thing 2")
        assertThat(results!!.first().getFirstChild("name")!!.value!!.value, equalTo("thing2"))
    }
}
