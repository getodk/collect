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

package org.odk.collect.android.activities;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.DeleteFormsTabsAdapter;
import org.odk.collect.android.formmanagement.BlankFormsListViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.androidshared.utils.AppBarUtils;

import javax.inject.Inject;

public class DeleteSavedFormActivity extends CollectAbstractActivity {

    @Inject
    BlankFormsListViewModel.Factory viewModelFactory;
    BlankFormsListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(BlankFormsListViewModel.class);

        setContentView(R.layout.tabs_layout);
        AppBarUtils.setupAppBarLayout(this, getString(R.string.manage_files));
        setUpViewPager();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.notes);
    }

    private void setUpViewPager() {
        String[] tabNames = {getString(R.string.data), getString(R.string.forms)};
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager.setAdapter(new DeleteFormsTabsAdapter(this, viewModel.isMatchExactlyEnabled()));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabNames[position])).attach();
    }
}
