package org.odk.collect.webpage

import android.app.Activity
import android.net.Uri

interface WebPageService {
    fun openWebPage(activity: Activity, uri: Uri)
}
