package org.odk.collect.android.geo

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.odk.collect.android.databinding.ActivityReferenceLayerImportBinding
import java.io.InputStream

class ReferenceLayerImportActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_URI = "org.odk.collect.android.EXTRA_FILE_URI"
        const val EXTRA_CURRENT_PROJECT = "org.odk.collect.android.EXTRA_CURRENT_PROJECT"
    }

    private lateinit var binding: ActivityReferenceLayerImportBinding
    private var fileUri: Uri? = null
    private var currentProject: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferenceLayerImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the file URI and current project from the intent
        fileUri = intent.getStringExtra(EXTRA_FILE_URI)?.let { Uri.parse(it) }
        currentProject = intent.getStringExtra(EXTRA_CURRENT_PROJECT)

        // Show the file URI and current project in a toast (for debugging)
        Toast.makeText(this, "URI: $fileUri, Project: $currentProject", Toast.LENGTH_LONG).show()

        // Populate the ListView with the file name (if available)
        fileUri?.let {
            val fileName = it.lastPathSegment ?: "Unknown"
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, listOf(fileName))
            binding.layersList.adapter = adapter
        }

        // Handle the "Add layer" button click
        binding.addLayerButton.setOnClickListener {
            if (fileUri != null && currentProject != null) {
                importLayer(fileUri!!, currentProject!!)
            } else {
                Toast.makeText(this, "No file or project provided.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importLayer(uri: Uri, currentProject: String) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // TODO: Process the inputStream as needed for your application

                // Make sure to close the inputStream when done
                inputStream.close()
                Toast.makeText(this, "Import successful. You can select the layer from the layer switcher.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Unable to open file.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An error occurred during import. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
