package org.odk.collect.android.widgets.support

import org.javarosa.core.model.SelectChoice

object FormFixtures {

    fun selectChoice(): SelectChoice {
        return SelectChoice(null, "foo").also { it.index = 0 }
    }

    fun SelectChoice.copy(value: String = this.value, index: Int = this.index): SelectChoice {
        return SelectChoice(null, value).also { it.index = index }
    }
}
