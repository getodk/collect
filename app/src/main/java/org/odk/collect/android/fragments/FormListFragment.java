/*

Copyright 2017 Shobhit
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

import org.odk.collect.android.provider.FormsProviderAPI;

public class FormListFragment extends FileManagerFragment {
    @Override
    protected void sortByNameAsc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " DESC");
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DATE + " ASC");
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DATE + " DESC");
    }

    @Override
    protected void sortByStatusAsc() {
    }

    @Override
    protected void sortByStatusDesc() {
    }

    @Override
    protected void setupAdapter(String sortOrder) {
    }
}
