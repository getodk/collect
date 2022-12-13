/*
 * Copyright 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import org.odk.collect.android.R
import org.odk.collect.android.adapters.AboutListAdapter
import org.odk.collect.android.adapters.AboutListAdapter.AboutItemClickListener
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.ExternalWebPageHelper
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class AboutActivity : LocalizedActivity(), AboutItemClickListener {
    private val websiteTabHelper = ExternalWebPageHelper()
    private val forumTabHelper = ExternalWebPageHelper()
    private lateinit var websiteUri: Uri
    private lateinit var forumUri: Uri

    @Inject
    lateinit var intentLauncher: IntentLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)
        DaggerUtils.getComponent(this).inject(this)
        initToolbar()

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@AboutActivity)
            adapter = AboutListAdapter(ITEMS, this@AboutActivity, this@AboutActivity)
            itemAnimator = DefaultItemAnimator()
        }

        websiteUri = Uri.parse(getString(R.string.app_url))
        forumUri = Uri.parse(getString(R.string.forum_url))
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        title = getString(R.string.about_preferences)
        setSupportActionBar(toolbar)
    }

    override fun onClick(position: Int) {
        if (allowClick(javaClass.name)) {
            when (position) {
                0 -> websiteTabHelper.openWebPageInCustomTab(this, websiteUri)
                1 -> forumTabHelper.openWebPageInCustomTab(this, forumUri)
                2 -> shareApp()
                3 -> addReview()
                4 -> startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        websiteTabHelper.bindCustomTabsService(this, websiteUri)
        forumTabHelper.bindCustomTabsService(this, forumUri)
    }

    public override fun onDestroy() {
        unbindService(websiteTabHelper.serviceConnection)
        unbindService(forumTabHelper.serviceConnection)
        super.onDestroy()
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL + packageName
            )
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                getString(R.string.tell_your_friends)
            )
        )
    }

    private fun addReview() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        )
        intentLauncher.launch(this, intent) {
            // Show a list of all available browsers if user doesn't have a default browser
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_URL + packageName)
                )
            )
        }
    }

    companion object {
        private const val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="
        private val ITEMS = arrayOf(
            intArrayOf(
                R.drawable.ic_outline_website_24,
                R.string.odk_website,
                R.string.odk_website_summary
            ),
            intArrayOf(
                R.drawable.ic_outline_forum_24,
                R.string.odk_forum,
                R.string.odk_forum_summary
            ),
            intArrayOf(
                R.drawable.ic_outline_share_24,
                R.string.tell_your_friends,
                R.string.tell_your_friends_msg
            ),
            intArrayOf(
                R.drawable.ic_outline_rate_review_24,
                R.string.leave_a_review,
                R.string.leave_a_review_msg
            ),
            intArrayOf(
                R.drawable.ic_outline_stars_24,
                R.string.all_open_source_licenses,
                R.string.all_open_source_licenses_msg
            )
        )
    }
}
