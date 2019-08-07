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

import android.Manifest;
import android.database.Cursor;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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

    // sample instances
    private Instance hypertensionScreeningInstance;
    private Instance cascadingSelectInstance;
    private Instance biggestNOfSetInstance;
    private Instance widgetsInstance;
    private Instance sampleInstance;
    private Instance biggestNOfSet2Instance;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

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
        assertEquals(cascadingSelectInstance, instances.get(0));
        assertEquals(hypertensionScreeningInstance, instances.get(1));
        assertEquals(sampleInstance, instances.get(2));
        assertEquals(biggestNOfSet2Instance, instances.get(3));
    }

    @Test
    public void getSentInstancesCursorTest() {
        Cursor cursor = instancesDao.getSentInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(2, instances.size());
        assertEquals(biggestNOfSetInstance, instances.get(0));
        assertEquals(widgetsInstance, instances.get(1));
    }

    @Test
    public void getSavedInstancesCursorTest() {
        Cursor cursor = instancesDao.getSavedInstancesCursor(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(5, instances.size());
        assertEquals(biggestNOfSetInstance, instances.get(0));
        assertEquals(biggestNOfSet2Instance, instances.get(1));
        assertEquals(cascadingSelectInstance, instances.get(2));
        assertEquals(hypertensionScreeningInstance, instances.get(3));
        assertEquals(sampleInstance, instances.get(4));
    }

    @Test
    public void getFinalizedInstancesCursorTest() {
        Cursor cursor = instancesDao.getFinalizedInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(1, instances.size());
        assertEquals(biggestNOfSet2Instance, instances.get(0));
    }

    @Test
    public void getInstancesCursorForFilePathTest() {
        Cursor cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Hypertension Screening_2017-02-20_14-03-53/Hypertension Screening_2017-02-20_14-03-53.xml");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(1, instances.size());
        assertEquals(hypertensionScreeningInstance, instances.get(0));
    }

    @Test
    public void getAllCompletedUndeletedInstancesCursorTest() {
        Cursor cursor = instancesDao.getAllCompletedUndeletedInstancesCursor();
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(2, instances.size());
        assertEquals(biggestNOfSetInstance, instances.get(0));
        assertEquals(biggestNOfSet2Instance, instances.get(1));
    }

    @Test
    public void getInstancesCursorForIdTest() {
        Cursor cursor = instancesDao.getInstancesCursorForId("2");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(1, instances.size());
        assertEquals(cascadingSelectInstance, instances.get(0));
    }

    @Test
    public void updateInstanceTest() {
        Cursor cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml");
        List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(1, instances.size());
        assertEquals(biggestNOfSet2Instance, instances.get(0));

        biggestNOfSetInstance = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487597090653L)
                .build();

        String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] whereArgs = {Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml"};

        assertEquals(instancesDao.updateInstance(instancesDao.getValuesFromInstanceObject(biggestNOfSet2Instance), where, whereArgs), 1);

        cursor = instancesDao.getInstancesCursorForFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml");

        instances = instancesDao.getInstancesFromCursor(cursor);

        assertEquals(1, instances.size());
        assertEquals(biggestNOfSet2Instance, instances.get(0));
    }

    private void fillDatabase() {
        hypertensionScreeningInstance = new Instance.Builder()
                .displayName("Hypertension Screening")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Hypertension Screening_2017-02-20_14-03-53/Hypertension Screening_2017-02-20_14-03-53.xml")
                .jrFormId("hypertension")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487595836793L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(hypertensionScreeningInstance));

        cascadingSelectInstance = new Instance.Builder()
                .displayName("Cascading Select Form")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Cascading Select Form_2017-02-20_14-06-44/Cascading Select Form_2017-02-20_14-06-44.xml")
                .jrFormId("CascadingSelect")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487596015000L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(cascadingSelectInstance));

        biggestNOfSetInstance = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-06-51/Biggest N of Set_2017-02-20_14-06-51.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487596015100L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(biggestNOfSetInstance));

        widgetsInstance = new Instance.Builder()
                .displayName("Widgets")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Widgets_2017-02-20_14-06-58/Widgets_2017-02-20_14-06-58.xml")
                .jrFormId("widgets")
                .status(InstanceProviderAPI.STATUS_SUBMITTED)
                .lastStatusChangeDate(1487596020803L)
                .deletedDate(1487596020803L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(widgetsInstance));

        sampleInstance = new Instance.Builder()
                .displayName("sample")
                .instanceFilePath(Collect.INSTANCES_PATH + "/sample_2017-02-20_14-07-03/sample_2017-02-20_14-07-03.xml")
                .jrFormId("sample")
                .status(InstanceProviderAPI.STATUS_INCOMPLETE)
                .lastStatusChangeDate(1487596026373L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(sampleInstance));

        biggestNOfSet2Instance = new Instance.Builder()
                .displayName("Biggest N of Set")
                .instanceFilePath(Collect.INSTANCES_PATH + "/Biggest N of Set_2017-02-20_14-24-46/Biggest N of Set_2017-02-20_14-24-46.xml")
                .jrFormId("N_Biggest")
                .status(InstanceProviderAPI.STATUS_COMPLETE)
                .lastStatusChangeDate(1487597090653L)
                .build();
        instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(biggestNOfSet2Instance));
    }

    @After
    public void tearDown() {
        instancesDao.deleteInstancesDatabase();
    }
}
