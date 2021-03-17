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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.support.InstanceUtils.buildInstance;

public abstract class InstancesRepositoryTest {

    public abstract InstancesRepository buildSubject();

    @Test
    public void getAllNotDeleted_returnsUndeletedInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("deleted", "1")
                .status(Instance.STATUS_COMPLETE)
                .deletedDate(System.currentTimeMillis())
                .build());
        instancesRepository.save(buildInstance("undeleted", "1")
                .status(Instance.STATUS_COMPLETE)
                .build());

        List<Instance> allNotDeleted = instancesRepository.getAllNotDeleted();
        assertThat(allNotDeleted.size(), is(1));
        assertThat(allNotDeleted.get(0).getJrFormId(), is("undeleted"));
    }

    @Test
    public void getAllByStatus_withOneStatus_returnsMatchingInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("incomplete", "1")
                .status(Instance.STATUS_INCOMPLETE)
                .build());
        instancesRepository.save(buildInstance("incomplete", "1")
                .status(Instance.STATUS_INCOMPLETE)
                .build());

        instancesRepository.save(buildInstance("complete", "1")
                .status(Instance.STATUS_COMPLETE)
                .build());
        instancesRepository.save(buildInstance("complete", "1")
                .status(Instance.STATUS_COMPLETE)
                .build());

        List<Instance> incomplete = instancesRepository.getAllByStatus(Instance.STATUS_INCOMPLETE);
        assertThat(incomplete.size(), is(2));
        assertThat(incomplete.get(0).getJrFormId(), is("incomplete"));
        assertThat(incomplete.get(1).getStatus(), is("incomplete"));

        // Check corresponding count method is also correct
        assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE), is(2));
    }

    @Test
    public void getAllByStatus_withMultipleStatus_returnsMatchingInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("incomplete", "1")
                .status(Instance.STATUS_INCOMPLETE)
                .build());
        instancesRepository.save(buildInstance("incomplete", "1")
                .status(Instance.STATUS_INCOMPLETE)
                .build());

        instancesRepository.save(buildInstance("complete", "1")
                .status(Instance.STATUS_COMPLETE)
                .build());
        instancesRepository.save(buildInstance("complete", "1")
                .status(Instance.STATUS_COMPLETE)
                .build());

        instancesRepository.save(buildInstance("submitted", "1")
                .status(Instance.STATUS_SUBMITTED)
                .build());
        instancesRepository.save(buildInstance("submitted", "1")
                .status(Instance.STATUS_SUBMITTED)
                .build());

        List<Instance> incomplete = instancesRepository.getAllByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED);
        assertThat(incomplete.size(), is(4));
        assertThat(incomplete.get(0).getJrFormId(), is(not("complete")));
        assertThat(incomplete.get(1).getJrFormId(), is(not("complete")));
        assertThat(incomplete.get(2).getJrFormId(), is(not("complete")));
        assertThat(incomplete.get(3).getStatus(), is(not("complete")));

        // Check corresponding count method is also correct
        assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED), is(4));
    }

    @Test
    public void getAllByFormId_includesAllVersionsForFormId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("formid", "1").build());
        instancesRepository.save(buildInstance("formid", "2", "display", Instance.STATUS_COMPLETE, null).build());
        instancesRepository.save(buildInstance("formid", "3").build());
        instancesRepository.save(buildInstance("formid", "4", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis()).build());
        instancesRepository.save(buildInstance("formid2", "1", "display", Instance.STATUS_COMPLETE, null).build());

        List<Instance> instances = instancesRepository.getAllByFormId("formid");
        assertThat(instances.size(), is(4));
    }

    @Test
    public void getAllByFormIdAndVersionNotDeleted_excludesDeleted() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("formid", "1").build());
        instancesRepository.save(buildInstance("formid", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());
        instancesRepository.save(buildInstance("formid", "1").build());
        instancesRepository.save(buildInstance("formid", "1", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis())
                .build());
        instancesRepository.save(buildInstance("formid2", "1", "display", Instance.STATUS_COMPLETE, null)
                .build());

        List<Instance> instances = instancesRepository.getAllNotDeletedByFormIdAndVersion("formid", "1");
        assertThat(instances.size(), is(3));
    }

    @Test
    public void deleteAll_deletesAllInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("formid", "1").build());
        instancesRepository.save(buildInstance("formid", "1").build());
        Long id1 = instancesRepository.getAll().get(0).getId();
        Long id2 = instancesRepository.getAll().get(1).getId();

        instancesRepository.deleteAll();
        assertThat(instancesRepository.get(id1), is(nullValue()));
        assertThat(instancesRepository.get(id2), is(nullValue()));
    }

    @Test
    public void save_addsUniqueId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(buildInstance("formid", "1").build());
        instancesRepository.save(buildInstance("formid", "1").build());

        Long id1 = instancesRepository.getAll().get(0).getId();
        Long id2 = instancesRepository.getAll().get(1).getId();
        assertThat(id1, notNullValue());
        assertThat(id2, notNullValue());
        assertThat(id1, not(equalTo(id2)));
    }

    @Test
    public void save_returnsInstanceWithId() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(buildInstance("formid", "1").build());
        assertThat(instancesRepository.get(instance.getId()), is(instance));
    }

    @Test
    public void save_whenInstanceHasId_updatesExisting() {
        InstancesRepository instancesRepository = buildSubject();

        Instance originalInstance = instancesRepository.save(buildInstance("formid", "1")
                .displayName("Blah")
                .build());

        instancesRepository.save(new Instance.Builder(originalInstance)
                .displayName("A different blah")
                .build());

        assertThat(instancesRepository.get(originalInstance.getId()).getDisplayName(), is("A different blah"));
    }

    @Test
    public void save_whenStatusIsNull_usesIncomplete() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(buildInstance("formid", "1")
                .status(null)
                .build());
        assertThat(instancesRepository.get(instance.getId()).getStatus(), is(Instance.STATUS_INCOMPLETE));
    }

    @Test
    public void save_whenLastStatusChangeDateIsNull_setsIt() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(buildInstance("formid", "1")
                .lastStatusChangeDate(null)
                .build());
        assertThat(instancesRepository.get(instance.getId()).getLastStatusChangeDate(), is(notNullValue()));
    }

    @Test
    public void softDelete_setsDeletedDate() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(buildInstance("formid", "1").build());

        instancesRepository.softDelete(instance.getId());
        assertThat(instancesRepository.get(instance.getId()).getDeletedDate(), is(notNullValue()));
    }
}
