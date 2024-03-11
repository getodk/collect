package org.odk.collect.entities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class EntityBrowserActivity : LocalizedActivity() {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var entitiesRepository: EntitiesRepository

    val viewModelFactory = viewModelFactory {
        addInitializer(EntitiesViewModel::class) {
            EntitiesViewModel(scheduler, entitiesRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(DatasetsFragment::class) { DatasetsFragment(viewModelFactory, ::getToolbar) }
            .forClass(EntitiesFragment::class) { EntitiesFragment(viewModelFactory) }
            .build()

        super.onCreate(savedInstanceState)
        (applicationContext as EntitiesDependencyComponentProvider)
            .entitiesDependencyComponent.inject(this)

        setContentView(R.layout.entities_layout)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        getToolbar().setupWithNavController(navController, appBarConfiguration)
    }

    private fun getToolbar() = findViewById<Toolbar>(org.odk.collect.androidshared.R.id.toolbar)
}
