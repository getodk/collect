package org.odk.collect.entities

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.instance.TreeElement
import org.junit.Test

class OfflineEntitiesExternalDataInstanceProcessorTest {

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

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root")
        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val item = instance.getChildAt(0)!!
        assertThat(item.numChildren, equalTo(4))
        assertThat(item.getFirstChild("age")?.value?.value, equalTo("35"))
        assertThat(item.getFirstChild("born")?.value?.value, equalTo("England"))
    }
}
