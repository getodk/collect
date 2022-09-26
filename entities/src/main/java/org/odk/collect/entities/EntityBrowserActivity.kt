package org.odk.collect.entities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import org.odk.collect.strings.localization.LocalizedActivity

class EntityBrowserActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.entities_layout)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}
