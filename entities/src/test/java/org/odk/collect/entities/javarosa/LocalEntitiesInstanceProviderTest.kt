package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.entities.browser.EntityItemElement
import org.odk.collect.entities.javarosa.intance.LocalEntitiesInstanceProvider
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository

class LocalEntitiesInstanceProviderTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `includes properties in local entity elements`() {
        val entity =
            Entity.New(
                "people",
                "1",
                "Shiv Roy",
                properties = listOf(Pair("age", "35"), Pair("born", "England"))
            )
        entitiesRepository.save(entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(5))
        assertThat(item.getFirstChild("age")?.value?.value, equalTo("35"))
        assertThat(item.getFirstChild("born")?.value?.value, equalTo("England"))
    }

    @Test
    fun `includes version in local entity elements`() {
        val entity =
            Entity.New(
                "people",
                "1",
                "Shiv Roy",
                version = 1
            )
        entitiesRepository.save(entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(3))
        assertThat(item.getFirstChild(EntityItemElement.VERSION)?.value?.value, equalTo("1"))
    }

    @Test
    fun `partial parse returns elements without values`() {
        val entity =
            Entity.New(
                "people",
                "1",
                "Shiv Roy",
                properties = listOf(Pair("age", "35")),
                version = 1
            )
        entitiesRepository.save(entity)

        val parser = LocalEntitiesInstanceProvider { entitiesRepository }
        val instance = parser.get("people", "people.csv", true)
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.isPartial, equalTo(true))
        assertThat(item.numChildren, equalTo(4))
        0.until(item.numChildren).forEach {
            assertThat(item.getChildAt(it).value?.value, equalTo(null))
        }
    }

    @Test
    fun `uses entity index for multiplicity`() {
        val entities = arrayOf(
            Entity.New(
                "people",
                "1",
                "Shiv Roy"
            ),
            Entity.New(
                "people",
                "2",
                "Kendall Roy"
            )
        )

        val repository = InMemEntitiesRepository()
        repository.save(*entities)

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
