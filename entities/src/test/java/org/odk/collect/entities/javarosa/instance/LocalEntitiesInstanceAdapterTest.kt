package org.odk.collect.entities.javarosa.instance

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository

class LocalEntitiesInstanceAdapterTest {

    @Test
    fun `#queryEq supports label`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1"))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2"))

        val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)
        val results = instanceAdapter.queryEq("things", EntityItemElement.LABEL, "Thing 2")
        assertThat(results.size, equalTo(1))
        assertThat(results.first().getFirstChild("name")!!.value!!.value, equalTo("thing2"))
    }

    @Test
    fun `#queryEq supports __version`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1", version = 1))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2", version = 2))

        val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)
        val results = instanceAdapter.queryEq("things", EntityItemElement.VERSION, "2")
        assertThat(results.size, equalTo(1))
        assertThat(results.first().getFirstChild("name")!!.value!!.value, equalTo("thing2"))
    }

    @Test
    fun `#queryEq supports __trunkVersion`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1", trunkVersion = 1))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2", trunkVersion = 2))

        val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)
        val results = instanceAdapter.queryEq("things", EntityItemElement.TRUNK_VERSION, "2")
        assertThat(results.size, equalTo(1))
        assertThat(results.first().getFirstChild("name")!!.value!!.value, equalTo("thing2"))
    }

    @Test
    fun `#queryEq supports __branchId`() {
        val entitiesRepository = InMemEntitiesRepository()
        entitiesRepository.save("things", Entity.New("thing1", "Thing 1", branchId = "branch1"))
        entitiesRepository.save("things", Entity.New("thing2", "Thing 2", branchId = "branch2"))

        val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)
        val results = instanceAdapter.queryEq("things", EntityItemElement.BRANCH_ID, "branch2")
        assertThat(results.size, equalTo(1))
        assertThat(results.first().getFirstChild("name")!!.value!!.value, equalTo("thing2"))
    }
}
