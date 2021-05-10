package org.odk.collect.android.provider

import android.net.Uri

object ProjectsProviderAPI {
    const val AUTHORITY = "org.odk.collect.android.provider.odk.projects"

    val CONTENT_URI = Uri.parse("content://$AUTHORITY/projects")

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.project"
}
