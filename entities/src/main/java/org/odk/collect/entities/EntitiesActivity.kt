package org.odk.collect.entities

import android.os.Bundle
import org.odk.collect.entities.databinding.ListItemLayoutBinding
import org.odk.collect.entities.databinding.ListLayoutBinding
import org.odk.collect.shared.injection.ObjectProviderHost
import org.odk.collect.strings.localization.LocalizedActivity

class EntitiesActivity : LocalizedActivity() {

    private val entitiesRepository by lazy {
        (application as ObjectProviderHost)
            .getObjectProvider()
            .provide(EntitiesRepository::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ListLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataset = intent.getStringExtra(EXTRA_DATASET)!!

        title = dataset
        setSupportActionBar(binding.appBarLayout.toolbar)

        entitiesRepository.getEntities(dataset).forEach { entity ->
            val item = ListItemLayoutBinding.inflate(layoutInflater)

            val firstField = entity.fields[0]
            item.content.text =
                getString(R.string.entity_summary, firstField.first, firstField.second)

            binding.list.addView(item.root)
        }
    }

    companion object {
        const val EXTRA_DATASET = "dataset"
    }
}
