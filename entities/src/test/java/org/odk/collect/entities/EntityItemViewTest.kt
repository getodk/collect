package org.odk.collect.entities

import androidx.core.view.isVisible
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EntityItemViewTest {

    private val context = RuntimeEnvironment.getApplication().also {
        it.setTheme(com.google.android.material.R.style.Theme_Material3_DayNight)
    }

    @Test
    fun `sorts order of properties`() {
        val view = EntityItemView(context)
        view.setEntity(
            Entity(
                "songs",
                "1",
                "S.D.O.S",
                properties = listOf(Pair("name", "S.D.O.S"), Pair("length", "2:50"))
            )
        )

        val propertiesView = view.binding.properties
        assertThat(propertiesView.text, equalTo("length: 2:50, name: S.D.O.S"))
    }

    @Test
    fun `shows offline pill when entity is offline`() {
        val view = EntityItemView(context)
        val entity = Entity("songs", "1", "S.D.O.S")

        view.setEntity(entity.copy(offline = true))
        assertThat(view.binding.offlinePill.isVisible, equalTo(true))

        view.setEntity(entity.copy(offline = false))
        assertThat(view.binding.offlinePill.isVisible, equalTo(false))
    }
}
