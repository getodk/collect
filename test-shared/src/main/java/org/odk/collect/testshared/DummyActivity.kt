package org.odk.collect.testshared

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class DummyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).also {
                it.text = TEXT
            }
        )
    }

    companion object {
        const val TEXT = "I AM DUMMY"
    }
}
