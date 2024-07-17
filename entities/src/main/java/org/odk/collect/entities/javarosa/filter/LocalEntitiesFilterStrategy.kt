package org.odk.collect.entities.javarosa.filter

import org.javarosa.core.model.CompareToNodeExpression
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.xpath.expr.XPathEqExpr
import org.javarosa.xpath.expr.XPathExpression
import org.odk.collect.entities.browser.EntityItemElement
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import java.util.function.Supplier

class LocalEntitiesFilterStrategy(private val entitiesRepository: EntitiesRepository) :
    FilterStrategy {

    private val lists = entitiesRepository.getLists()

    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): List<TreeReference> {
        if (!lists.contains(sourceInstance.instanceId)) {
            return next.get()
        }

        val candidate = CompareToNodeExpression.parse(predicate)
        return when (val original = candidate?.original) {
            is XPathEqExpr -> {
                if (original.isEqual) {
                    val filterChild = candidate.nodeSide.steps[0].name.name

                    when {
                        filterChild == "name" -> {
                            val value = candidate.evalContextSide(sourceInstance, evaluationContext)
                            val entity = entitiesRepository.getById(
                                sourceInstance.instanceId,
                                value as String
                            )

                            if (entity != null) {
                                val element = convertToElement(sourceInstance, entity)
                                sourceInstance.replacePartialElements(listOf(element))
                                listOf(element.ref)
                            } else {
                                emptyList()
                            }
                        }

                        !listOf("label", "__version").contains(filterChild) -> {
                            val value = candidate.evalContextSide(sourceInstance, evaluationContext)
                            val entities = entitiesRepository.getAllByProperty(
                                sourceInstance.instanceId,
                                filterChild,
                                value as String
                            )

                            val elements = entities.map { convertToElement(sourceInstance, it) }
                            sourceInstance.replacePartialElements(elements)
                            elements.map { it.ref }
                        }

                        else -> next.get()
                    }
                } else {
                    next.get()
                }
            }

            else -> {
                next.get()
            }
        }
    }

    private fun convertToElement(
        sourceInstance: DataInstance<*>,
        entity: Entity.Saved
    ): TreeElement {
        val name = TreeElement(EntityItemElement.ID)
        val label = TreeElement(EntityItemElement.LABEL)
        val version = TreeElement(EntityItemElement.VERSION)

        name.value = StringData(entity.id)
        label.value = StringData(entity.label)
        version.value = StringData(entity.version.toString())

        val item = TreeElement("item", entity.index)
        item.addChild(name)
        item.addChild(label)
        item.addChild(version)

        entity.properties.forEach { property ->
            val propertyElement = TreeElement(property.first)
            propertyElement.value = StringData(property.second)
            item.addChild(propertyElement)
        }

        item.parent = sourceInstance.root
        return item
    }
}
