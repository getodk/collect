package org.odk.collect.entities

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.StringData
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

    @Test
    fun `uses offline entity when there is a duplicate in online entities`() {
        val entity = Entity("people", "1", "Shiv Roy", version = 1, properties = listOf(Pair("age", "35")))
        entitiesRepository.save(entity)

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root").also { root ->
            root.addChild(
                TreeElement("item", 0).also { item ->
                    item.addChild(
                        TreeElement(EntityItemElement.ID).also { value ->
                            value.value = StringData(entity.id)
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.LABEL).also { label ->
                            label.value = StringData("Siobhan Roy")
                        }
                    )
                    item.addChild(
                        TreeElement("age").also { label ->
                            label.value = StringData("34")
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.VERSION).also { label ->
                            label.value = StringData("1")
                        }
                    )
                }
            )
        }

        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val firstItem = instance.getChildAt(0)
        assertThat(firstItem.getFirstChild(EntityItemElement.LABEL)?.value?.value, equalTo(entity.label))
        assertThat(
            firstItem.getFirstChild("age")?.value?.value,
            equalTo(entity.properties[0].second)
        )
    }

    @Test
    fun `merges properties from an offline and online duplicate`() {
        val entity = Entity("people", "1", "Shiv Roy", properties = listOf(Pair("age", "35")))
        entitiesRepository.save(entity)

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root").also { root ->
            root.addChild(
                TreeElement("item", 0).also { item ->
                    item.addChild(
                        TreeElement(EntityItemElement.ID).also { value ->
                            value.value = StringData(entity.id)
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.LABEL).also { label ->
                            label.value = StringData("Siobhan Roy")
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.VERSION).also { label ->
                            label.value = StringData("1")
                        }
                    )
                    item.addChild(
                        TreeElement("nickname").also { label ->
                            label.value = StringData("Shiv")
                        }
                    )
                }
            )
        }

        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val firstItem = instance.getChildAt(0)
        assertThat(firstItem.getFirstChild(EntityItemElement.LABEL)?.value?.value, equalTo(entity.label))
        assertThat(
            firstItem.getFirstChild("age")?.value?.value,
            equalTo(entity.properties[0].second)
        )
        assertThat(firstItem.getFirstChild("nickname")?.value?.value, equalTo("Shiv"))
    }

    @Test
    fun `uses online label if offline entity does not have one`() {
        val entity = Entity("people", "1", null, version = 2)
        entitiesRepository.save(entity)

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root").also { root ->
            root.addChild(
                TreeElement("item", 0).also { item ->
                    item.addChild(
                        TreeElement(EntityItemElement.ID).also { value ->
                            value.value = StringData(entity.id)
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.LABEL).also { label ->
                            label.value = StringData("Siobhan Roy")
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.VERSION).also { label ->
                            label.value = StringData("1")
                        }
                    )
                }
            )
        }

        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val firstItem = instance.getChildAt(0)
        assertThat(firstItem.getFirstChild(EntityItemElement.LABEL)?.value?.value, equalTo("Siobhan Roy"))
    }

    @Test
    fun `uses offline label even if blank`() {
        val entity = Entity("people", "1", "", version = 2)
        entitiesRepository.save(entity)

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root").also { root ->
            root.addChild(
                TreeElement("item", 0).also { item ->
                    item.addChild(
                        TreeElement(EntityItemElement.ID).also { value ->
                            value.value = StringData(entity.id)
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.LABEL).also { label ->
                            label.value = StringData("Siobhan Roy")
                        }
                    )
                    item.addChild(
                        TreeElement(EntityItemElement.VERSION).also { label ->
                            label.value = StringData("1")
                        }
                    )
                }
            )
        }

        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val firstItem = instance.getChildAt(0)
        assertThat(firstItem.getFirstChild(EntityItemElement.LABEL)?.value?.value, equalTo(""))
    }

    @Test
    fun `uses blank label if offline entity does not have one`() {
        val entity = Entity("people", "1", null)
        entitiesRepository.save(entity)

        val processor =
            OfflineEntitiesExternalDataInstanceProcessor(entitiesRepository)

        val instance = TreeElement("root")
        processor.processInstance("people", instance)
        assertThat(instance.numChildren, equalTo(1))

        val firstItem = instance.getChildAt(0)
        assertThat(firstItem.getFirstChild(EntityItemElement.LABEL)?.value?.value, equalTo(""))
    }
}
