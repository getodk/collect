package org.odk.collect.android.formentry.savepoint

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.Form
import java.io.File

object SavePointUtils {
    @JvmStatic
    fun getInstancePathIfSavePointExists(form: Form): String? {
        val filePrefix = form.formFilePath.substringAfterLast('/').substringBeforeLast('.') + "_"
        val fileSuffix = ".xml.save"

        val cacheDir = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE))

        val savePointFile = cacheDir.listFiles { pathname ->
            pathname.name.startsWith(filePrefix) && pathname.name.endsWith(fileSuffix)
        }?.firstOrNull()

        if (savePointFile != null) {
            val instanceDirName = savePointFile.name.removeSuffix(fileSuffix)
            val instanceDir = File(
                StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES),
                instanceDirName
            )
            val instanceFile = File(instanceDir, "$instanceDirName.xml")

            if (instanceDir.exists() && instanceDir.isDirectory && !instanceFile.exists()) {
                return instanceFile.absolutePath
            }
        }
        return null
    }

    @JvmStatic
    fun getSavepointFile(instanceName: String): File {
        val cacheDir = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE))
        return File(cacheDir, "$instanceName.save")
    }
}
