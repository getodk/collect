package org.odk.collect.android.projects

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.projects.Project

class ProjectListItemView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.project_list_item, this)
    }

    fun setupView(project: Project?, username: String, url: String) {
        if (project != null) {
            findViewById<ProjectIconView>(R.id.project_icon).project = project
            findViewById<TextView>(R.id.project_name).text = project.name
            findViewById<TextView>(R.id.project_subtext).text = if (username.isNotBlank()) {
                "$username / $url"
            } else {
                url
            }
        }
    }
}
