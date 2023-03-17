package org.odk.collect.android.formmanagement.matchexactly

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.external.FormsContract
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.FormSourceException

class SyncStatusAppState(private val appState: AppState, private val context: Context) {

    fun isSyncing(projectId: String): LiveData<Boolean> {
        return getSyncingLiveData(projectId)
    }

    fun getSyncError(projectId: String): LiveData<FormSourceException?> {
        return getSyncErrorLiveData(projectId)
    }

    fun startSync(projectId: String) {
        getSyncingLiveData(projectId).postValue(true)
    }

    fun finishSync(projectId: String, exception: FormSourceException?) {
        getSyncErrorLiveData(projectId).postValue(exception)
        getSyncingLiveData(projectId).postValue(false)
        context.contentResolver.notifyChange(FormsContract.getUri(projectId), null)
    }

    private fun getSyncingLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_SYNCING:$projectId", MutableLiveData(false))

    private fun getSyncErrorLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_ERROR:$projectId", MutableLiveData<FormSourceException>(null))

    fun clear(projectId: String) {
        getSyncingLiveData(projectId).value = false
        getSyncErrorLiveData(projectId).value = null
    }

    companion object {
        const val KEY_PREFIX_SYNCING = "syncStatusSyncing"
        const val KEY_PREFIX_ERROR = "syncStatusError"
    }
}
