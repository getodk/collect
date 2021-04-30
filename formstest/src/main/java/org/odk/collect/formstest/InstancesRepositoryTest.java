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

import org.junit.Test;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class InstancesRepositoryTest {

    public abstract InstancesRepository buildSubject();

    public abstract InstancesRepository buildSubject(Supplier<Long> clock);

    public abstract String getInstancesDir();

    @Test
    public void getAllNotDeleted_returnsUndeletedInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("deleted", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .deletedDate(System.currentTimeMillis())
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("undeleted", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .build());

        List<Instance> allNotDeleted = instancesRepository.getAllNotDeleted();
        assertThat(allNotDeleted.size(), is(1));
        assertThat(allNotDeleted.get(0).getFormId(), is("undeleted"));
    }

    @Test
    public void getAllByStatus_withOneStatus_returnsMatchingInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("incomplete", "1", getInstancesDir())
                .status(Instance.STATUS_INCOMPLETE)
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("incomplete", "1", getInstancesDir())
                .status(Instance.STATUS_INCOMPLETE)
                .build());

        instancesRepository.save(InstanceUtils.buildInstance("complete", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("complete", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .build());

        List<Instance> incomplete = instancesRepository.getAllByStatus(Instance.STATUS_INCOMPLETE);
        assertThat(incomplete.size(), is(2));
        assertThat(incomplete.get(0).getFormId(), is("incomplete"));
        assertThat(incomplete.get(1).getStatus(), is("incomplete"));

        // Check corresponding count method is also correct
        assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE), is(2));
    }

    @Test
    public void getAllByStatus_withMultipleStatus_returnsMatchingInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("incomplete", "1", getInstancesDir())
                .status(Instance.STATUS_INCOMPLETE)
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("incomplete", "1", getInstancesDir())
                .status(Instance.STATUS_INCOMPLETE)
                .build());

        instancesRepository.save(InstanceUtils.buildInstance("complete", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("complete", "1", getInstancesDir())
                .status(Instance.STATUS_COMPLETE)
                .build());

        instancesRepository.save(InstanceUtils.buildInstance("submitted", "1", getInstancesDir())
                .status(Instance.STATUS_SUBMITTED)
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("submitted", "1", getInstancesDir())
                .status(Instance.STATUS_SUBMITTED)
                .build());

        List<Instance> incomplete = instancesRepository.getAllByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED);
        assertThat(incomplete.size(), is(4));
        assertThat(incomplete.get(0).getFormId(), is(not("complete")));
        assertThat(incomplete.get(1).getFormId(), is(not("complete")));
        assertThat(incomplete.get(2).getFormId(), is(not("complete")));
        assertThat(incomplete.get(3).getStatus(), is(not("complete")));

        // Check corresponding count method is also correct
        assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED), is(4));
    }

    @Test
    public void getAllByFormId_includesAllVersionsForFormId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "2", "display", Instance.STATUS_COMPLETE, null, getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "3", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "4", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis(), getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid2", "1", "display", Instance.STATUS_COMPLETE, null, getInstancesDir()).build());

        List<Instance> instances = instancesRepository.getAllByFormId("formid");
        assertThat(instances.size(), is(4));
    }

    @Test
    public void getAllByFormIdAndVersionNotDeleted_excludesDeleted() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", "display", Instance.STATUS_COMPLETE, null, getInstancesDir())
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", "display", Instance.STATUS_COMPLETE, System.currentTimeMillis(), getInstancesDir())
                .build());
        instancesRepository.save(InstanceUtils.buildInstance("formid2", "1", "display", Instance.STATUS_COMPLETE, null, getInstancesDir())
                .build());

        List<Instance> instances = instancesRepository.getAllNotDeletedByFormIdAndVersion("formid", "1");
        assertThat(instances.size(), is(3));
    }

    @Test
    public void deleteAll_deletesAllInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.deleteAll();
        assertThat(instancesRepository.getAll().size(), is(0));
    }

    @Test
    public void deleteAll_deletesInstanceFiles() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance1 = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        Instance instance2 = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.deleteAll();
        assertThat(new File(instance1.getInstanceFilePath()).exists(), is(false));
        assertThat(new File(instance2.getInstanceFilePath()).exists(), is(false));
    }

    @Test
    public void save_addsUniqueId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        Long id1 = instancesRepository.getAll().get(0).getDbId();
        Long id2 = instancesRepository.getAll().get(1).getDbId();
        assertThat(id1, notNullValue());
        assertThat(id2, notNullValue());
        assertThat(id1, not(equalTo(id2)));
    }

    @Test
    public void save_returnsInstanceWithId() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        assertThat(instancesRepository.get(instance.getDbId()), is(instance));
    }

    @Test
    public void save_whenInstanceHasId_updatesExisting() {
        InstancesRepository instancesRepository = buildSubject();

        Instance originalInstance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .displayName("Blah")
                .build());

        instancesRepository.save(new Instance.Builder(originalInstance)
                .displayName("A different blah")
                .build());

        assertThat(instancesRepository.get(originalInstance.getDbId()).getDisplayName(), is("A different blah"));
    }

    @Test
    public void save_whenInstanceHasId_updatesLastStatusChangeDate() {
        Supplier<Long> clock = mock(Supplier.class);
        when(clock.get()).thenReturn(123L);

        InstancesRepository instancesRepository = buildSubject(clock);

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.save(instance);
        assertThat(instancesRepository.get(instance.getDbId()).getLastStatusChangeDate(), is(123L));
    }

    @Test
    public void save_whenStatusIsNull_usesIncomplete() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .status(null)
                .build());
        assertThat(instancesRepository.get(instance.getDbId()).getStatus(), is(Instance.STATUS_INCOMPLETE));
    }

    @Test
    public void save_whenLastStatusChangeDateIsNull_setsIt() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .lastStatusChangeDate(null)
                .build());
        assertThat(instancesRepository.get(instance.getDbId()).getLastStatusChangeDate(), is(notNullValue()));
    }

    @Test
    public void save_whenInstanceHasDeletedDate_doesNotUpdateLastChangesStatusDate() {
        Supplier<Long> clock = mock(Supplier.class);
        when(clock.get()).thenReturn(123L);

        InstancesRepository instancesRepository = buildSubject(clock);

        Instance originalInstance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        Long originalInstanceDbId = originalInstance.getDbId();

        when(clock.get()).thenReturn(456L);
        instancesRepository.deleteWithLogging(originalInstanceDbId);
        instancesRepository.save(instancesRepository.get(originalInstanceDbId));

        assertThat(instancesRepository.get(originalInstanceDbId).getLastStatusChangeDate(), is(123L));
    }

    @Test
    public void deleteWithLogging_setsDeletedDate() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.deleteWithLogging(instance.getDbId());
        assertThat(instancesRepository.get(instance.getDbId()).getDeletedDate(), is(notNullValue()));
    }

    @Test
    public void deleteWithLogging_deletesInstanceDir() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        File instanceDir = new File(instance.getInstanceFilePath()).getParentFile();
        assertThat(instanceDir.exists(), is(true));
        assertThat(instanceDir.isDirectory(), is(true));

        instancesRepository.deleteWithLogging(instance.getDbId());
        assertThat(instanceDir.exists(), is(false));
    }

    @Test
    public void deleteWithLogging_clearsGeometryData() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .geometry("blah")
                .geometryType("blah")
                .build());

        instancesRepository.deleteWithLogging(instance.getDbId());
        assertThat(instancesRepository.get(instance.getDbId()).getGeometry(), is(nullValue()));
        assertThat(instancesRepository.get(instance.getDbId()).getGeometryType(), is(nullValue()));
    }

    @Test
    public void delete_deletesInstance() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.delete(instance.getDbId());
        assertThat(instancesRepository.getAll().size(), is(0));
    }

    @Test
    public void delete_deletesInstanceDir() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        // The repo assumes the parent of the file also contains other instance files
        File instanceDir = new File(instance.getInstanceFilePath()).getParentFile();
        assertThat(instanceDir.exists(), is(true));
        assertThat(instanceDir.isDirectory(), is(true));

        instancesRepository.delete(instance.getDbId());
        assertThat(instanceDir.exists(), is(false));
    }
}
