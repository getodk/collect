package org.odk.collect.entities

import android.widget.TextView
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EntityItemViewTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun sortsOrderOfProperties() {
        val view = EntityItemView(context)
        view.setEntity(Entity("songs", listOf(Pair("name", "S.D.O.S"), Pair("length", "2:50"))))

        val propertiesView = view.findViewById<TextView>(R.id.properties)
        assertThat(propertiesView.text, equalTo("length: 2:50, name: S.D.O.S"))
    }
}
