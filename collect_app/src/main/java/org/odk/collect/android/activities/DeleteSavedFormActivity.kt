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

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.odk.collect.android.databinding.TabsLayoutBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formlists.blankformlist.DeleteBlankFormFragment
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.fragments.SavedFormListFragment
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.ListFragmentStateAdapter
import org.odk.collect.androidshared.ui.MultiSelectViewModel
import org.odk.collect.androidshared.utils.AppBarUtils.setupAppBarLayout
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.shared.settings.Settings
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class DeleteSavedFormActivity : LocalizedActivity() {
    @Inject
    lateinit var projectDependencyProviderFactory: ProjectDependencyProviderFactory

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var scheduler: Scheduler

    private lateinit var binding: TabsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)

        val projectId = projectsDataService.getCurrentProject().uuid
        val projectDependencyProvider = projectDependencyProviderFactory.create(projectId)

        val viewModelFactory = ViewModelFactory(
            projectDependencyProvider.instancesRepository,
            this.application,
            formsDataService,
            scheduler,
            projectDependencyProvider.generalSettings,
            projectId
        )

        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        val blankFormsListViewModel = viewModelProvider[BlankFormListViewModel::class.java]

        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(DeleteBlankFormFragment::class) {
                DeleteBlankFormFragment(viewModelFactory, this)
            }
            .build()

        super.onCreate(savedInstanceState)
        binding = TabsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAppBarLayout(this, getString(org.odk.collect.strings.R.string.manage_files))
        setUpViewPager(blankFormsListViewModel)
    }

    private fun setUpViewPager(viewModel: BlankFormListViewModel) {
        val fragments = if (viewModel.isMatchExactlyEnabled()) {
            listOf(SavedFormListFragment::class.java.name)
        } else {
            listOf(
                SavedFormListFragment::class.java.name,
                DeleteBlankFormFragment::class.java.name
            )
        }

        val viewPager = binding.viewPager.also {
            it.adapter =
                ListFragmentStateAdapter(
                    this,
                    fragments
                )
        }

        TabLayoutMediator(binding.tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = if (position == 0) {
                getString(org.odk.collect.strings.R.string.data)
            } else {
                getString(org.odk.collect.strings.R.string.forms)
            }
        }.attach()
    }

    private class ViewModelFactory(
        private val instancesRepository: InstancesRepository,
        private val application: Application,
        private val formsDataService: FormsDataService,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
        private val projectId: String
    ) :
        ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                BlankFormListViewModel::class.java -> BlankFormListViewModel(
                    instancesRepository,
                    application,
                    formsDataService,
                    scheduler,
                    generalSettings,
                    projectId,
                    showAllVersions = true
                )

                MultiSelectViewModel::class.java -> MultiSelectViewModel()
                else -> throw IllegalArgumentException()
            } as T
        }
    }
}
