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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.odk.collect.formstest.InstanceUtils.buildInstance;

public abstract class InstancesRepositoryTest {

    public abstract InstancesRepository buildSubject();

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
        MatcherAssert.assertThat(allNotDeleted.size(), Matchers.is(1));
        MatcherAssert.assertThat(allNotDeleted.get(0).getFormId(), Matchers.is("undeleted"));
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
        MatcherAssert.assertThat(incomplete.size(), Matchers.is(2));
        MatcherAssert.assertThat(incomplete.get(0).getFormId(), Matchers.is("incomplete"));
        MatcherAssert.assertThat(incomplete.get(1).getStatus(), Matchers.is("incomplete"));

        // Check corresponding count method is also correct
        MatcherAssert.assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE), Matchers.is(2));
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
        MatcherAssert.assertThat(incomplete.size(), Matchers.is(4));
        MatcherAssert.assertThat(incomplete.get(0).getFormId(), Matchers.is(Matchers.not("complete")));
        MatcherAssert.assertThat(incomplete.get(1).getFormId(), Matchers.is(Matchers.not("complete")));
        MatcherAssert.assertThat(incomplete.get(2).getFormId(), Matchers.is(Matchers.not("complete")));
        MatcherAssert.assertThat(incomplete.get(3).getStatus(), Matchers.is(Matchers.not("complete")));

        // Check corresponding count method is also correct
        MatcherAssert.assertThat(instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED), Matchers.is(4));
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
        MatcherAssert.assertThat(instances.size(), Matchers.is(4));
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
        MatcherAssert.assertThat(instances.size(), Matchers.is(3));
    }

    @Test
    public void deleteAll_deletesAllInstances() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.deleteAll();
        MatcherAssert.assertThat(instancesRepository.getAll().size(), Matchers.is(0));
    }

    @Test
    public void deleteAll_deletesInstanceFiles() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance1 = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        Instance instance2 = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.deleteAll();
        MatcherAssert.assertThat(new File(instance1.getInstanceFilePath()).exists(), Matchers.is(false));
        MatcherAssert.assertThat(new File(instance2.getInstanceFilePath()).exists(), Matchers.is(false));
    }

    @Test
    public void save_addsUniqueId() {
        InstancesRepository instancesRepository = buildSubject();

        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        Long id1 = instancesRepository.getAll().get(0).getDbId();
        Long id2 = instancesRepository.getAll().get(1).getDbId();
        MatcherAssert.assertThat(id1, Matchers.notNullValue());
        MatcherAssert.assertThat(id2, Matchers.notNullValue());
        MatcherAssert.assertThat(id1, Matchers.not(Matchers.equalTo(id2)));
    }

    @Test
    public void save_returnsInstanceWithId() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());
        MatcherAssert.assertThat(instancesRepository.get(instance.getDbId()), Matchers.is(instance));
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

        MatcherAssert.assertThat(instancesRepository.get(originalInstance.getDbId()).getDisplayName(), Matchers.is("A different blah"));
    }

    @Test
    public void save_whenStatusIsNull_usesIncomplete() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .status(null)
                .build());
        MatcherAssert.assertThat(instancesRepository.get(instance.getDbId()).getStatus(), Matchers.is(Instance.STATUS_INCOMPLETE));
    }

    @Test
    public void save_whenLastStatusChangeDateIsNull_setsIt() {
        InstancesRepository instancesRepository = buildSubject();

        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir())
                .lastStatusChangeDate(null)
                .build());
        MatcherAssert.assertThat(instancesRepository.get(instance.getDbId()).getLastStatusChangeDate(), Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void softDelete_setsDeletedDate() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.softDelete(instance.getDbId());
        MatcherAssert.assertThat(instancesRepository.get(instance.getDbId()).getDeletedDate(), Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void softDelete_deletesInstanceDir() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        File instanceDir = new File(instance.getInstanceFilePath()).getParentFile();
        MatcherAssert.assertThat(instanceDir.exists(), Matchers.is(true));
        MatcherAssert.assertThat(instanceDir.isDirectory(), Matchers.is(true));

        instancesRepository.softDelete(instance.getDbId());
        MatcherAssert.assertThat(instanceDir.exists(), Matchers.is(false));
    }

    @Test
    public void delete_deletesInstance() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        instancesRepository.delete(instance.getDbId());
        MatcherAssert.assertThat(instancesRepository.getAll().size(), Matchers.is(0));
    }

    @Test
    public void delete_deletesInstanceDir() {
        InstancesRepository instancesRepository = buildSubject();
        Instance instance = instancesRepository.save(InstanceUtils.buildInstance("formid", "1", getInstancesDir()).build());

        // The repo assumes the parent of the file also contains other instance files
        File instanceDir = new File(instance.getInstanceFilePath()).getParentFile();
        MatcherAssert.assertThat(instanceDir.exists(), Matchers.is(true));
        MatcherAssert.assertThat(instanceDir.isDirectory(), Matchers.is(true));

        instancesRepository.delete(instance.getDbId());
        MatcherAssert.assertThat(instanceDir.exists(), Matchers.is(false));
    }
}
