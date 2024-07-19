package org.odk.collect.android.entities.support

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.entities.storage.Entity

class EntitySameAsMatcher(private val expected: Entity) : TypeSafeMatcher<Entity>() {
    override fun describeTo(description: Description) {
        description.appendText("is the same as $expected")
    }

    override fun matchesSafely(item: Entity?): Boolean {
        return if (item != null) {
            expected.sameAs(item)
        } else {
            false
        }
    }

    companion object {
        fun sameEntityAs(entity: Entity): EntitySameAsMatcher {
            return EntitySameAsMatcher(entity)
        }
    }
}
