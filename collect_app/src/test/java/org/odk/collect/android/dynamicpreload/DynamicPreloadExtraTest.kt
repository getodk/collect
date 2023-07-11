package org.odk.collect.android.dynamicpreload

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.util.externalizable.ExtUtil
import org.junit.Test

class DynamicPreloadExtraTest {

    @Test
    fun `can be externalized`() {
        val extra = DynamicPreloadExtra(true)

        val external = ExtUtil.serialize(extra)
        val deserialized =
            ExtUtil.deserialize(external, DynamicPreloadExtra::class.java) as DynamicPreloadExtra

        assertThat(deserialized.usesDynamicPreload, equalTo(true))
    }
}
