package org.odk.collect.android.feature.smoke

import org.junit.Rule
import org.junit.Test
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.BlankFormTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

/**
 * Integration test that runs through a form with all question types.
 */
class AllWidgetsFormTest {

    private val activityTestRule = BlankFormTestRule("all-widgets.xml", "All widgets")

    @get:Rule
    val copyFormChain = chain()
        .around(activityTestRule)

    @Test
    fun testActivityOpen() {
        activityTestRule.startInFormEntry()
            .swipeToNextQuestion("String widget")
            .swipeToNextQuestion("String number widget")
            .swipeToNextQuestion("URL widget")
            .swipeToNextQuestion("Ex string widget")
            .swipeToNextQuestion("Ex printer widget")

            .swipeToNextQuestion("Integer widget")
            .swipeToNextQuestion("Integer widget with thousands separators")
            .swipeToNextQuestion("Ex integer widget")
            .swipeToNextQuestion("Decimal widget")
            .swipeToNextQuestion("Ex decimal widget")
            .swipeToNextQuestion("Bearing widget")

            .swipeToNextQuestion("Range integer widget")
            .swipeToNextQuestion("Range decimal widget")
            .swipeToNextQuestion("Range vertical integer widget")
            .swipeToNextQuestion("Range picker integer widget")
            .swipeToNextQuestion("Range rating integer widget")

            .swipeToNextQuestion("Image widget")
            .swipeToNextQuestion("Image widget without Choose button")
            .swipeToNextQuestion("Selfie widget")
            .clickOnText("Take Picture")
            .pressBack(FormEntryPage("All widgets"))
            .swipeToNextQuestion("Draw widget")
            .swipeToNextQuestion("Annotate widget")
            .swipeToNextQuestion("Signature widget")
            .swipeToNextQuestion("Barcode widget")
            .swipeToNextQuestion("Audio widget")
            .swipeToNextQuestion("Video widget")
            .swipeToNextQuestion("File widget")

            .swipeToNextQuestion("Date widget")
            .swipeToNextQuestion("Date Widget")
            .swipeToNextQuestion("Date widget")
            .swipeToNextQuestion("Date widget")
            .swipeToNextQuestion("Time widget")
            .swipeToNextQuestion("Date time widget")
            .swipeToNextQuestion("Date time widget")
            .swipeToNextQuestion("Ethiopian date widget")
            .swipeToNextQuestion("Coptic date widget")
            .swipeToNextQuestion("Islamic date widget")
            .swipeToNextQuestion("Bikram Sambat date widget")
            .swipeToNextQuestion("Myanmar date widget")
            .swipeToNextQuestion("Persian date widget")

            .swipeToNextQuestion("Geopoint widget")
            .swipeToNextQuestion("Geopoint widget")
            .swipeToNextQuestion("Geopoint widget")
            .swipeToNextQuestion("Geotrace widget")
            .clickOnText("Start GeoTrace")
            .pressBack(FormEntryPage("All widgets"))
            .swipeToNextQuestion("Geoshape widget")
            .clickOnText("Start GeoShape")
            .pressBack(FormEntryPage("All widgets"))
            .swipeToNextQuestion("OSM integration")

            .swipeToNextQuestion("Select one widget")
            .swipeToNextQuestion("Spinner widget")
            .swipeToNextQuestion("Select one autoadvance widget")
            .swipeToNextQuestion("Select one search widget")
            .swipeToNextQuestion("Select one search widget")
            .swipeToNextQuestion("Grid select one widget")
            .swipeToNextQuestion("Grid select one widget")
            .swipeToNextQuestion("Grid select one widget")
            .swipeToNextQuestion("Grid select one widget")
            .swipeToNextQuestion("Grid select one widget")
            .swipeToNextQuestion("Image select one widget")
            .swipeToNextQuestion("Likert widget")
            .swipeToNextQuestion("Select one from map widget")
            .clickOnText("Select place")
            .pressBack(FormEntryPage("All widgets"))
            .swipeToNextQuestion("Multi select widget")
            .swipeToNextQuestion("Multi select autocomplete widget")
            .swipeToNextQuestion("Grid select multiple widget")
            .swipeToNextQuestion("Grid select multiple widget")
            .swipeToNextQuestion("Spinner widget: select multiple")
            .swipeToNextQuestion("Image select multiple widget")

            .swipeToNextQuestion("Label widget")
            .swipeToNextQuestion("Rank widget")
            .swipeToNextQuestion("Trigger widget")
    }
}
