/*
 * Copyright (C) 2020 ODK
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

package org.odk.collect.android.database;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.instances.DatabaseInstancesRepository;
import org.odk.collect.android.database.instances.InstancesDatabaseProvider;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.InstancesRepositoryTest;

import java.util.function.Supplier;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstancesRepositoryTest extends InstancesRepositoryTest {

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject();
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
        storagePathProvider = component.storagePathProvider();
    }

    @Override
    public InstancesRepository buildSubject() {
        return new DatabaseInstancesRepository(new InstancesDatabaseProvider(Collect.getInstance(), new StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA)), storagePathProvider, System::currentTimeMillis);
    }

    @Override
    public InstancesRepository buildSubject(Supplier<Long> clock) {
        return new DatabaseInstancesRepository(new InstancesDatabaseProvider(Collect.getInstance(), new StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA)), storagePathProvider, clock);
    }

    @Override
    public String getInstancesDir() {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES);
    }
}
