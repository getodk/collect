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

package org.odk.collect.formstest;

import org.junit.Before;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.shared.TempFiles;

import java.util.function.Supplier;

public class InMemInstancesRepositoryTest extends InstancesRepositoryTest {

    private String tempDirectory;

    @Before
    public void setup() {
        tempDirectory = TempFiles.createTempDir().getAbsolutePath();
    }

    @Override
    public InstancesRepository buildSubject() {
        return new InMemInstancesRepository(System::currentTimeMillis);
    }

    @Override
    public InstancesRepository buildSubject(Supplier<Long> clock) {
        return new InMemInstancesRepository(clock);
    }

    @Override
    public String getInstancesDir() {
        return tempDirectory;
    }
}
