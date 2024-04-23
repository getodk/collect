package org.odk.collect.entities

import org.javarosa.core.model.instance.CsvExternalInstance
import java.io.File

object LocalEntityUseCases {

    fun updateLocalEntities(dataset: String, entityList: File, entitiesRepository: EntitiesRepository) {
        val root = CsvExternalInstance.parse(dataset, entityList.absolutePath)
        val items = root.getChildrenWithName("item")
        items.forEach {
            val entity = Entity(
                "people",
                it.getFirstChild("name")!!.value!!.value as String,
                it.getFirstChild("label")!!.value!!.value as String,
            )

            entitiesRepository.save(entity)
        }
    }
}
