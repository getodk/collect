package org.odk.collect.android.wassan.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.odk.collect.android.R
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.strings.localization.LocalizedActivity


class SplashActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        Handler().postDelayed({
            val mainIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(mainIntent)
            finish() // Close the splash activity
        }, 3000)
    }
}