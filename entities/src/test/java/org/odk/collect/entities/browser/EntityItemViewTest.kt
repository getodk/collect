package org.odk.collect.entities.browser

import androidx.core.view.isVisible
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.entities.storage.Entity
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
            Entity.Saved(
                "1",
                "S.D.O.S",
                properties = listOf(Pair("name", "S.D.O.S"), Pair("length", "2:50")),
                index = 0
            )
        )

        val propertiesView = view.binding.properties
        assertThat(propertiesView.text, equalTo("length: 2:50\nname: S.D.O.S"))
    }

    @Test
    fun `shows offline pill when entity is offline`() {
        val view = EntityItemView(context)
        val entity = Entity.Saved("1", "S.D.O.S", index = 0)

        view.setEntity(entity.copy(state = Entity.State.OFFLINE))
        assertThat(view.binding.offlinePill.isVisible, equalTo(true))

        view.setEntity(entity.copy(state = Entity.State.ONLINE))
        assertThat(view.binding.offlinePill.isVisible, equalTo(false))
    }

    @Test
    fun `shows id and version`() {
        val view = EntityItemView(context)
        val entity = Entity.Saved("1", "S.D.O.S", version = 11, index = 0)

        view.setEntity(entity.copy(state = Entity.State.OFFLINE))
        assertThat(view.binding.id.text, equalTo("${entity.id} (${entity.version})"))
    }
}
