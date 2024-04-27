package org.odk.collect.entities

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class LocalEntitiesFileInstanceParserTest {

    private val entitiesRepository = InMemEntitiesRepository()

    @Test
    fun `includes properties in offline entity elements`() {
        val entity =
            Entity(
                "people",
                "1",
                "Shiv Roy",
                properties = listOf(Pair("age", "35"), Pair("born", "England"))
            )
        entitiesRepository.save(entity)

        val parser = LocalEntitiesFileInstanceParser { entitiesRepository }
        val instance = parser.parse("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(5))
        assertThat(item.getFirstChild("age")?.value?.value, equalTo("35"))
        assertThat(item.getFirstChild("born")?.value?.value, equalTo("England"))
    }

    @Test
    fun `includes version in offline entity elements`() {
        val entity =
            Entity(
                "people",
                "1",
                "Shiv Roy",
                version = 1
            )
        entitiesRepository.save(entity)

        val parser = LocalEntitiesFileInstanceParser { entitiesRepository }
        val instance = parser.parse("people", "people.csv")
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(3))
        assertThat(item.getFirstChild("__version")?.value?.value, equalTo("1"))
    }
}
