package org.odk.collect.android.widgets.items

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.widgets.support.FormElementFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormElementFixtures.treeElement
import org.odk.collect.geo.items.MappableItem

class MappableItemsParserTest {

    @Test
    fun `uses Options color when point marker color is blank`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(GeoSelectChoiceElements.GEOMETRY, "1.0 -1.0 0 0"),
                        treeElement(GeoSelectChoiceElements.MARKER_COLOR, "")
                    )
                )
            )
        )

        val mappableItems = MappableItemsParser.parseChoices(
            choices,
            MappableItemsParser.Options(color = "#ffffff")
        )
        assertThat((mappableItems[0] as MappableItem.Point).color, equalTo("#ffffff"))
    }

    @Test
    fun `uses Options color when line stroke is blank`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(GeoSelectChoiceElements.GEOMETRY, "1.0 -1.0 0 0;2.0 -2.0 0 0"),
                        treeElement(GeoSelectChoiceElements.STROKE, "")
                    )
                )
            )
        )

        val mappableItems = MappableItemsParser.parseChoices(
            choices,
            MappableItemsParser.Options(color = "#ffffff")
        )
        assertThat((mappableItems[0] as MappableItem.Line).strokeColor, equalTo("#ffffff"))
    }

    @Test
    fun `uses Options color when polygon stroke is blank`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(GeoSelectChoiceElements.GEOMETRY, "1.0 -1.0 0 0;1.0 -1.0 0 0"),
                        treeElement(GeoSelectChoiceElements.STROKE, "")
                    )
                )
            )
        )

        val mappableItems = MappableItemsParser.parseChoices(
            choices,
            MappableItemsParser.Options(color = "#ffffff")
        )
        assertThat((mappableItems[0] as MappableItem.Polygon).strokeColor, equalTo("#ffffff"))
    }

    @Test
    fun `uses Options color when polygon fill is blank`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(GeoSelectChoiceElements.GEOMETRY, "1.0 -1.0 0 0;1.0 -1.0 0 0"),
                        treeElement(GeoSelectChoiceElements.FILL, "")
                    )
                )
            )
        )

        val mappableItems = MappableItemsParser.parseChoices(
            choices,
            MappableItemsParser.Options(color = "#ffffff")
        )
        assertThat((mappableItems[0] as MappableItem.Polygon).fillColor, equalTo("#ffffff"))
    }
}
