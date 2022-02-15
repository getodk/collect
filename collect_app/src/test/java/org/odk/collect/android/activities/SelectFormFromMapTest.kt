package org.odk.collect.android.activities

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class SelectFormFromMapTest {

    @Test
    fun `parseResult returns null when result is cancelled`() {
        assertThat(SelectFormFromMap().parseResult(RESULT_CANCELED, Intent()), equalTo(null))
    }
}
