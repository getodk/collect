package org.odk.collect.servicetest

import android.app.Notification
import android.content.Intent
import org.robolectric.Shadows

/**
 * Wraps a [Notification] object and provides access to details exposed by its Robolectric "Shadow"
 * without needing to explicitly depend on Robolectric.
 */
class NotificationDetails internal constructor(private val notification: Notification) {

    val contentText: String
        get() = Shadows.shadowOf(notification).contentText.toString()

    val contentIntent: Intent
        get() = Shadows.shadowOf(notification.contentIntent).savedIntent
}
