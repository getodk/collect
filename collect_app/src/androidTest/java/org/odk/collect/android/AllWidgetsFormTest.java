package org.odk.collect.android;

import android.Manifest;
import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import net.bytebuddy.utility.RandomString;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ActivityAvailability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.app.Activity.RESULT_OK;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

// import android.support.annotation.Nullable;
// import org.odk.collect.android.activities.BearingActivity;
// import static android.app.Activity.RESULT_CANCELED;
//import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
//import static org.odk.collect.android.activities.FormEntryActivity.BEARING_RESULT;

/**
 * Integration test that runs through a form with all question types.
 *
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */

@RunWith(AndroidJUnit4.class)
public class AllWidgetsFormTest {

    private static final String ALL_WIDGETS_FORM = "all-widgets.xml";
    private static final String FORMS_DIRECTORY = "/odk/forms/";

    private final Random random = new Random();
    private final ActivityResult okResult = new ActivityResult(RESULT_OK, new Intent());

    @ClassRule
    public static final LocaleTestRule LOCALE_TEST_RULE = new LocaleTestRule();

    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    private ActivityAvailability activityAvailability;

    //region Test prep.
    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname = formPath();
        if (new File(pathname).exists()) {
            return;
        }

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(ALL_WIDGETS_FORM);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Before
    public void prepareDependencies() {
        FormEntryActivity activity = activityTestRule.getActivity();
        activity.setActivityAvailability(activityAvailability);
        activity.setShouldOverrideAnimations(true);
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

        testImageWidget();
        testImageWithoutChooseWidget();
        testSelfieWidget();

        testDrawWidget();
        testAnnotateWidget();
        testSignatureWidget();

        testWebViewImageWidget();
        testAlignImageWidget();

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

        testGeopointNoAppearance();
        testGeopointPlacementMapApperance();
        testGeopointMapsAppearance();

        testGeotraceWidget();
        testGeoshapeWidget();

        testOSMIntegrationOSMType();
        testOSMIntegrationBuildingType();

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

        testMultiSelectWidget();

        testGridSelectMultipleCompact();
        testGridSelectCompact2();

        testSpinnerSelectMultiple();

        testImageSelectMultiple();

        testLabelWidget();

        testTriggerWidget(false);

        testTriggerWidget(true);
    }
    //endregion

    //region Widget tests.

    public void skipInitialLabel() {

        onView(withText(startsWith("Welcome to ODK Collect!"))).perform(swipeLeft());

    }

    public void testStringWidget() {
        String stringWidgetText = randomString();

        onVisibleEditText().perform(replaceText(stringWidgetText));

        // captures screenshot of string widget
        Screengrab.screenshot("string-input");

        openWidgetList();
        onView(withText("String widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringWidgetText)));

        onView(withText("String widget")).perform(swipeLeft());
    }


    public void testStringNumberWidget() {
        String stringNumberWidgetText = randomIntegerString();

        onVisibleEditText().perform(replaceText(stringNumberWidgetText));

        Screengrab.screenshot("string-number");

        openWidgetList();

        onView(withText("String number widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringNumberWidgetText)));

        onView(withText("String number widget")).perform(swipeLeft());

    }

    public void testUrlWidget() {
        Uri uri = Uri.parse("http://opendatakit.org/");

        intending(allOf(hasAction(Intent.ACTION_VIEW), hasData(uri)))
                .respondWith(okResult);

        Screengrab.screenshot("url");

        onView(withId(R.id.simple_button)).perform(click());
        onView(withText("URL widget")).perform(swipeLeft());
    }

    public void testExStringWidget() {

        // Manually input the value:
        String exStringWidgetFirstText = randomString();

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
               .thenReturn(false);

        onView(withText("Launch")).perform(click());
        onVisibleEditText().perform(replaceText(exStringWidgetFirstText));

        openWidgetList();
        onView(withText("Ex string widget")).perform(click());

        Screengrab.screenshot("ex-string");

        onVisibleEditText().check(matches(withText(exStringWidgetFirstText)));

        // Replace with Intent value:
        String exStringWidgetSecondText = randomString();

        Intent stringIntent = new Intent();
        stringIntent.putExtra("value", exStringWidgetSecondText);

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);
        intending(allOf(
                hasAction("change.uw.android.BREATHCOUNT"),
                hasExtra("value", exStringWidgetFirstText)

        )).respondWith(exStringResult);

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        onView(withText("Launch")).perform(click());
        onView(withText(exStringWidgetSecondText))
                .check(matches(isDisplayed()));

        Screengrab.screenshot("ex-string2");

        openWidgetList();
        onView(withText("Ex string widget")).perform(click());

        onVisibleEditText().check(matches(withText(exStringWidgetSecondText)));

        onView(withText("Ex string widget")).perform(swipeLeft());
    }

    public void testExPrinterWidget() {
        onView(withText("Initiate Printing")).perform(click());

        Screengrab.screenshot("ex-printer");

        intending(hasAction("org.opendatakit.sensors.ZebraPrinter"));
        intended(hasAction("org.opendatakit.sensors.ZebraPrinter"));

        // There is also a BroadcastIntent that sends the data but we don't
        // have a way to test that currently.
        // Will probably move that out to a helper class we can Unit test in Robolectric and
        // inject here.

        onView(withText("Ex printer widget")).perform(swipeLeft());
    }

    public void testIntegerWidget() {
        String integerString = randomIntegerString();
        onVisibleEditText().perform(replaceText(integerString));

        Screengrab.screenshot("integer");

        openWidgetList();
        onView(withText("Integer widget")).perform(click());

        onVisibleEditText().check(matches(withText(integerString)));

        onView(withText("Integer widget")).perform(swipeLeft());
    }

     public void testIntegerThousandSeparators() {
          String randomInteger = randomIntegerSeparator();
          onVisibleEditText().perform(replaceText(randomInteger));

          Screengrab.screenshot("integer-separators");

          openWidgetList();
          onView(withText("Integer widget with thousands separators")).perform(click());

          onVisibleEditText().check(matches(withText(randomInteger)));

          onView(withText("Integer widget with thousands separators")).perform(swipeLeft());

      }

    public void testExIntegerWidget() {
        // Manually input the value:
        String exIntegerFirstValue = randomIntegerString();

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

        onView(withText("Launch")).perform(click());
        onVisibleEditText().perform(replaceText(exIntegerFirstValue));

        Screengrab.screenshot("ex-integer");

        openWidgetList();
        onView(withText("Ex integer widget")).perform(click());

        onVisibleEditText().check(matches(withText(exIntegerFirstValue)));

        // Replace with Intent value:
        String exIntegerSecondValue = randomIntegerString();

        Intent stringIntent = new Intent();
        stringIntent.putExtra("value", Integer.parseInt(exIntegerSecondValue));

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);
        intending(allOf(
                hasAction("change.uw.android.BREATHCOUNT"),
                hasExtra("value", Integer.parseInt(exIntegerFirstValue))

        )).respondWith(exStringResult);

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        onView(withText("Launch")).perform(click());
        onView(withText(exIntegerSecondValue))
                .check(matches(isDisplayed()));

        Screengrab.screenshot("ex-integer2");

        openWidgetList();
        onView(withText("Ex integer widget")).perform(click());

        onVisibleEditText().check(matches(withText(exIntegerSecondValue)));

        onView(withText("Ex integer widget")).perform(swipeLeft());
    }

    public void testDecimalWidget() {
        String decimalString = randomDecimalString();
        onVisibleEditText().perform(replaceText(decimalString));

        Screengrab.screenshot("decimal1");

        openWidgetList();
        onView(withText("Decimal widget")).perform(click());

        onVisibleEditText().check(matches(withText(decimalString)));

        onView(withText("Decimal widget")).perform(swipeLeft());
    }

    public void testExDecimalWidget() {
        // Manually input the value:
        String exDecimalFirstValue = randomDecimalString();

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

        onView(withText("Launch")).perform(click());
        onVisibleEditText().perform(replaceText(exDecimalFirstValue));

        Screengrab.screenshot("ex-decimal");

        openWidgetList();
        onView(withText("Ex decimal widget")).perform(click());

        onVisibleEditText().check(matches(withText(exDecimalFirstValue)));

        // Replace with Intent value:
        String exDecimalSecondValue = randomDecimalString();

        Intent stringIntent = new Intent();
        stringIntent.putExtra("value", Double.parseDouble(exDecimalSecondValue));

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);
        intending(allOf(
                hasAction("change.uw.android.BREATHCOUNT"),
                hasExtra("value", Double.parseDouble(exDecimalFirstValue))

        )).respondWith(exStringResult);

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        onView(withText("Launch")).perform(click());
        onView(withText(exDecimalSecondValue))
                .check(matches(isDisplayed()));

        Screengrab.screenshot("ex-decimal2");

        openWidgetList();
        onView(withText("Ex decimal widget")).perform(click());

        onVisibleEditText().check(matches(withText(exDecimalSecondValue)));

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

        int randomValue = randomInt() % 9;
        onVisibleSeekBar().perform(setProgress(randomValue));

        Screengrab.screenshot("range-integer");

        openWidgetList();
        onView(withText("Range integer widget")).perform(click());

        onVisibleSeekBar().check(matches(withProgress(randomValue)));

        onView(withText("Range integer widget")).perform(swipeLeft());

    }

    public void testRangeVerticalAppearance() {

        int randomValue = randomInt() % 9;
        onVisibleSeekBar().perform(setProgress(randomValue));

        Screengrab.screenshot("range-integer-vertical");

        openWidgetList();
        onView(withText("Range vertical integer widget")).perform(click());

        onVisibleSeekBar().check(matches(withProgress(randomValue)));

        onView(withText("Range vertical integer widget")).perform(swipeLeft());

    }

    public void testRangeDecimalWidget() {

        int randomValue = randomInt() % 8;
        onVisibleSeekBar().perform(setProgress(randomValue));

        Screengrab.screenshot("range-decimal");

        openWidgetList();
        onView(withText("Range decimal widget")).perform(click());

        onVisibleSeekBar().check(matches(withProgress(randomValue)));

        onView(withText("Range decimal widget")).perform(swipeLeft());

    }

    public void testRangePickerIntegerWidget() {

        int randomValue = randomInt() % 8;
        onView(withText("Select value")).perform(click());

        onVisibleNumberPickerDialog().perform(setNumberPickerValue(randomValue));
        onView(withText("OK")).perform(click());

        Screengrab.screenshot("Range-picker-integer-widget");

        openWidgetList();
        onView(withText("Range picker integer widget")).perform(click());

        onView(withText("Edit value")).perform(click());
        onVisibleCustomEditText().check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());

        onView(withText("Range picker integer widget")).perform(swipeLeft());

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

    public void testWebViewImageWidget() {

        Screengrab.screenshot("web-view");

        onView(withText("Web view image widget")).perform(swipeLeft());
    }

    public void testAlignImageWidget() {

        Screengrab.screenshot("align-image");

        onView(withText("Align image widget")).perform(swipeLeft());
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

    public void testOSMIntegrationBuildingType() {

        Screengrab.screenshot("osm-build");

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

    public void testMultiSelectWidget() {

        Screengrab.screenshot("multi-select");

        onView(withText("Multi select widget")).perform(swipeLeft());
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

    public void testTriggerWidget(boolean check) {

        if (check) {
            onVisibleCheckBox().perform(click());
        }

        // captures screenshot of trigger widget
        Screengrab.screenshot("trigger-widget");

        openWidgetList();
        onView(withText("Trigger widget")).perform(click());

        onVisibleCheckBox().check(matches(check ? isChecked() : isNotChecked()));

        if (check) {
            onView(withText("Trigger widget")).perform(swipeLeft());
        }
    }

    public void testSubmission() {

    }
    //endregion

    //region Helper methods.
    private static String formPath() {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + ALL_WIDGETS_FORM;
    }

    public static Matcher<View> withProgress(final int expectedProgress) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("expected: ");
                description.appendText(String.valueOf(expectedProgress));
            }

            @Override
            public boolean matchesSafely(SeekBar seekBar) {
                return seekBar.getProgress() == expectedProgress;
            }
        };
    }

    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }

            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    public static ViewAction setNumberPickerValue(final int value) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker numberPickerDialog = (NumberPicker) view;
                numberPickerDialog.setValue(value);
            }

            @Override
            public String getDescription() {
                return "Set a value on a Number Picker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    private ViewInteraction onVisibleSeekBar() {
        return onView(withId(R.id.seek_bar));
    }

    private ViewInteraction onVisibleEditText() {
        return onView(withClassName(endsWith("EditText")));
    }

    private ViewInteraction onVisibleCustomEditText() {
        return onView(withClassName(endsWith("CustomEditText")));
    }

    private ViewInteraction onVisibleCheckBox() {
        return onView(withClassName(endsWith("CheckBox")));
    }

    private ViewInteraction onVisibleNumberPickerDialog() {
        return onView(withClassName(endsWith("NumberPicker")));
    }

    // private void openWidget(String name) {
    //     openWidgetList();
    //     onView(withText(name)).perform(click());
    // }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    // private void saveForm() {
    //    onView(withId(R.id.menu_save)).perform(click());
    // }

    private String randomString() {
        return RandomString.make();
    }

    private int randomInt() {
        return Math.abs(random.nextInt());
    }

    private String randomIntegerString() {
        String s = Integer.toString(randomInt());
        while (s.length() > 9) {
            s = s.substring(1);
        }

        // Make sure the result is a valid Integer String:
        return Integer.toString(Integer.parseInt(s));
    }

    private double randomDecimal() {
        return random.nextDouble();
    }

    private String randomDecimalString() {
        DecimalFormat decimalFormat = new DecimalFormat("####.#####");
        return decimalFormat.format(randomDecimal());
    }

    //    private ActivityResult cancelledResult() {
    //        return new ActivityResult(RESULT_CANCELED, null);
    //    }
    //
    //    private ActivityResult okResult(@Nullable Intent data) {
    //        return new ActivityResult(RESULT_OK, data);
    //    }

    private String randomIntegerSeparator() {
        int number = 123456;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(number);
     }

    //endregion

    //region Custom TestRule.
    private class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {

        FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            intent.putExtra(EXTRA_TESTING_PATH, formPath());

            return intent;
        }
    }
    //endregion
}
