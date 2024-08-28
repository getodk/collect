package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository

class LocalEntitiesInstanceProviderTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `includes properties in local entity elements`() {
        val entity =
            Entity.New(
                "1",
                "Shiv Roy",
                properties = listOf(Pair("age", "35"), Pair("born", "England"))
            )
        entitiesRepository.save("people", entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(7))
        assertThat(item.getFirstChild("age")?.value?.value, equalTo("35"))
        assertThat(item.getFirstChild("born")?.value?.value, equalTo("England"))
    }

    @Test
    fun `includes version in local entity elements`() {
        val entity =
            Entity.New(
                "1",
                "Shiv Roy",
                version = 1
            )
        entitiesRepository.save("people", entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(5))
        assertThat(item.getFirstChild(EntityItemElement.VERSION)?.value?.value, equalTo("1"))
    }

    @Test
    fun `includes trunk version in local entity elements`() {
        val entity =
            Entity.New(
                "1",
                "Shiv Roy",
                trunkVersion = 1
            )
        entitiesRepository.save("people", entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(5))
        assertThat(item.getFirstChild(EntityItemElement.TRUNK_VERSION)?.value?.value, equalTo("1"))
    }

    @Test
    fun `includes branch id in local entity elements`() {
        val entity =
            Entity.New(
                "1",
                "Shiv Roy",
                branchId = "branch-1"
            )
        entitiesRepository.save("people", entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(5))
        assertThat(
            item.getFirstChild(EntityItemElement.BRANCH_ID)?.value?.value,
            equalTo("branch-1")
        )
    }

    @Test
    fun `includes blank trunk version when it is null`() {
        val entity =
            Entity.New(
                "1",
                "Shiv Roy",
                trunkVersion = null
            )
        entitiesRepository.save("people", entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.getFirstChild(EntityItemElement.TRUNK_VERSION)?.value, equalTo(null))
    }

    @Test
    fun `partial parse returns elements without values for first item and just item for others`() {
        val entity = arrayOf(
            Entity.New(
                "1",
                "Shiv Roy",
                properties = listOf(Pair("age", "35")),
                version = 1
            ),
            Entity.New(
                "2",
                "Kendall Roy",
                properties = listOf(Pair("age", "40")),
                version = 1
            )
        )
        entitiesRepository.save("people", *entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv", true)
        assertThat(instance.numChildren, equalTo(2))

        val item1 = instance.getChildAt(0)!!
        assertThat(item1.isPartial, equalTo(true))
        assertThat(item1.numChildren, equalTo(6))
        0.until(item1.numChildren).forEach {
            assertThat(item1.getChildAt(it).value?.value, equalTo(null))
        }

        val item2 = instance.getChildAt(1)!!
        assertThat(item2.isPartial, equalTo(true))
        assertThat(item2.numChildren, equalTo(0))
    }

    @Test
    fun `uses entity index for multiplicity`() {
        val entities = arrayOf(
            Entity.New(
                "1",
                "Shiv Roy"
            ),
            Entity.New(
                "2",
                "Kendall Roy"
            )
        )

        val repository = InMemEntitiesRepository()
        repository.save("people", *entities)

        val parser = LocalEntitiesInstanceProvider { repository }
        val instance = parser.get("people", "people.csv", false)

        val first = instance.getChildAt(0)!!
        assertThat(first.getFirstChild("name")!!.value!!.value, equalTo("1"))
        assertThat(first.multiplicity, equalTo(0))

        val second = instance.getChildAt(1)!!
        assertThat(second.getFirstChild("name")!!.value!!.value, equalTo("2"))
        assertThat(second.multiplicity, equalTo(1))
    }
}
