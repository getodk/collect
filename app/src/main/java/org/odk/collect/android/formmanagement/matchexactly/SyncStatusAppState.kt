package org.odk.collect.android.formmanagement.matchexactly

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.external.FormsContract
import org.odk.collect.forms.FormSourceException
import javax.inject.Singleton

@Singleton
class SyncStatusAppState(private val context: Context) {

    private val syncing = mutableMapOf<String, MutableLiveData<Boolean>>()
    private val lastSyncFailure = mutableMapOf<String, MutableLiveData<FormSourceException?>>()

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
        syncing.getOrPut(projectId) { MutableLiveData(false) }

    private fun getSyncErrorLiveData(projectId: String) =
        lastSyncFailure.getOrPut(projectId) { MutableLiveData(null) }
}
