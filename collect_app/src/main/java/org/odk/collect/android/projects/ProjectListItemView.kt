package org.odk.collect.android.projects

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.Project
import org.odk.collect.shared.Settings
import java.net.URL

class ProjectListItemView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.project_list_item, this)
    }

    fun setupView(project: Project, generalSettings: Settings) {
        findViewById<ProjectIconView>(R.id.project_icon).project = project
        findViewById<TextView>(R.id.project_name).text = project.name
        findViewById<TextView>(R.id.project_subtext).text = getSubtext(generalSettings)
    }

    private fun getSubtext(generalSettings: Settings): String {
        val username = if (generalSettings.getString(GeneralKeys.KEY_PROTOCOL).equals(Collect.getInstance().getString(R.string.protocol_google_sheets))) {
            generalSettings.getString(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT) ?: ""
        } else {
            generalSettings.getString(GeneralKeys.KEY_USERNAME) ?: ""
        }

        val connectedTo = if (generalSettings.getString(GeneralKeys.KEY_PROTOCOL).equals(Collect.getInstance().getString(R.string.protocol_google_sheets))) {
            "Google Drive"
        } else {
            val url = generalSettings.getString(GeneralKeys.KEY_SERVER_URL) ?: ""
            try {
                URL(url).host
            } catch (e: Exception) {
                ""
            }
        }

        return if (username.isNotBlank()) {
            "$username / $connectedTo"
        } else {
            connectedTo
        }
    }
}
