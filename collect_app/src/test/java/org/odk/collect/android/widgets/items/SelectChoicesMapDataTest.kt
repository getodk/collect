package org.odk.collect.android.widgets.items

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.odk.collect.android.support.MockFormEntryPromptBuilder

class SelectChoicesMapDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `choices without geometry are not included in mappable items`() {
        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(
                listOf(
                    SelectChoice(
                        null,
                        "A",
                        "a",
                        false,
                        TreeElement("").also { item ->
                            item.addChild(
                                TreeElement("geometry").also {
                                    it.value = StringData("12.0 -1.0 305 0")
                                }
                            )
                        }
                    ),
                    SelectChoice(null, "B", "b", false, TreeElement(""))
                )
            )
            .build()

        val data = SelectChoicesMapData(mock(), prompt)
        assertThat(data.getItemCount().value, equalTo(2))
        assertThat(data.getMappableItems().value.size, equalTo(1))
        assertThat(data.getMappableItems().value[0].name, equalTo("A"))
    }
}
