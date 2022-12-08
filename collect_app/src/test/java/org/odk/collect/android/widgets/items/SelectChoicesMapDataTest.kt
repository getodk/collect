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
import org.odk.collect.android.R
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.support.FormFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormFixtures.treeElement
import org.odk.collect.androidtest.getOrAwaitValue
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
    fun `choices with geometry with latitude greater than bounds are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "90.01 0 0 0")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "80.00 0 0 0")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("b"))
    }

    @Test
    fun `choices with geometry with latitude less than bounds are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "-90.01 0 0 0")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "80.00 0 0 0")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("b"))
    }

    @Test
    fun `choices with geometry with longitude greater than bounds are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 180.01 0 0")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 170.00 0 0")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("b"))
    }

    @Test
    fun `choices with geometry with longitude less than bounds are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 -180.01 0 0")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 170.00 0 0")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("b"))
    }

    @Test
    fun `choices with incorrect geometry are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "blah")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 170.00 0 0")
                    )
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("b"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `marker symbol and color are pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "12.0 -1.0 305 0"),
                        treeElement("marker-symbol", "A"),
                        treeElement("marker-color", "#ffffff")
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
        val item = data.getMappableItems().getOrAwaitValue()!![0]
        assertThat(item.symbol, equalTo("A"))
        assertThat(item.color, equalTo("#ffffff"))
    }

    @Test
    fun `uses different icon if marker-symbol is defined`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "12.0 -1.0 305 0"),
                        treeElement("marker-symbol", "A")
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
        val item = data.getMappableItems().getOrAwaitValue()!![0]
        assertThat(item.smallIcon, equalTo(R.drawable.ic_map_marker_small))
        assertThat(item.largeIcon, equalTo(R.drawable.ic_map_marker_big))
    }

    private fun loadDataForPrompt(prompt: FormEntryPrompt): SelectChoicesMapData {
        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        scheduler.runBackground()
        return data
    }
}
