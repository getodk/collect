package org.odk.collect.android.geo


import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import org.odk.collect.android.databinding.ActivityReferenceLayerImportBinding

class ReferenceLayerImportActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_NAMES = "org.odk.collect.android.EXTRA_FILE_NAMES"
        const val EXTRA_CURRENT_PROJECT = "org.odk.collect.android.EXTRA_CURRENT_PROJECT"
    }

    private lateinit var binding: ActivityReferenceLayerImportBinding
    private lateinit var fileNames: ArrayList<String>
    private var currentProject: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferenceLayerImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the file names and current project from the intent
        fileNames = intent.getStringArrayListExtra("fileNames") ?: arrayListOf()
        currentProject = intent.getStringExtra("currentProject")

        // Populate the ListView with the file names
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, fileNames)
        binding.layersList.adapter = adapter

        // Handle the "Add layer" button click
        binding.addLayerButton.setOnClickListener {
            val selectedFiles = getSelectedFiles()
            importLayers(selectedFiles, currentProject!!)
        }
    }


    private fun getSelectedFiles(): List<String> {
        val listView = binding.layersList
        val checkedItems = listView.checkedItemPositions
        val selectedFiles = mutableListOf<String>()

        for (i in 0 until listView.count) {
            if (checkedItems[i]) {
                selectedFiles.add(fileNames[i])
            }
        }
        return selectedFiles
    }

    private fun importLayers(selectedFiles: List<String>, currentProject: String) {
        // Implement your logic to import layers here
        // If `importToAllProjects` is true, import to all projects, otherwise to current project
        // You'll likely need access to file storage and project management systems in your app
    }
}
