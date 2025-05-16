package org.odk.collect.android.formmanagement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.InstancesContract
import kotlin.reflect.KClass

object FormFillingIntentFactory {
    fun newFormIntent(
        context: Context,
        uri: Uri?,
        clazz: KClass<out Activity> = FormUriActivity::class
    ): Intent {
        return Intent(context, clazz.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = uri
        }
    }

    @JvmStatic
    @JvmOverloads
    fun editDraftFormIntent(
        context: Context,
        uri: Uri?,
        clazz: KClass<out Activity> = FormUriActivity::class
    ): Intent {
        return Intent(context, clazz.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = uri
        }
    }

    @JvmStatic
    @JvmOverloads
    fun editDraftFormIntent(
        context: Context,
        projectId: String,
        instanceId: Long,
        clazz: KClass<out Activity> = FormUriActivity::class
    ): Intent {
        return Intent(context, clazz.java).also {
            it.action = Intent.ACTION_EDIT
            it.data = InstancesContract.getUri(projectId, instanceId)
        }
    }
}
