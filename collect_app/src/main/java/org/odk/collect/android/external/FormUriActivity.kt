package org.odk.collect.android.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.odk.collect.android.activities.FormEntryActivity

class FormUriActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, FormEntryActivity::class.java).also {
            it.data = intent.data
        })
    }
}
