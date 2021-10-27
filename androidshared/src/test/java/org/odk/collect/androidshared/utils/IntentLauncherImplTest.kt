package org.odk.collect.androidshared.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.system.IntentLauncherImpl
import java.lang.Exception

class IntentLauncherImplTest {
    private val context = mock<Context>()
    private val activity = mock<Activity>()
    private val activityResultLauncher = mock<ActivityResultLauncher<Intent>>()
    private val intent = mock<Intent>()
    private val onError = mock<() -> Unit>()
    private val intentLauncher = IntentLauncherImpl

    @Test
    fun `startActivity with given intent should be called on the context when calling IntentLauncher#launch(context, intent, onError)`() {
        intentLauncher.launch(context, intent, onError)
        verify(context).startActivity(intent)
        verifyNoMoreInteractions(onError)
    }

    @Test
    fun `onError should be called if any exception occurs when calling IntentLauncher#launch(context, intent, onError)`() {
        whenever(context.startActivity(intent)).then {
            throw Exception()
        }
        intentLauncher.launch(context, intent, onError)
        verify(onError).invoke()
    }

    @Test
    fun `onError should be called if any error occurs when calling IntentLauncher#launch(context, intent, onError)`() {
        whenever(context.startActivity(intent)).then {
            throw Error()
        }
        intentLauncher.launch(context, intent, onError)
        verify(onError).invoke()
    }

    @Test
    fun `startActivityForResult with given intent should be called on the context when calling IntentLauncher#launchForResult(context, intent, requestCode, onError)`() {
        intentLauncher.launchForResult(activity, intent, 1, onError)
        verify(activity).startActivityForResult(intent, 1)
        verifyNoMoreInteractions(onError)
    }

    @Test
    fun `onError should be called if any exception occurs when calling IntentLauncher#launchForResult(context, intent, requestCode, onError)`() {
        whenever(activity.startActivityForResult(intent, 1)).then {
            throw Exception()
        }
        intentLauncher.launchForResult(activity, intent, 1, onError)
        verify(onError).invoke()
    }

    @Test
    fun `onError should be called if any error occurs when calling IntentLauncher#launchForResult(context, intent, requestCode, onError)`() {
        whenever(activity.startActivityForResult(intent, 1)).then {
            throw Error()
        }
        intentLauncher.launchForResult(activity, intent, 1, onError)
        verify(onError).invoke()
    }

    @Test
    fun `startActivityForResult with given intent should be called on the context when calling IntentLauncher#launchForResult(resultLauncher, intent, onError)`() {
        intentLauncher.launchForResult(activityResultLauncher, intent, onError)
        verify(activityResultLauncher).launch(intent)
        verifyNoMoreInteractions(onError)
    }

    @Test
    fun `onError should not be called if no exception occurs when calling IntentLauncher#launchForResult(resultLauncher, intent, onError)`() {
        intentLauncher.launchForResult(activityResultLauncher, intent, onError)
        verifyNoMoreInteractions(onError)
    }

    @Test
    fun `onError should be called if any exception occurs when calling IntentLauncher#launchForResult(resultLauncher, intent, onError)`() {
        whenever(activityResultLauncher.launch(intent)).then {
            throw Exception()
        }
        intentLauncher.launchForResult(activityResultLauncher, intent, onError)
        verify(onError).invoke()
    }

    @Test
    fun `onError should be called if any error occurs when calling IntentLauncher#launchForResult(resultLauncher, intent, onError)`() {
        whenever(activityResultLauncher.launch(intent)).then {
            throw Error()
        }
        intentLauncher.launchForResult(activityResultLauncher, intent, onError)
        verify(onError).invoke()
    }
}
