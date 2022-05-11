package org.odk.collect.android.widgets.items

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.items.SelectChoicesMapData.InvalidGeometry
import org.odk.collect.android.widgets.support.FormFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormFixtures.treeElement
import org.odk.collect.geo.selection.MappableSelectItem.IconifiedText
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SelectChoicesMapDataTest {

    private val scheduler = FakeScheduler()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `choices without geometry are not included in mappable items`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(children = listOf(treeElement("geometry", "12.0 -1.0 305 0")))
            ),
            selectChoice(
                value = "b",
                item = treeElement(children = emptyList())
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A", choices[1] to "B"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().value, equalTo(2))
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("A"))
    }

    @Test
    fun `additional children are returned as properties`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "12.0 -1.0 305 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val properties = data.getMappableItems().value!![0].properties
        assertThat(properties.size, equalTo(1))
        assertThat(properties[0], equalTo(IconifiedText(null, "property: blah")))
    }

    @Test
    fun `isLoading is true and items is null when items are being loaded from choices`() {
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(emptyList())
            .withSelectChoiceText(emptyMap())
            .build()

        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        assertThat(data.isLoading().value, equalTo(true))
        assertThat(data.getMappableItems().value, equalTo(null))

        scheduler.runBackground()
        assertThat(data.isLoading().value, equalTo(false))
    }

    @Test
    fun `geometry with latitude greater than bounds is invalid`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "90.01 0 0 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.hasInvalidGeometry().value, equalTo(InvalidGeometry("A", "90.01 0 0 0")))
    }

    @Test
    fun `geometry with latitude less than bounds is invalid`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "-90.01 0 0 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.hasInvalidGeometry().value, equalTo(InvalidGeometry("A", "-90.01 0 0 0")))
    }

    @Test
    fun `geometry with longitude greater than bounds is invalid`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 180.01 0 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.hasInvalidGeometry().value, equalTo(InvalidGeometry("A", "0 180.01 0 0")))
    }

    @Test
    fun `geometry with longitude less than bounds is invalid`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 -180.01 0 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.hasInvalidGeometry().value, equalTo(InvalidGeometry("A", "0 -180.01 0 0")))
    }

    private fun loadDataForPrompt(prompt: FormEntryPrompt): SelectChoicesMapData {
        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        scheduler.runBackground()
        return data
    }
}
