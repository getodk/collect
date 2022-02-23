package org.odk.collect.geo

import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class SelectItemFromMapTest {

    @Test
    fun `parseResult returns null when result is cancelled`() {
        assertThat(TestContract().parseResult(RESULT_CANCELED, Intent()), equalTo(null))
    }
}

private class TestContract : SelectItemFromMap<Any>() {
    override fun createIntent(context: Context, input: Any?): Intent {
        TODO("Not yet implemented")
    }
}
