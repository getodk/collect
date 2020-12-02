package org.odk.collect.strings.format

import java.util.Locale

private const val ONE_HOUR = 3600000
private const val ONE_MINUTE = 60000
private const val ONE_SECOND = 1000

fun formatLength(milliseconds: Long): String {
    val hours = milliseconds / ONE_HOUR
    val minutes = milliseconds % ONE_HOUR / ONE_MINUTE
    val seconds = milliseconds % ONE_MINUTE / ONE_SECOND
    return if (milliseconds < ONE_HOUR) {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
