package org.odk.collect.maps.layers

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.system.getFileName
import org.odk.collect.androidshared.system.toFile
import org.odk.collect.async.Scheduler
import org.odk.collect.shared.TempFiles
import java.io.File
import java.util.ArrayList

class OfflineMapLayersImporterViewModel(
    private val scheduler: Scheduler,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _data = MutableLiveData<List<ReferenceLayer>>()
    val data: LiveData<List<ReferenceLayer>> = _data

    private lateinit var tempLayersDir: File

    fun init(uris: ArrayList<String>?) {
        scheduler.immediate(
            background = {
                tempLayersDir = TempFiles.createTempDir().also {
                    it.deleteOnExit()
                }
                val layers = mutableListOf<ReferenceLayer>()
                uris?.forEach { uriString ->
                    val uri = Uri.parse(uriString)
                    uri.getFileName(contentResolver)?.let { fileName ->
                        if (fileName.endsWith(MbtilesFile.FILE_EXTENSION)) {
                            val layerFile = File(tempLayersDir, fileName).also { file ->
                                uri.toFile(contentResolver, file)
                            }
                            layers.add(ReferenceLayer(layerFile.absolutePath, layerFile, MbtilesFile.readName(layerFile) ?: layerFile.name))
                        }
                    }
                }
                _data.postValue(layers)
            },
            foreground = { }
        )
    }

    fun addLayers(layersDir: String): LiveData<Boolean> {
        val isLoading = MutableLiveData(true)
        scheduler.immediate(
            background = {
                val destDir = File(layersDir)
                tempLayersDir.listFiles()?.forEach {
                    it.copyTo(File(destDir, it.name), true)
                }
                tempLayersDir.delete()

                isLoading.postValue(false)
            },
            foreground = { }
        )
        return isLoading
    }
}
