package org.odk.collect.android.dynamicpreload

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.junit.Test

class DynamicPreloadParseProcessorTest {

    private val processor = DynamicPreloadParseProcessor()

    /**
     * This is currently a "stub" implementation until we have a good way of determining whether
     * a form contains search/pulldata.
     */
    @Test
    fun `usesDynamicPreload is true`() {
        val formDef = FormDef()

        processor.processFormDef(formDef)
        assertThat(
            formDef.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload,
            equalTo(true)
        )
    }
}
