package org.odk.collect.android.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ThemeUtils
import javax.inject.Inject

class FormUriActivity : Activity() {

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setTheme(ThemeUtils(this).appTheme)

        val formUri = intent.data
        val projectId = formUri!!.getQueryParameter("projectId")!!

        if (projectId == currentProjectProvider.getCurrentProject().uuid) {
            startActivity(
                Intent(this, FormEntryActivity::class.java).also {
                    it.data = formUri
                }
            )
        } else {
            AlertDialog.Builder(this)
                .setMessage(R.string.wrong_project_selected_for_form)
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .create()
                .show()
        }
    }
}
