package org.odk.collect.entities.javarosa.spec

import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathFuncExpr
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_CREATE
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_DATASET
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_ID
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_UPDATE
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_ENTITY
import org.odk.collect.entities.javarosa.spec.FormEntityElement.ELEMENT_LABEL

object EntityFormParser {
    fun parseDataset(entity: TreeElement): String? {
        return entity.getAttributeValue(null, ATTRIBUTE_DATASET)
    }

    @JvmStatic
    fun parseLabel(entity: TreeElement): String? {
        val labelElement = entity.getFirstChild(ELEMENT_LABEL)
        return labelElement?.value?.uncast()?.string
    }

    fun parseId(entity: TreeElement): String? {
        return entity.getAttributeValue("", ATTRIBUTE_ID)
    }

    fun getEntityElements(treeElement: TreeElement): List<TreeElement> {
        val entityElements = mutableListOf<TreeElement>()

        val numOfChildren = treeElement.numChildren
        for (i in 0..<numOfChildren) {
            val childTreeElement = treeElement.getChildAt(i)
            if ("meta" == childTreeElement.name) {
                val entity = childTreeElement.getFirstChild(ELEMENT_ENTITY)
                if (entity != null) {
                    entityElements.add(entity)
                }
            } else if (childTreeElement.hasChildren() && childTreeElement.mult != TreeReference.INDEX_TEMPLATE) {
                entityElements.addAll(getEntityElements(childTreeElement))
            }
        }

        return entityElements
    }

    fun hasEntityElement(treeElement: TreeElement): Boolean {
        return getEntityElements(treeElement).isNotEmpty()
    }

    @JvmStatic
    fun parseAction(entity: TreeElement): EntityAction? {
        val create = entity.getAttributeValue(null, ATTRIBUTE_CREATE)
        val update = entity.getAttributeValue(null, ATTRIBUTE_UPDATE)

        var shouldCreate = false
        if (create != null) {
            if (XPathFuncExpr.boolStr(create)) {
                shouldCreate = true
            }
        }

        var shouldUpdate = false
        if (update != null) {
            if (XPathFuncExpr.boolStr(update)) {
                shouldUpdate = true
            }
        }

        return if (shouldCreate && shouldUpdate) {
            null
        } else if (shouldCreate) {
            EntityAction.CREATE
        } else if (shouldUpdate) {
            EntityAction.UPDATE
        } else {
            null
        }
    }
}
