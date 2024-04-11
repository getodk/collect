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
import org.odk.collect.android.widgets.support.FormElementFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormElementFixtures.treeElement
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.geo.selection.IconifiedText
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.maps.MapPoint
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SelectChoicesMapDataTest {

    private val scheduler = FakeScheduler()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `choices with geo trace format geometry have multiple points`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(treeElement("geometry", "12.0 -1.0 3 4; 12.1 -1.0 3 4"))
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().getOrAwaitValue(), equalTo(1))

        val mappableItems = data.getMappableItems().getOrAwaitValue()!!
        assertThat(mappableItems.size, equalTo(1))

        val points = (mappableItems[0] as MappableSelectItem.MappableSelectLine).points
        assertThat(
            points,
            equalTo(listOf(MapPoint(12.0, -1.0, 3.0, 4.0), MapPoint(12.1, -1.0, 3.0, 4.0)))
        )
    }

    @Test
    fun `choices with geo shape format geometry have multiple points`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(treeElement("geometry", "12.0 -1.0 3 4; 12.1 -1.0 3 4; 12.0 -1.0 3 4"))
                )
            )
        )

        val prompt = MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().getOrAwaitValue(), equalTo(1))

        val mappableItems = data.getMappableItems().getOrAwaitValue()!!
        assertThat(mappableItems.size, equalTo(1))

        val points = (mappableItems[0] as MappableSelectItem.MappableSelectPolygon).points
        assertThat(
            points,
            equalTo(listOf(MapPoint(12.0, -1.0, 3.0, 4.0), MapPoint(12.1, -1.0, 3.0, 4.0), MapPoint(12.0, -1.0, 3.0, 4.0)))
        )
    }

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
        assertThat(data.isLoading().value, equalTo(true))

        scheduler.runForeground()
        assertThat(data.isLoading().value, equalTo(false))
    }

    @Test
    fun `choices with incorrect geometry are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 170.00 0 0")
                    )
                )
            ),
            // Invalid
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "blah")
                    )
                )
            ),
            // Out of bounds
            selectChoice(
                value = "c",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 180.1 0 0")
                    )
                )
            ),
            // Second point out of bounds
            selectChoice(
                value = "c",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "0 180 0 0; 0 180.1 0 0")
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
        assertThat(data.getMappableItems().value!![0].name, equalTo("a"))
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
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPoint
        assertThat(item.symbol, equalTo("A"))
        assertThat(item.color, equalTo("#ffffff"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `line stroke color is pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "12.0 -1.0 3 4; 12.1 -1.0 3 4"),
                        treeElement("stroke", "#ffffff")
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
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectLine
        assertThat(item.strokeColor, equalTo("#ffffff"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `polygon fill color is pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement("geometry", "12.0 -1.0 3 4; 12.1 -1.0 3 4; 12.0 -1.0 3 4"),
                        treeElement("fill", "#ffffff")
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
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPolygon
        assertThat(item.fillColor, equalTo("#ffffff"))
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
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPoint
        assertThat(item.smallIcon, equalTo(org.odk.collect.icons.R.drawable.ic_map_marker_small))
        assertThat(item.largeIcon, equalTo(org.odk.collect.icons.R.drawable.ic_map_marker_big))
    }

    private fun loadDataForPrompt(prompt: FormEntryPrompt): SelectChoicesMapData {
        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        scheduler.flush()
        return data
    }
}
