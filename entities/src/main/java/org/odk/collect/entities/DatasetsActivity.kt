package org.odk.collect.entities

import android.content.Intent
import android.os.Bundle
import org.odk.collect.entities.EntitiesActivity.Companion.EXTRA_DATASET
import org.odk.collect.entities.databinding.ListItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding
import org.odk.collect.shared.injection.ObjectProviderHost
import org.odk.collect.strings.localization.LocalizedActivity

class DatasetsActivity : LocalizedActivity() {

    private val entitiesRepository by lazy {
        (application as ObjectProviderHost)
            .getObjectProvider()
            .provide(EntitiesRepository::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.entities_title)
        setSupportActionBar(binding.appBarLayout.toolbar)

        entitiesRepository.getDatasets().forEach { dataset ->
            val item = ListItemLayoutBinding.inflate(layoutInflater)
            item.content.text = dataset

            item.root.setOnClickListener {
                val intent = Intent(this, EntitiesActivity::class.java).also {
                    it.putExtra(EXTRA_DATASET, dataset)
                }

                startActivity(intent)
            }

            binding.list.addView(item.root)
        }
    }
}
