package org.odk.collect.testshared

import org.joda.time.DateTimeZone
import java.util.TimeZone

object TimeZoneSetter {
    /**
     * Always update both java.util.TimeZone and org.joda.time.DateTimeZone to avoid weird bugs in
     * tests that depend on time zones.
     */
    @JvmStatic
    fun setTimezone(timezone: TimeZone) {
        TimeZone.setDefault(timezone)
        DateTimeZone.setDefault(DateTimeZone.forID(timezone.id))
    }
}
