package org.odk.collect.android.widgets.datetime.pickers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.joda.time.LocalDateTime
import org.joda.time.chrono.BuddhistChronology
import org.odk.collect.android.R
import org.odk.collect.android.widgets.datetime.DateTimeUtils

class BuddhistDatePickerDialog : CustomDatePickerDialog() {
    private lateinit var monthsArray: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        monthsArray = resources.getStringArray(R.array.buddhist_months)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
        setUpMonthPicker(buddhistDate.monthOfYear, monthsArray)
        setUpYearPicker(buddhistDate.year, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR)
    }

    private fun setUpValues() {
        setUpDatePicker()
        updateGregorianDateLabel()
    }

    private fun getCurrentBuddhistDate(): LocalDateTime {
        var buddhistDay = day
        val buddhistMonth = monthsArray.indexOf(month)
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
    }
}
