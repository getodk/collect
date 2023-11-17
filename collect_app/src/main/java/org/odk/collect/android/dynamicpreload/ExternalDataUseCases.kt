package org.odk.collect.android.dynamicpreload

import android.content.res.Resources
import org.javarosa.core.model.FormDef
import java.io.File
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

object ExternalDataUseCases {

    @JvmStatic
    fun create(
        form: FormDef,
        mediaDir: File,
        isCancelled: Supplier<Boolean>,
        progressReporter: Consumer<Function<Resources, String>>
    ) {
        if (form.extras.get(DynamicPreloadExtra::class.java)?.usesDynamicPreload == false) {
            return
        }

        val csvFiles = mediaDir.listFiles { file ->
            val lowerCaseName = file.name.lowercase()
            lowerCaseName.endsWith(".csv") && !lowerCaseName.equals(
                "itemsets.csv",
                ignoreCase = true
            )
        }

        val externalDataMap: MutableMap<String, File> = HashMap()

        if (csvFiles != null) {
            for (csvFile in csvFiles) {
                val dataSetName = csvFile.name.substring(
                    0,
                    csvFile.name.lastIndexOf(".")
                )
                externalDataMap[dataSetName] = csvFile
            }
            if (externalDataMap.isNotEmpty()) {
                progressReporter.accept { resources ->
                    resources.getString(org.odk.collect.strings.R.string.survey_loading_reading_csv_message)
                }
                val externalDataReader: ExternalDataReader =
                    ExternalDataReaderImpl(isCancelled, progressReporter)
                externalDataReader.doImport(externalDataMap)
            }
        }
    }
}
