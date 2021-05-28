package org.odk.collect.android.projects

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.android.R
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
        val username = generalSettings.getString(GeneralKeys.KEY_USERNAME) ?: ""
        var url = generalSettings.getString(GeneralKeys.KEY_SERVER_URL) ?: ""

        try {
            url = URL(url).host
        } catch (e: Exception) {
        }

        return if (username.isNotBlank()) {
            "$username / $url"
        } else {
            url
        }
    }
}
