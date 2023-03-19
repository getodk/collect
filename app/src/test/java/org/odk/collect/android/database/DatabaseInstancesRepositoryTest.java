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

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.odk.collect.android.database.instances.DatabaseInstancesRepository;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.formstest.InstancesRepositoryTest;
import org.odk.collect.shared.TempFiles;

import java.io.File;
import java.util.function.Supplier;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstancesRepositoryTest extends InstancesRepositoryTest {

    private final File dbDir = TempFiles.createTempDir();
    private final File instancesDir = TempFiles.createTempDir();

    @Override
    public InstancesRepository buildSubject() {
        return new DatabaseInstancesRepository(ApplicationProvider.getApplicationContext(), dbDir.getAbsolutePath(), instancesDir.getAbsolutePath(), System::currentTimeMillis);
    }

    @Override
    public InstancesRepository buildSubject(Supplier<Long> clock) {
        return new DatabaseInstancesRepository(ApplicationProvider.getApplicationContext(), dbDir.getAbsolutePath(), instancesDir.getAbsolutePath(), clock);
    }

    @Override
    public String getInstancesDir() {
        return instancesDir.getAbsolutePath();
    }
}
