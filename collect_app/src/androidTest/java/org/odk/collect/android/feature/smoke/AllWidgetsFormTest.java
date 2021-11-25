package org.odk.collect.android.feature.smoke;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

/**
 * Integration test that runs through a form with all question types.
 *
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */
public class AllWidgetsFormTest {

    @ClassRule
    public static final LocaleTestRule LOCALE_TEST_RULE = new LocaleTestRule();

    public FormActivityTestRule activityTestRule = new FormActivityTestRule("all-widgets.xml", "All widgets");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new CopyFormRule("all-widgets.xml", true))
            .around(activityTestRule);

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }
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
        testSelfieVideoWidget();

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
        // captures screenshot of string widget
        Screengrab.screenshot("string-input");

        onView(withText("String widget")).perform(swipeLeft());
    }

    public void testStringNumberWidget() {
        Screengrab.screenshot("string-number");

        onView(withText("String number widget")).perform(swipeLeft());
    }

    public void testUrlWidget() {
        onView(withText("URL widget")).perform(swipeLeft());
    }

    public void testExStringWidget() {
        Screengrab.screenshot("ex-string");

        onView(withText("Ex string widget")).perform(swipeLeft());
    }

    public void testExPrinterWidget() {
        Screengrab.screenshot("ex-printer");

        onView(withText("Ex printer widget")).perform(swipeLeft());
    }

    public void testIntegerWidget() {
        Screengrab.screenshot("integer");

        onView(withText("Integer widget")).perform(swipeLeft());
    }

    public void testIntegerThousandSeparators() {
        Screengrab.screenshot("integer-separators");

        onView(withText("Integer widget with thousands separators")).perform(swipeLeft());
    }

    public void testExIntegerWidget() {
        Screengrab.screenshot("ex-integer");

        onView(withText("Ex integer widget")).perform(swipeLeft());
    }

    public void testDecimalWidget() {
        Screengrab.screenshot("decimal1");

        onView(withText("Decimal widget")).perform(swipeLeft());
    }

    public void testExDecimalWidget() {
        Screengrab.screenshot("ex-decimal");

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

        Screengrab.screenshot("bearing-widget");

        onView(withText("Bearing widget")).perform(swipeLeft());
    }

    public void testRangeIntegerWidget() {
        Screengrab.screenshot("range-integer");

        onView(withText("Range integer widget")).perform(swipeLeft());
    }

    public void testRangeVerticalAppearance() {
        Screengrab.screenshot("range-integer-vertical");

        onView(withText("Range vertical integer widget")).perform(swipeLeft());
    }

    public void testRangeDecimalWidget() {
        Screengrab.screenshot("range-decimal");

        onView(withText("Range decimal widget")).perform(swipeLeft());
    }

    public void testRangePickerIntegerWidget() {
        Screengrab.screenshot("Range-picker-integer-widget");

        onView(withText("Range picker integer widget")).perform(swipeLeft());
    }

    public void testRangeRatingIntegerWidget() {
        Screengrab.screenshot("Range-rating-integer-widget");

        onView(withText("Range rating integer widget")).perform(swipeLeft());
    }

    public void testImageWidget() {
        Screengrab.screenshot("image-widget");

        onView(withText("Image widget")).perform(swipeLeft());
    }

    public void testImageWithoutChooseWidget() {
        Screengrab.screenshot("image-without-choose-widget");

        onView(withText("Image widget without Choose button")).perform(swipeLeft());
    }

    public void testSelfieWidget() {
        Screengrab.screenshot("selfie-widget");

        onView(withText("Selfie widget")).perform(swipeLeft());
    }

    public void testDrawWidget() {
        Screengrab.screenshot("draw-widget");

        onView(withText("Draw widget")).perform(swipeLeft());
    }

    public void testAnnotateWidget() {
        Screengrab.screenshot("annotate");

        onView(withText("Annotate widget")).perform(swipeLeft());
    }

    public void testSignatureWidget() {
        Screengrab.screenshot("signature");

        onView(withText("Signature widget")).perform(swipeLeft());
    }

    public void testBarcodeWidget() {
        Screengrab.screenshot("barcode-widget");

        onView(withText("Barcode widget")).perform(swipeLeft());
    }

    public void testAudioWidget() {
        Screengrab.screenshot("audio");

        onView(withText("Audio widget")).perform(swipeLeft());
    }

    public void testVideoWidget() {
        Screengrab.screenshot("video");

        onView(withText("Video widget")).perform(swipeLeft());
    }

    public void testSelfieVideoWidget() {
        Screengrab.screenshot("selfie-video");

        onView(withText("Selfie video widget")).perform(swipeLeft());

    }

    public void testFileWidget() {
        Screengrab.screenshot("file-widget");

        onView(withText("File widget")).perform(swipeLeft());
    }

    public void testDateNoAppearanceWidget() {
        Screengrab.screenshot("date-no-appearance");

        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testDateNoCalendarAppearance() {
        Screengrab.screenshot("date-no-calendar");

        onView(withText("Date Widget")).perform(swipeLeft());
    }

    public void testDateMonthYearAppearance() {
        Screengrab.screenshot("date-with-calendar");

        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testDateYearAppearance() {
        Screengrab.screenshot("date-year");

        onView(withText("Date widget")).perform(swipeLeft());
    }

    public void testTimeNoAppearance() {
        Screengrab.screenshot("time-no-appearance");

        onView(withText("Time widget")).perform(swipeLeft());
    }

    public void testDateTimeNoAppearance() {
        Screengrab.screenshot("date-time");

        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testDateTimeNoCalendarAppearance() {
        Screengrab.screenshot("date-time-appear");

        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testEthiopianDateAppearance() {
        Screengrab.screenshot("ethopian");

        onView(allOf(withText("Ethiopian date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testCopticDateAppearance() {
        Screengrab.screenshot("coptic");

        onView(allOf(withText("Coptic date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testIslamicDateAppearance() {
        Screengrab.screenshot("islamic-date");

        onView(allOf(withText("Islamic date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testBikramSambatDateAppearance() {
        Screengrab.screenshot("bikram-sambat-date");

        onView(allOf(withText("Bikram Sambat date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testMyanmarDateAppearance() {
        Screengrab.screenshot("myanmar-date");

        onView(allOf(withText("Myanmar date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testPersianDateAppearance() {
        Screengrab.screenshot("persian-date");

        onView(allOf(withText("Persian date widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
    }

    public void testGeopointNoAppearance() {
        Screengrab.screenshot("geopoint");

        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeopointPlacementMapApperance() {
        Screengrab.screenshot("geopoint2");

        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeopointMapsAppearance() {
        Screengrab.screenshot("geopint-map");

        onView(withText("Geopoint widget")).perform(swipeLeft());
    }

    public void testGeotraceWidget() {
        Screengrab.screenshot("geo-trace");

        onView(withText("Geotrace widget")).perform(swipeLeft());
    }

    public void testGeoshapeWidget() {
        Screengrab.screenshot("geo-space");

        onView(withText("Geoshape widget")).perform(swipeLeft());
    }

    public void testOSMIntegrationOSMType() {
        Screengrab.screenshot("osm");

        onView(withText("OSM integration")).perform(swipeLeft());
    }

    public void testSelectOneNoAppearance() {
        Screengrab.screenshot("select-one");

        onView(withText("Select one widget")).perform(swipeLeft());
    }

    public void testSpinnerWidget() {
        Screengrab.screenshot("spinner");

        onView(withText("Spinner widget")).perform(swipeLeft());
    }

    public void testSelectOneAutoAdvance() {
        Screengrab.screenshot("select-auto-advance");

        onView(withText("Select one autoadvance widget")).perform(swipeLeft());
    }

    public void testSelectOneSearchAppearance() {
        Screengrab.screenshot("select-search-appearance");

        onView(withText("Select one search widget")).perform(swipeLeft());
    }

    public void testSelectOneSearchAutoAdvance() {
        Screengrab.screenshot("one-auto");

        onView(withText("Select one search widget")).perform(swipeLeft());
    }

    public void testGridSelectNoAppearance() {
        Screengrab.screenshot("grid-no-appearance");

        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectCompactAppearance() {
        Screengrab.screenshot("grid-select-compact1");

        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectCompact2Appearance() {
        Screengrab.screenshot("grid-select-compact2");

        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectQuickCompactAppearance() {
        Screengrab.screenshot("grid-select1");

        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testGridSelectQuickCompact2Appearance() {
        Screengrab.screenshot("grid-select2");

        onView(withText("Grid select one widget")).perform(swipeLeft());
    }

    public void testImageSelectOne() {
        Screengrab.screenshot("image-select1");

        onView(withText("Image select one widget")).perform(swipeLeft());
    }

    public void testLikertWidget() {
        Screengrab.screenshot("likert-widget");

        onView(withText("Likert widget")).perform(swipeLeft());
    }

    public void testMultiSelectWidget() {
        Screengrab.screenshot("multi-select");

        onView(withText("Multi select widget")).perform(swipeLeft());
    }

    public void testMultiSelectAutocompleteWidget() {
        Screengrab.screenshot("multi-select-autocomplete");

        onView(withText("Multi select autocomplete widget")).perform(swipeLeft());
    }

    public void testGridSelectMultipleCompact() {
        Screengrab.screenshot("grid-multi1");

        onView(withText("Grid select multiple widget")).perform(swipeLeft());
    }

    public void testGridSelectCompact2() {
        Screengrab.screenshot("grid-multi2");

        onView(withText("Grid select multiple widget")).perform(swipeLeft());
    }

    public void testSpinnerSelectMultiple() {
        Screengrab.screenshot("spinner-select");

        onView(withText("Spinner widget: select multiple")).perform(swipeLeft());
    }

    public void testImageSelectMultiple() {
        Screengrab.screenshot("image-select-multiple");

        onView(withText("Image select multiple widget")).perform(swipeLeft());
    }

    public void testLabelWidget() {
        Screengrab.screenshot("label-widget");

        onView(withText("Label widget")).perform(swipeLeft());
    }

    public void testRankWidget() {
        Screengrab.screenshot("rank-widget");

        onView(withText("Rank widget")).perform(swipeLeft());
    }

    public void testTriggerWidget() {
        // captures screenshot of trigger widget
        Screengrab.screenshot("trigger-widget");

        onView(withText("Trigger widget")).perform(click());
    }

    public void testSubmission() {

    }
    //endregion
}
