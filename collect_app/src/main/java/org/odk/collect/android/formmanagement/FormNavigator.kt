package org.odk.collect.android.formmanagement

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.InstancesContract

object FormNavigator {
    fun newInstanceIntent(context: Context, uri: Uri?): Intent {
        return Intent(context, FormUriActivity::class.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = uri
        }
    }

    fun editInstanceIntent(
        context: Context,
        projectId: String,
        instanceId: Long
    ): Intent {
        return Intent(context, FormUriActivity::class.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = InstancesContract.getUri(projectId, instanceId)
        }
    }
}
