package org.odk.collect.android.widgets.datetime.pickers

import org.joda.time.LocalDateTime
import org.joda.time.chrono.BuddhistChronology
import org.odk.collect.android.widgets.datetime.DateTimeUtils

class BuddhistDatePickerDialog : CustomDatePickerDialog() {
    override fun onResume() {
        super.onResume()
        setUpValues()
    }

    override fun updateDays() {
        val localDateTime = getCurrentBuddhistDate()
        setUpDayPicker(localDateTime.dayOfMonth, localDateTime.dayOfMonth().maximumValue)
    }

    override fun getOriginalDate(): LocalDateTime {
        return getCurrentBuddhistDate()
    }

    private fun setUpDatePicker() {
        val buddhistDate = DateTimeUtils
            .skipDaylightSavingGapIfExists(date)
            .toDateTime()
            .withChronology(BuddhistChronology.getInstance())
            .toLocalDateTime()

        setUpDayPicker(buddhistDate.dayOfMonth, buddhistDate.dayOfMonth().maximumValue)
        setUpMonthPicker(buddhistDate.monthOfYear, MONTHS)
        setUpYearPicker(buddhistDate.year, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR)
    }

    private fun setUpValues() {
        setUpDatePicker()
        updateGregorianDateLabel()
    }

    private fun getCurrentBuddhistDate(): LocalDateTime {
        var buddhistDay = day
        val buddhistMonth = MONTHS.indexOf(month)
        val buddhistYear = year

        val buddhistDate = LocalDateTime(
            buddhistYear,
            buddhistMonth + 1,
            1,
            0,
            0,
            0,
            0,
            BuddhistChronology.getInstance()
        )
        if (buddhistDay > buddhistDate.dayOfMonth().maximumValue) {
            buddhistDay = buddhistDate.dayOfMonth().maximumValue
        }

        return LocalDateTime(
            buddhistYear,
            buddhistMonth + 1,
            buddhistDay,
            0,
            0,
            0,
            0,
            BuddhistChronology.getInstance()
        )
    }

    companion object {
        const val MIN_SUPPORTED_YEAR = 2443 // 1900 in Gregorian calendar
        const val MAX_SUPPORTED_YEAR = 2643 // 2100 in Gregorian calendar

        @JvmField
        val MONTHS = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )
    }
}
