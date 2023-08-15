package org.odk.collect.testshared

import android.app.Activity
import android.content.Intent
import org.junit.rules.ExternalResource
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

class ActivityControllerRule : ExternalResource() {

    private val controllers = mutableListOf<ActivityController<*>>()

    override fun after() {
        controllers.forEach { it.close() }
        controllers.clear()
    }

    fun <A : Activity> build(activityClass: Class<A>, intent: Intent): ActivityController<A> {
        return Robolectric.buildActivity(activityClass, intent).also {
            controllers.add(it)
        }
    }

    fun <A : Activity> add(supplier: () -> ActivityController<A>): ActivityController<A> {
        return supplier().also {
            controllers.add(it)
        }
    }
}
