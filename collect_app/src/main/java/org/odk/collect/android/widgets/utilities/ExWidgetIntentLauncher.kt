package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.Intent.ACTION_SENDTO
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ExternalAppIntentProvider
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.androidshared.utils.IntentLauncher
import java.lang.Error
import java.lang.Exception

object ExWidgetIntentLauncherImpl : ExWidgetIntentLauncher {
    override fun launchForFileWidget(
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
                        showLongToast(activity, getErrorMessage(formEntryPrompt, activity))
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

    override fun launchForStringWidget(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt,
        onError: (String) -> Unit
    ) {
        try {
            val intent = externalAppIntentProvider.getIntentToRunExternalApp(formEntryPrompt)
            if (ACTION_SENDTO == intent.action) {
                intentLauncher.launch(
                    activity, intent
                ) {
                    try {
                        val intentWithoutDefaultCategory =
                            externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                                formEntryPrompt,
                                activity.packageManager
                            )
                        intentLauncher.launch(
                            activity, intentWithoutDefaultCategory
                        ) {
                            onError(getErrorMessage(formEntryPrompt, activity))
                        }
                    } catch (e: Exception) {
                        onError(e.message!!)
                    } catch (e: Error) {
                        onError(e.message!!)
                    }
                }
            } else {
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
                            onError(getErrorMessage(formEntryPrompt, activity))
                        }
                    } catch (e: Exception) {
                        onError(e.message!!)
                    } catch (e: Error) {
                        onError(e.message!!)
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

interface ExWidgetIntentLauncher {
    fun launchForFileWidget(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt
    )

    fun launchForStringWidget(
        intentLauncher: IntentLauncher,
        activity: Activity,
        requestCode: Int,
        externalAppIntentProvider: ExternalAppIntentProvider,
        formEntryPrompt: FormEntryPrompt,
        onError: (String) -> Unit
    )
}
