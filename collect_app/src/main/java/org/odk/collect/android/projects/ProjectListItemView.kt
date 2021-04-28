package org.odk.collect.android.projects

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.odk.collect.android.R
import org.odk.collect.projects.Project
import kotlin.properties.Delegates

class ProjectListItemView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.project_list_item, this)
    }

    var project: Project? by Delegates.observable(null) { _, _, new ->
        if (new != null) {
            findViewById<ProjectIconView>(R.id.project_icon).project = new
            findViewById<TextView>(R.id.project_name).text = new.name
        }
    }
}
