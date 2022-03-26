package org.odk.collect.android.widgets.items

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.support.MockFormEntryPromptBuilder

@RunWith(AndroidJUnit4::class)
class SelectChoicesMapDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `choices without geometry are not included in mappable items`() {
        val choices = listOf(
            SelectChoice(
                null,
                "a",
                false,
                TreeElement("").also { item ->
                    item.addChild(
                        TreeElement("geometry").also {
                            it.value = StringData("12.0 -1.0 305 0")
                        }
                    )
                },
                ""
            ),
            SelectChoice(null, "b", false, TreeElement(""), "")
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(
                choices
            )
            .withSelectChoiceText(
                mapOf(
                    choices[0] to "A",
                    choices[1] to "B"
                )
            )
            .build()

        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(mock(), prompt)
        assertThat(data.getItemCount().value, equalTo(2))
        assertThat(data.getMappableItems().value.size, equalTo(1))
        assertThat(data.getMappableItems().value[0].name, equalTo("A"))
    }
}
