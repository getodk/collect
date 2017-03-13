/*
Copyright 2017 Nafundi
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.collect.android.fragments;

import org.odk.collect.android.provider.InstanceProviderAPI;

public class InstanceListFragment extends FileManagerFragment {
    @Override
    protected void sortByNameAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC, " +
                InstanceProviderAPI.InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " DESC, " +
                InstanceProviderAPI.InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC");
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC");
    }

    @Override
    protected void sortByStatusAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.STATUS + " ASC, " +
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void sortByStatusDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " +
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void setupAdapter(String sortOrder) {
    }
}
