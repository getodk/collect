package org.odk.collect.android.widgets.support

import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement

object FormFixtures {

    fun selectChoice(
        value: String = "foo",
        index: Int = 0,
        item: TreeElement? = null
    ): SelectChoice {
        return SelectChoice(null, value, false, item, "label").also { it.index = index }
    }

    fun treeElement(
        name: String = "",
        value: String = ""
    ): TreeElement {
        return TreeElement(name).also {
            it.value = StringData(value)
        }
    }

    fun treeElement(
        children: List<TreeElement> = emptyList()
    ): TreeElement {
        return TreeElement("").also {
            children.forEach(it::addChild)
        }
    }
}
