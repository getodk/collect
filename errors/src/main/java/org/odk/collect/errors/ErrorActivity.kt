package org.odk.collect.errors

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.strings.localization.LocalizedActivity
import org.odk.collect.strings.localization.getLocalizedString

class ErrorActivity : LocalizedActivity() {
    companion object {
        const val EXTRA_ERRORS = "ERRORS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        title = getLocalizedString(R.string.errors)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val failures = intent.getSerializableExtra(EXTRA_ERRORS) as? List<ErrorItem>
        if (failures != null) {
            findViewById<RecyclerView>(R.id.errors).apply {
                adapter = ErrorAdapter(failures)
                layoutManager = LinearLayoutManager(context)
            }
        } else {
            finish()
        }
    }
}
