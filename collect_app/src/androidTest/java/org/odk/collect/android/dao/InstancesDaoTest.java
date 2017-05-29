/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.dao;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
/**
 * This class contains tests for {@link InstancesDao}
 */
public class InstancesDaoTest {

    private InstancesDao instancesDao;

    @Before
    public void setUp() {
        instancesDao = new InstancesDao();
        instancesDao.deleteInstancesDatabase();
        fillDatabase();
    }

    @Test
    public void getUnsentInstancesCursorTest() {
        Cursor cursor = instancesDao.getUnsentInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(4, instances.size());

        assertEquals("Cascading Select Form", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(0).getStatus());

        assertEquals("Hypertension Screening", instances.get(1).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(1).getStatus());

        assertEquals("sample", instances.get(2).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(2).getStatus());

        assertEquals("Biggest N of Set", instances.get(3).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_COMPLETE, instances.get(3).getStatus());
    }

    @Test
    public void getSentInstancesCursorTest() {
        Cursor cursor = instancesDao.getSentInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(2, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_SUBMITTED, instances.get(0).getStatus());

        assertEquals("Widgets", instances.get(1).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_SUBMITTED, instances.get(1).getStatus());
    }

    @Test
    public void getSavedInstancesCursorTest() {
        Cursor cursor = instancesDao.getSavedInstancesCursor(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(5, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_SUBMITTED, instances.get(0).getStatus());

        assertEquals("Biggest N of Set", instances.get(1).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_COMPLETE, instances.get(1).getStatus());

        assertEquals("Cascading Select Form", instances.get(2).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(2).getStatus());

        assertEquals("Hypertension Screening", instances.get(3).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(3).getStatus());

        assertEquals("sample", instances.get(4).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(4).getStatus());
    }

    @Test
    public void getFinalizedInstancesCursorTest() {
        Cursor cursor = instancesDao.getFinalizedInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(1, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_COMPLETE, instances.get(0).getStatus());
    }

    @Test
    public void getInstancesCursorForFilePathTest() {
        Cursor cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Hypertension Screening_2017-02-20_14-03-53/Hypertension Screening_2017-02-20_14-03-53.xml");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(1, instances.size());

        assertEquals("Hypertension Screening", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(0).getStatus());
    }

    @Test
    public void getAllCompletedUndeletedInstancesCursorTest() {
        Cursor cursor = instancesDao.getAllCompletedUndeletedInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(2, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_SUBMITTED, instances.get(0).getStatus());

        assertEquals("Biggest N of Set", instances.get(1).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_COMPLETE, instances.get(1).getStatus());
    }

    @Test
    public void getInstancesCursorForIdTest() {
        Cursor cursor = instancesDao.getInstancesCursorForId("2");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(1, instances.size());

        assertEquals("Cascading Select Form", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_INCOMPLETE, instances.get(0).getStatus());
    }


    @Test
    public void updateInstanceTest() {
        Cursor cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(1, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_COMPLETE, instances.get(0).getStatus());

        Instance instance = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487597090653L)
                .displaySubtext("Finalized on Mon, Feb 20, 2017 at 14:24")
                .build();

        String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] whereArgs = {Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml"};

        assertEquals(instancesDao.updateInstance(instancesDao.getValuesFromInstanceObject(instance), where, whereArgs), 1);

        cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml");

        instances = instancesDao.getInstancesFromCursor(cursor);
        assertEquals(1, instances.size());

        assertEquals("Biggest N of Set", instances.get(0).getDisplayName());
        assertEquals(InstanceProviderAPI.STATUS_SUBMITTED, instances.get(0).getStatus());
    }

    private void fillDatabase() {
        Instance instance1 = new Instance.Builder()
                .displayName("Hypertension Screening")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Hypertension Screening_2017-02-20_14-03-53/Hypertension Screening_2017-02-20_14-03-53.xml")
                .jrFormId("hypertension")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487595836793L)
                .displaySubtext("Saved on Mon, Feb 20, 2017 at 14:03")
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance1));

        Instance instance2 = new Instance.Builder()
                .displayName("Cascading Select Form")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Cascading Select Form_2017-02-20_14-06-44/Cascading Select Form_2017-02-20_14-06-44.xml")
                .jrFormId("CascadingSelect")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487596015000L)
                .displaySubtext("Saved on Mon, Feb 20, 2017 at 14:06")
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance2));

        Instance instance3 = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-06-51/Biggest N of Set_2017-02-20_14-06-51.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487596015100L)
                .displaySubtext("Saved on Mon, Feb 20, 2017 at 14:06")
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance3));

        Instance instance4 = new Instance.Builder()
                .displayName("Widgets")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Widgets_2017-02-20_14-06-58/Widgets_2017-02-20_14-06-58.xml")
                .jrFormId("widgets")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487596020803L)
                .displaySubtext("Saved on Mon, Feb 20, 2017 at 14:07")
                .deletedDate(1487596020803L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance4));

        Instance instance5 = new Instance.Builder()
                .displayName("sample")
                .instanceFilePath(Collect.INSTANCES_PATH + "/sample_2017-02-20_14-07-03/sample_2017-02-20_14-07-03.xml")
                .jrFormId("sample")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487596026373L)
                .displaySubtext("Saved on Mon, Feb 20, 2017 at 14:07")
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance5));

        Instance instance6 = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_COMPLETE)
                .lastStatusChangeDate(1487597090653L)
                .displaySubtext("Finalized on Mon, Feb 20, 2017 at 14:24")
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance6));
    }

    @After
    public void tearDown() {
        instancesDao.deleteInstancesDatabase();
    }
}
