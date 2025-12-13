package org.odk.collect.entities.javarosa

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.javarosa.core.model.data.IntegerData
import org.javarosa.core.model.instance.TreeElement
import org.junit.Test
import org.odk.collect.entities.javarosa.spec.EntityAction
import org.odk.collect.entities.javarosa.spec.EntityFormParser.parseAction
import org.odk.collect.entities.javarosa.spec.EntityFormParser.parseLabel
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_CREATE
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_UPDATE
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_ENTITY
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_LABEL

class EntityFormParserTest {
    @Test
    fun `parse action finds create with true string`() {
        val entityElement = TreeElement(ELEMENT_ENTITY)
        entityElement.setAttribute(null, ATTRIBUTE_CREATE, "true")

        val action = parseAction(entityElement)
        assertThat<EntityAction?>(action, equalTo(EntityAction.CREATE))
    }

    @Test
    fun `parse action finds update with true string`() {
        val entityElement = TreeElement(ELEMENT_ENTITY)
        entityElement.setAttribute(null, ATTRIBUTE_UPDATE, "true")

        val dataset = parseAction(entityElement)
        assertThat(dataset, equalTo(EntityAction.UPDATE))
    }

    @Test
    fun `parse label when label is an int, converts to string`() {
        val labelElement = TreeElement(ELEMENT_LABEL)
        labelElement.setAnswer(IntegerData(0))
        val entityElement = TreeElement(ELEMENT_ENTITY)
        entityElement.addChild(labelElement)

        val label = parseLabel(entityElement)
        assertThat(label, Matchers.equalTo<String?>("0"))
    }
}
