package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ExternalAppIntentProvider
import org.odk.collect.androidshared.system.IntentLauncher
import java.lang.Error
import java.lang.Exception

object StringRequesterImpl : StringRequester {
    override fun launch(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt,
        onError: (String) -> Unit
    ) {
        try {
            val intent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)
            val intentWithoutDefaultCategory =
                externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                    formEntryPrompt,
                    activity.packageManager
                )

            // ACTION_SENDTO used for sending text messages or emails doesn't require any results
            if (Intent.ACTION_SENDTO == intent.action) {
                intentLauncher.launch(activity, intent) {
                    intentLauncher.launch(
                        activity, intentWithoutDefaultCategory
                    ) {
                        onError(getErrorMessage(formEntryPrompt, activity))
                    }
                }
            } else {
                intentLauncher.launchForResult(activity, intent, requestCode) {
                    intentLauncher.launchForResult(
                        activity, intentWithoutDefaultCategory, requestCode
                    ) {
                        onError(getErrorMessage(formEntryPrompt, activity))
                    }
                }
            }
        } catch (e: Exception) {
            onError(e.message!!)
        } catch (e: Error) {
            onError(e.message!!)
        }
    }

    private fun getErrorMessage(formEntryPrompt: FormEntryPrompt, activity: Activity): String {
        val customErrorMessage = formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")
        return customErrorMessage ?: activity.getString(R.string.no_app)
    }
}

interface StringRequester {
    fun launch(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt,
        onError: (String) -> Unit
    )
}
