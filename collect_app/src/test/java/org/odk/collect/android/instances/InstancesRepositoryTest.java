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

package org.odk.collect.android.instances;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.support.InstanceUtils.buildInstance;

public abstract class InstancesRepositoryTest {
    public abstract InstancesRepository buildSubject();

    @Test
    public void getAllFinalized_excludesUnfinalized() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance(1L, "formid", "1").build());
        instancesRepository.save(buildInstance(2L, "formid", "1", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis())
                .build());
        instancesRepository.save(buildInstance(3L, "formid", "1").build());
        instancesRepository.save(buildInstance(4L, "formid", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());
        instancesRepository.save(buildInstance(5L, "formid2", "1", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis())
                .build());

        List<Instance> instances = instancesRepository.getAllFinalized();
        assertThat(instances.size(), is(3));
    }

    @Test
    public void getAllByFormId_includesAllVersionsForFormId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance(1L, "formid", "1").build());
        instancesRepository.save(buildInstance(2L, "formid", "2", "display", Instance.STATUS_COMPLETE, null)
                .build());
        instancesRepository.save(buildInstance(3L, "formid", "3").build());
        instancesRepository.save(buildInstance(4L, "formid", "4", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis())
                .build());
        instancesRepository.save(buildInstance(5L, "formid2", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());

        List<Instance> instances = instancesRepository.getAllByFormId("formid");
        assertThat(instances.size(), is(4));
    }

    @Test
    public void getAllByFormIdAndVersionNotDeleted_excludesDeleted() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance(1L, "formid", "1").build());
        instancesRepository.save(buildInstance(2L, "formid", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());
        instancesRepository.save(buildInstance(3L, "formid", "1").build());
        instancesRepository.save(buildInstance(4L, "formid", "1", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis())
                .build());
        instancesRepository.save(buildInstance(5L, "formid2", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());

        List<Instance> instances = instancesRepository.getAllNotDeletedByFormIdAndVersion("formid", "1");
        assertThat(instances.size(), is(3));
    }
}
