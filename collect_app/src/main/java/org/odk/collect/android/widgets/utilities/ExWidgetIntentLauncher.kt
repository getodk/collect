package org.odk.collect.android.widgets.utilities

import android.app.Activity
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ExternalAppIntentProvider
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.androidshared.utils.IntentLauncher
import java.lang.Error
import java.lang.Exception

object ExWidgetIntentLauncherImpl : ExWidgetIntentLauncher {
    override fun launch(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt
    ) {
        try {
            val intent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)
            intentLauncher.launchForResult(
                activity, intent, requestCode
            ) {
                try {
                    val intentWithoutDefaultCategory =
                        externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                            formEntryPrompt,
                            activity.packageManager
                        )
                    intentLauncher.launchForResult(
                        activity, intentWithoutDefaultCategory, requestCode
                    ) {
                        val errorString: String
                        val v: String? =
                            formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")
                        errorString = v ?: activity.getString(R.string.no_app)
                        showLongToast(activity, errorString)
                    }
                } catch (e: Exception) {
                    showLongToast(activity, e.message!!)
                } catch (e: Error) {
                    showLongToast(activity, e.message!!)
                }
            }
        } catch (e: Exception) {
            showLongToast(activity, e.message!!)
        } catch (e: Error) {
            showLongToast(activity, e.message!!)
        }
    }
}

interface ExWidgetIntentLauncher {
    fun launch(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt
    )
}
