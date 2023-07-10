package org.odk.collect.android.dynamicpreload

import org.javarosa.core.model.FormDef
import org.odk.collect.android.application.Collect
import java.io.File
import java.util.function.Consumer
import java.util.function.Supplier

class ExternalDataCreator {

    fun create(
        form: FormDef,
        mediaDir: File,
        isCancelled: Supplier<Boolean>,
        progressReporter: Consumer<String>
    ) {
        if (!form.extras.get(DynamicPreloadExtra::class.java).usesDynamicPreload) {
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
            if (!externalDataMap.isEmpty()) {
                progressReporter.accept(
                    Collect.getInstance().getString(org.odk.collect.strings.R.string.survey_loading_reading_csv_message)
                )
                val externalDataReader: ExternalDataReader =
                    ExternalDataReaderImpl(isCancelled, progressReporter)
                externalDataReader.doImport(externalDataMap)
            }
        }
    }
}
