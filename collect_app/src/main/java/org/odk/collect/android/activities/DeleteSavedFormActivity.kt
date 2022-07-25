/*
 * Copyright (C) 2017 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.odk.collect.android.R
import org.odk.collect.android.adapters.DeleteFormsTabsAdapter
import org.odk.collect.android.databinding.TabsLayoutBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.androidshared.utils.AppBarUtils.setupAppBarLayout
import javax.inject.Inject

class DeleteSavedFormActivity : CollectAbstractActivity() {
    @Inject
    lateinit var viewModelFactory: BlankFormListViewModel.Factory

    private val viewModel: BlankFormListViewModel by viewModels { viewModelFactory }

    private lateinit var binding: TabsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        binding = TabsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAppBarLayout(this, getString(R.string.manage_files))
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val viewPager = binding.viewPager.apply {
            adapter = DeleteFormsTabsAdapter(this@DeleteSavedFormActivity, viewModel.isMatchExactlyEnabled())
        }

        TabLayoutMediator(binding.tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = if (position == 0) getString(R.string.data) else getString(R.string.forms)
        }.attach()
    }
}
