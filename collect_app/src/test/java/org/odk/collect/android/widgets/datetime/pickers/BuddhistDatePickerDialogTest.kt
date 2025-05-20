package org.odk.collect.android.widgets.datetime.pickers

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.joda.time.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.widgets.datetime.DatePickerDetails
import org.odk.collect.android.widgets.datetime.DatePickerDetails.DatePickerMode
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.Assertions
import org.odk.collect.testshared.Interactions
import org.odk.collect.testshared.ViewActions

@RunWith(AndroidJUnit4::class)
class BuddhistDatePickerDialogTest {
    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `The dialog is cancelable`() {
        val scenario = launchFragment(2010, 5, 12)
        scenario.onFragment {
            assertThat(it.isCancelable, equalTo(true))
        }
    }

    @Test
    fun `The dialog shows correct date`() {
        launchFragment(2010, 5, 12, DatePickerMode.SPINNERS)
        Assertions.assertVisible(withText("12 พฤษภาคม 2553 (May 12, 2010)"), isDialog())

        updateDate(6, 0, 2447)
        Assertions.assertVisible(withText("6 มกราคม 2447 (Jan 06, 1904)"), isDialog())

        updateDate(13, 1, 2459)
        Assertions.assertVisible(withText("13 กุมภาพันธ์ 2459 (Feb 13, 1916)"), isDialog())

        updateDate(21, 2, 2467)
        Assertions.assertVisible(withText("21 มีนาคม 2467 (Mar 21, 1924)"), isDialog())

        updateDate(10, 3, 2479)
        Assertions.assertVisible(withText("10 เมษายน 2479 (Apr 10, 1936)"), isDialog())

        updateDate(18, 4, 2487)
        Assertions.assertVisible(withText("18 พฤษภาคม 2487 (May 18, 1944)"), isDialog())

        updateDate(27, 5, 2499)
        Assertions.assertVisible(withText("27 มิถุนายน 2499 (Jun 27, 1956)"), isDialog())

        updateDate(8, 6, 2507)
        Assertions.assertVisible(withText("8 กรกฎาคม 2507 (Jul 08, 1964)"), isDialog())

        updateDate(15, 7, 2519)
        Assertions.assertVisible(withText("15 สิงหาคม 2519 (Aug 15, 1976)"), isDialog())

        updateDate(23, 8, 2527)
        Assertions.assertVisible(withText("23 กันยายน 2527 (Sep 23, 1984)"), isDialog())

        updateDate(30, 9, 2539)
        Assertions.assertVisible(withText("30 ตุลาคม 2539 (Oct 30, 1996)"), isDialog())

        updateDate(5, 10, 2547)
        Assertions.assertVisible(withText("5 พฤศจิกายน 2547 (Nov 05, 2004)"), isDialog())

        updateDate(12, 11, 2559)
        Assertions.assertVisible(withText("12 ธันวาคม 2559 (Dec 12, 2016)"), isDialog())

        updateDate(7, 0, 2567)
        Assertions.assertVisible(withText("7 มกราคม 2567 (Jan 07, 2024)"), isDialog())

        updateDate(14, 1, 2579)
        Assertions.assertVisible(withText("14 กุมภาพันธ์ 2579 (Feb 14, 2036)"), isDialog())

        updateDate(22, 2, 2587)
        Assertions.assertVisible(withText("22 มีนาคม 2587 (Mar 22, 2044)"), isDialog())

        updateDate(11, 3, 2599)
        Assertions.assertVisible(withText("11 เมษายน 2599 (Apr 11, 2056)"), isDialog())

        updateDate(19, 4, 2607)
        Assertions.assertVisible(withText("19 พฤษภาคม 2607 (May 19, 2064)"), isDialog())

        updateDate(26, 5, 2619)
        Assertions.assertVisible(withText("26 มิถุนายน 2619 (Jun 26, 2076)"), isDialog())

        updateDate(9, 6, 2627)
        Assertions.assertVisible(withText("9 กรกฎาคม 2627 (Jul 09, 2084)"), isDialog())

        updateDate(16, 7, 2639)
        Assertions.assertVisible(withText("16 สิงหาคม 2639 (Aug 16, 2096)"), isDialog())
    }

    @Test
    fun `The dialog shows correct date for 'year' mode`() {
        launchFragment(2010, 5, 12, DatePickerMode.YEAR)
        Assertions.assertVisible(withText("2553 (2010)"), isDialog())

        updateDate(year = 2447)
        Assertions.assertVisible(withText("2447 (1904)"), isDialog())

        updateDate(year = 2453)
        Assertions.assertVisible(withText("2453 (1910)"), isDialog())

        updateDate(year = 2461)
        Assertions.assertVisible(withText("2461 (1918)"), isDialog())

        updateDate(year = 2468)
        Assertions.assertVisible(withText("2468 (1925)"), isDialog())

        updateDate(year = 2474)
        Assertions.assertVisible(withText("2474 (1931)"), isDialog())

        updateDate(year = 2483)
        Assertions.assertVisible(withText("2483 (1940)"), isDialog())

        updateDate(year = 2501)
        Assertions.assertVisible(withText("2501 (1958)"), isDialog())

        updateDate(year = 2509)
        Assertions.assertVisible(withText("2509 (1966)"), isDialog())

        updateDate(year = 2517)
        Assertions.assertVisible(withText("2517 (1974)"), isDialog())

        updateDate(year = 2525)
        Assertions.assertVisible(withText("2525 (1982)"), isDialog())

        updateDate(year = 2542)
        Assertions.assertVisible(withText("2542 (1999)"), isDialog())

        updateDate(year = 2551)
        Assertions.assertVisible(withText("2551 (2008)"), isDialog())

        updateDate(year = 2559)
        Assertions.assertVisible(withText("2559 (2016)"), isDialog())

        updateDate(year = 2567)
        Assertions.assertVisible(withText("2567 (2024)"), isDialog())

        updateDate(year = 2575)
        Assertions.assertVisible(withText("2575 (2032)"), isDialog())

        updateDate(year = 2583)
        Assertions.assertVisible(withText("2583 (2040)"), isDialog())

        updateDate(year = 2599)
        Assertions.assertVisible(withText("2599 (2056)"), isDialog())

        updateDate(year = 2608)
        Assertions.assertVisible(withText("2608 (2065)"), isDialog())

        updateDate(year = 2616)
        Assertions.assertVisible(withText("2616 (2073)"), isDialog())

        updateDate(year = 2624)
        Assertions.assertVisible(withText("2624 (2081)"), isDialog())

        updateDate(year = 2632)
        Assertions.assertVisible(withText("2632 (2089)"), isDialog())

        updateDate(year = 2638)
        Assertions.assertVisible(withText("2638 (2095)"), isDialog())
    }

    @Test
    fun `The dialog shows correct date for 'month-year' mode`() {
        launchFragment(2010, 5, 12, DatePickerMode.MONTH_YEAR)
        Assertions.assertVisible(withText("พฤษภาคม 2553 (May 2010)"), isDialog())

        updateDate(month = 0, year = 2448)
        Assertions.assertVisible(withText("มกราคม 2448 (Jan 1905)"), isDialog())

        updateDate(month = 1, year = 2454)
        Assertions.assertVisible(withText("กุมภาพันธ์ 2454 (Feb 1911)"), isDialog())

        updateDate(month = 2, year = 2464)
        Assertions.assertVisible(withText("มีนาคม 2464 (Mar 1921)"), isDialog())

        updateDate(month = 3, year = 2473)
        Assertions.assertVisible(withText("เมษายน 2473 (Apr 1930)"), isDialog())

        updateDate(month = 4, year = 2486)
        Assertions.assertVisible(withText("พฤษภาคม 2486 (May 1943)"), isDialog())

        updateDate(month = 5, year = 2495)
        Assertions.assertVisible(withText("มิถุนายน 2495 (Jun 1952)"), isDialog())

        updateDate(month = 6, year = 2508)
        Assertions.assertVisible(withText("กรกฎาคม 2508 (Jul 1965)"), isDialog())

        updateDate(month = 7, year = 2519)
        Assertions.assertVisible(withText("สิงหาคม 2519 (Aug 1976)"), isDialog())

        updateDate(month = 8, year = 2527)
        Assertions.assertVisible(withText("กันยายน 2527 (Sep 1984)"), isDialog())

        updateDate(month = 9, year = 2538)
        Assertions.assertVisible(withText("ตุลาคม 2538 (Oct 1995)"), isDialog())

        updateDate(month = 10, year = 2549)
        Assertions.assertVisible(withText("พฤศจิกายน 2549 (Nov 2006)"), isDialog())

        updateDate(month = 11, year = 2560)
        Assertions.assertVisible(withText("ธันวาคม 2560 (Dec 2017)"), isDialog())

        updateDate(month = 0, year = 2571)
        Assertions.assertVisible(withText("มกราคม 2571 (Jan 2028)"), isDialog())

        updateDate(month = 1, year = 2582)
        Assertions.assertVisible(withText("กุมภาพันธ์ 2582 (Feb 2039)"), isDialog())

        updateDate(month = 2, year = 2592)
        Assertions.assertVisible(withText("มีนาคม 2592 (Mar 2049)"), isDialog())

        updateDate(month = 2, year = 2595)
        Assertions.assertVisible(withText("มีนาคม 2595 (Mar 2052)"), isDialog())

        updateDate(month = 3, year = 2604)
        Assertions.assertVisible(withText("เมษายน 2604 (Apr 2061)"), isDialog())

        updateDate(month = 4, year = 2615)
        Assertions.assertVisible(withText("พฤษภาคม 2615 (May 2072)"), isDialog())

        updateDate(month = 5, year = 2626)
        Assertions.assertVisible(withText("มิถุนายน 2626 (Jun 2083)"), isDialog())

        updateDate(month = 6, year = 2637)
        Assertions.assertVisible(withText("กรกฎาคม 2637 (Jul 2094)"), isDialog())
    }

    @Test
    fun `recreating maintains the date`() {
        val scenario = launchFragment(2010, 5, 12)
        Assertions.assertVisible(withText("12 พฤษภาคม 2553 (May 12, 2010)"), isDialog())
        scenario.recreate()
        Assertions.assertVisible(withText("12 พฤษภาคม 2553 (May 12, 2010)"), isDialog())
    }

    @Test
    fun `clicking OK dismisses the dialog`() {
        val scenario = launchFragment(2010, 5, 12)
        scenario.onFragment {
            assertThat(it.dialog, notNullValue())
            Interactions.clickOn(withText(string.ok), isDialog())
            assertThat(it.dialog, nullValue())
        }
    }

    @Test
    fun `clicking CANCEL dismisses the dialog`() {
        val scenario = launchFragment(2010, 5, 12)
        scenario.onFragment {
            assertThat(it.dialog, notNullValue())
            Interactions.clickOn(withText(string.cancel), isDialog())
            assertThat(it.dialog, nullValue())
        }
    }

    private fun updateDate(day: Int? = null, month: Int? = null, year: Int? = null) {
        day?.let {
            onView(withId(R.id.day_picker)).inRoot(isDialog()).perform(ViewActions.scrollNumberPickerToValue(it))
        }
        month?.let {
            onView(withId(R.id.month_picker)).inRoot(isDialog()).perform(ViewActions.scrollNumberPickerToValue(it))
        }
        year?.let {
            onView(withId(R.id.year_picker)).inRoot(isDialog()).perform(ViewActions.scrollNumberPickerToValue(it))
        }
    }

    private fun launchFragment(
        year: Int,
        month: Int,
        day: Int,
        datePickerMode: DatePickerMode = DatePickerMode.SPINNERS
    ): FragmentScenario<BuddhistDatePickerDialog> {
        val args = Bundle().apply {
            putSerializable(
                DateTimeWidgetUtils.DATE,
                LocalDateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day)
            )
            putSerializable(
                DateTimeWidgetUtils.DATE_PICKER_DETAILS,
                DatePickerDetails(
                    DatePickerDetails.DatePickerType.BUDDHIST,
                    datePickerMode
                )
            )
        }

        return launcherRule.launch(BuddhistDatePickerDialog::class.java, args)
    }
}
