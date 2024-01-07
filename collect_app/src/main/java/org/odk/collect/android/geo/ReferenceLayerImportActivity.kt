package org.odk.collect.android.geo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.odk.collect.android.databinding.ActivityReferenceLayerImportBinding
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FileUtils
import java.io.File


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

        // Set the Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(binding.toolbar)

        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title of the ActionBar
        supportActionBar?.title = "Add layers"

        // Retrieve the file URI and current project from the intent
        fileUri = intent.getStringExtra(EXTRA_FILE_URI)?.let { Uri.parse(it) }
        currentProject = intent.getStringExtra(EXTRA_CURRENT_PROJECT)

        // Show the file URI and current project in a toast (for debugging)
        Toast.makeText(this, "URI: $fileUri, Project: $currentProject", Toast.LENGTH_LONG).show()

        // Populate the ListView with the file name (if available)
        fileUri?.let {
            val fileName = FileUtils.getFileNameFromContentUri(contentResolver, fileUri)
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

    // Handle the back arrow click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun importLayer(uri: Uri, currentProject: String) {
        try {
            // Assuming FileUtils.getFileNameFromContentUri returns the file name as a String.
            val fileName = FileUtils.getFileNameFromContentUri(contentResolver, uri)

            if (fileName != null && fileName.isNotEmpty()) {
                // Create a destination file in your app's private storage
                val destFile = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS), fileName)

                // Assuming FileUtils.saveLayersFromUri copies the content from the URI to the destination file
                FileUtils.saveLayersFromUri(uri, destFile, this)

                Toast.makeText(this, "Import successful. You can select the layer from the layer switcher.", Toast.LENGTH_LONG).show()
                handleOperationAndReturnResult()

            } else {
                Toast.makeText(this, "Invalid file name.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An error occurred during import. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleOperationAndReturnResult() {
        val resultData = "some_result_data"

        val returnIntent = Intent()
        returnIntent.putExtra("result_key", resultData)

        setResult(RESULT_OK, returnIntent)

        finish()
    }
}
