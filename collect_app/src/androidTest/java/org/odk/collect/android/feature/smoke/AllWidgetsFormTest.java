package org.odk.collect.android.feature.smoke;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.rules.BlankFormTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

/**
 * Integration test that runs through a form with all question types.
 *
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */
public class AllWidgetsFormTest {
    public BlankFormTestRule activityTestRule = new BlankFormTestRule("all-widgets.xml", "All widgets");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);
    //endregion

    //region Main test block.
    @Test
    public void testActivityOpen()  {
        skipInitialLabel();

        testStringWidget();
        testStringNumberWidget();

        testUrlWidget();
        testExStringWidget();
        testExPrinterWidget();

        testIntegerWidget();
        testIntegerThousandSeparators();
        testExIntegerWidget();

        testDecimalWidget();
        testExDecimalWidget();

        // Doesn't work when sensor isn't available.
        testBearingWidget();

        testRangeIntegerWidget();
        testRangeDecimalWidget();
        testRangeVerticalAppearance();
        testRangePickerIntegerWidget();
        testRangeRatingIntegerWidget();

        testImageWidget();
        testImageWithoutChooseWidget();
        testSelfieWidget();

        testDrawWidget();
        testAnnotateWidget();
        testSignatureWidget();

        testBarcodeWidget();

        testAudioWidget();
        testVideoWidget();

        testFileWidget();

        testDateNoAppearanceWidget();
        testDateNoCalendarAppearance();
        testDateMonthYearAppearance();
        testDateYearAppearance();

        testTimeNoAppearance();

        testDateTimeNoAppearance();
        testDateTimeNoCalendarAppearance();

        testEthiopianDateAppearance();
        testCopticDateAppearance();
        testIslamicDateAppearance();
        testBikramSambatDateAppearance();
        testMyanmarDateAppearance();
        testPersianDateAppearance();

        testGeopointNoAppearance();
        testGeopointPlacementMapApperance();
        testGeopointMapsAppearance();

        testGeotraceWidget();
        testGeoshapeWidget();

        testOSMIntegrationOSMType();

        testSelectOneNoAppearance();

        testSpinnerWidget();

        testSelectOneAutoAdvance();
        testSelectOneSearchAppearance();
        testSelectOneSearchAutoAdvance();

        testGridSelectNoAppearance();
        testGridSelectCompactAppearance();
        testGridSelectCompact2Appearance();
        testGridSelectQuickCompactAppearance();
        testGridSelectQuickCompact2Appearance();

        testImageSelectOne();

        testLikertWidget();

        testSelectOneFromMapWidget();

        testMultiSelectWidget();
        testMultiSelectAutocompleteWidget();

        testGridSelectMultipleCompact();
        testGridSelectCompact2();

        testSpinnerSelectMultiple();

        testImageSelectMultiple();

        testLabelWidget();

        testRankWidget();

        testTriggerWidget();
    }

    //endregion

    //region Widget tests.

    public void skipInitialLabel() {
        onView(withText(startsWith("Welcome to ODK Collect!"))).perform(swipeLeft());
    }

    public void testStringWidget() {
        onView(withText("String widget")).perform(swipeLeft());
    }

    public void testStringNumberWidget() {
        onView(withText("String number widget")).perform(swipeLeft());
    }

    public void testUrlWidget() {
        onView(withText("URL widget")).perform(swipeLeft());
    }

    public void testExStringWidget() {
        onView(withText("Ex string widget")).perform(swipeLeft());
    }

    public void testExPrinterWidget() {
        onView(withText("Ex printer widget")).perform(swipeLeft());
    }

    public void testIntegerWidget() {
        onView(withText("Integer widget")).perform(swipeLeft());
    }

    public void testIntegerThousandSeparators() {
        onView(withText("Integer widget with thousands separators")).perform(swipeLeft());
    }

    public void testExIntegerWidget() {
        onView(withText("Ex integer widget")).perform(swipeLeft());
    }

    public void testDecimalWidget() {
        onView(withText("Decimal widget")).perform(swipeLeft());
    }

    public void testExDecimalWidget() {
        onView(withText("Ex decimal widget")).perform(swipeLeft());
    }

    public void testBearingWidget() {
        //
        //        intending(hasComponent(BearingActivity.class.getName()))
        //                .respondWith(cancelledResult());
        //
        //        onView(withText("Record Bearing")).perform(click());
        //        onView(withId(R.id.answer_text)).check(matches(withText("")));
        //
        //        double degrees = BearingActivity.normalizeDegrees(randomDecimal());
        //        String bearing = BearingActivity.formatDegrees(degrees);
        //
        //        Intent data = new Intent();
        //        data.putExtra(BEARING_RESULT, bearing);
        //
        //        intending(hasComponent(BearingActivity.class.getName()))
        //                .respondWith(okResult(data));
        //
        //        onView(withText("Record Bearing")).perform(click());
        //        onView(withId(R.id.answer_text))
        //                .check(matches(allOf(isDisplayed(), withText(bearing))));
        //
        //        openWidgetList();
        //        onView(withText("Bearing widget")).perform(click());
        //
        //        onView(withId(R.id.answer_text)).check(matches(withText(bearing)));

        onView(withText("Bearing widget")).perform(swipeLeft());
    }

    public void testRangeIntegerWidget() {
        onView(withText("Range integer widget")).perform(swipeLeft());
    }

    public void testRangeVerticalAppearance() {
        onView(withText("Range vertical integer widget")).perform(swipeLeft());
    }

    public void testRangeDecimalWidget() {
        onView(withText("Range decimal widget")).perform(swipeLeft());
    }

    public void testRangePickerIntegerWidget() {
        onView(withText("Range picker integer widget")).perform(swipeLeft());
    }

    public void testRangeRatingIntegerWidget() {
        onView(withText("Range rating integer widget")).perform(swipeLeft());
    }

    public void testImageWidget() {
        onView(withText("Image widget")).perform(swipeLeft());
    }

    public void testImageWithoutChooseWidget() {
        onView(withText("Image widget without Choose button")).perform(swipeLeft());
    }

    public void testSelfieWidget() {
        onView(withText("Take Picture")).perform(click());
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack();

        onView(withText("Selfie widget")).perform(swipeLeft());
    }

    public void testDrawWidget() {
        onView(withText("Draw widget")).perform(swipeLeft());
    }

    public void testAnnotateWidget() {
        onView(withText("Annotate widget")).perform(swipeLeft());
    }

    public void testSignatureWidget() {
        onView(withText("Signature widget")).perform(swipeLeft());
    }

    public void testBarcodeWidget() {
        onView(withText("Barcode widget")).perform(swipeLeft());
    }

    public void testAudioWidget() {
        onView(withText("Audio widget")).perform(swipeLeft());
    }

    public void testVideoWidget() {
        onView(withText("Video widget")).perform(swipeLeft());
    }

    public void testFileWidget() {
        onView(withText("File widget")).perform(swipeLeft());
    }

    public void testDateNoAppearanceWidget() {
        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testDateNoCalendarAppearance() {
        onView(withText("Date Widget")).perform(swipeLeft());
    }

    public void testDateMonthYearAppearance() {
        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testDateYearAppearance() {
        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testTimeNoAppearance() {
        onView(withText("Time widget")).perform(swipeLeft());
    }

    public void testDateTimeNoAppearance() {
        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testDateTimeNoCalendarAppearance() {
        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testEthiopianDateAppearance() {
        onView(allOf(withText("Ethiopian date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testCopticDateAppearance() {
        onView(allOf(withText("Coptic date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testIslamicDateAppearance() {
        onView(allOf(withText("Islamic date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testBikramSambatDateAppearance() {
        onView(allOf(withText("Bikram Sambat date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testMyanmarDateAppearance() {
        onView(allOf(withText("Myanmar date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testPersianDateAppearance() {
        onView(allOf(withText("Persian date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testGeopointNoAppearance() {
        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeopointPlacementMapApperance() {
        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeopointMapsAppearance() {
        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeotraceWidget() {
        onView(withText("Start GeoTrace")).perform(click());
        pressBack();

        onView(withText("Geotrace widget")).perform(swipeLeft());
    }

    public void testGeoshapeWidget() {
        onView(withText("Start GeoShape")).perform(click());
        pressBack();

        onView(withText("Geoshape widget")).perform(swipeLeft());
    }

    public void testOSMIntegrationOSMType() {
        onView(withText("OSM integration")).perform(swipeLeft());
    }

    public void testSelectOneNoAppearance() {
        onView(withText("Select one widget")).perform(swipeLeft());
    }

    public void testSpinnerWidget() {
        onView(withText("Spinner widget")).perform(swipeLeft());
    }

    public void testSelectOneAutoAdvance() {
        onView(withText("Select one autoadvance widget")).perform(swipeLeft());
    }

    public void testSelectOneSearchAppearance() {
        onView(withText("Select one search widget")).perform(swipeLeft());
    }

    public void testSelectOneSearchAutoAdvance() {
        onView(withText("Select one search widget")).perform(swipeLeft());
    }

    public void testGridSelectNoAppearance() {
        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectCompactAppearance() {
        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectCompact2Appearance() {
        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectQuickCompactAppearance() {
        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectQuickCompact2Appearance() {
        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testImageSelectOne() {
        onView(withText("Image select one widget")).perform(swipeLeft());
    }

    public void testLikertWidget() {
        onView(withText("Likert widget")).perform(swipeLeft());
    }

    public void testSelectOneFromMapWidget() {
        onView(withText("Select place")).perform(click());
        pressBack();

        onView(withText("Select one from map widget")).perform(swipeLeft());
    }

    public void testMultiSelectWidget() {
        onView(withText("Multi select widget")).perform(swipeLeft());
    }

    public void testMultiSelectAutocompleteWidget() {
        onView(withText("Multi select autocomplete widget")).perform(swipeLeft());
    }

    public void testGridSelectMultipleCompact() {
        onView(withText("Grid select multiple widget")).perform(swipeLeft());
    }

    public void testGridSelectCompact2() {
        onView(withText("Grid select multiple widget")).perform(swipeLeft());
    }

    public void testSpinnerSelectMultiple() {
        onView(withText("Spinner widget: select multiple")).perform(swipeLeft());
    }

    public void testImageSelectMultiple() {
        onView(withText("Image select multiple widget")).perform(swipeLeft());
    }

    public void testLabelWidget() {
        onView(withText("Label widget")).perform(swipeLeft());
    }

    public void testRankWidget() {
        onView(withText("Rank widget")).perform(swipeLeft());
    }

    public void testTriggerWidget() {
        onView(withText("Trigger widget")).perform(click());
    }
    //endregion
}
