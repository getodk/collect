package org.odk.collect.testshared

import android.app.Activity
import android.net.Uri
import org.odk.collect.webpage.WebPageService

class MockWebPageService : WebPageService {

    private val _openedPages = mutableListOf<Uri>()
    val openedPages: List<Uri> = _openedPages

    override fun openWebPage(activity: Activity, uri: Uri) {
        _openedPages += uri
    }
}
