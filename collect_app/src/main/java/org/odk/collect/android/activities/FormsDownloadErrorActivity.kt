package org.odk.collect.android.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.adapters.FormsDownloadErrorAdapter
import org.odk.collect.android.logic.FormDownloadErrorItem
import org.odk.collect.android.utilities.TranslationHandler

class FormsDownloadErrorActivity : CollectAbstractActivity() {
    companion object {
        const val FAILURES = "FAILURES"
    }

    private lateinit var failures: List<FormDownloadErrorItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forms_download_error)
        initToolbar(TranslationHandler.getString(this, R.string.errors))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        failures = intent.getSerializableExtra(FAILURES) as List<FormDownloadErrorItem>
        findViewById<RecyclerView>(R.id.errors).apply {
            adapter = FormsDownloadErrorAdapter(failures)
            layoutManager = LinearLayoutManager(context)
        }
    }
}
