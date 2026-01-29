package org.odk.collect.testshared

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.odk.collect.androidshared.ui.CollectComposeThemeProvider

class DummyActivity : ComponentActivity(), CollectComposeThemeProvider {

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
