package org.odk.collect.errors

import android.app.NotificationManager
import android.content.Context
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
        const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        title = getLocalizedString(org.odk.collect.strings.R.string.errors)
        val toolbar = findViewById<View>(org.odk.collect.androidshared.R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        findViewById<Toolbar>(org.odk.collect.androidshared.R.id.toolbar).setNavigationOnClickListener { finish() }

        val failures = intent.getSerializableExtra(EXTRA_ERRORS) as? List<ErrorItem>
        if (failures != null) {
            findViewById<RecyclerView>(R.id.errors).apply {
                adapter = ErrorAdapter(failures)
                layoutManager = LinearLayoutManager(context)
            }
        } else {
            finish()
        }

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    }
}
