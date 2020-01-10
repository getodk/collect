package org.odk.collect.android.utilities;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InstanceUploaderUtilsTest {
    /**
     * 1000 instances is a big number that would product a very long sql query that would cause
     * SQLiteException: Expression tree is too large if we didn't split it into parts.
     */
    private static final int NUMBER_OF_INSTANCES_TO_SEND = 1000;

    private final InstancesDao instancesDao = new InstancesDao();

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    @Test
    public void getUploadResultMessageTest() {
        fillTestDatabase();
        assertEquals(getExpectedResultMsg(), InstanceUploaderUtils.getUploadResultMessage(ApplicationProvider.getApplicationContext(), getTestUploadResult()));
        instancesDao.deleteInstancesDatabase();
    }

    @Test
    public void doesUrlRefersToGoogleSheetsFileTest() {
        assertTrue(InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile("https://docs.google.com/spreadsheets/d/169qibpJCWgUy-SRtoyvKd1EKwV1nDfM0/edit#gid=773120038"));
        assertFalse(InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile("https://drive.google.com/file/d/169qibpJCWgUy-SRtoyvKd1EKwV1nDfM0/edit#gid=773120038"));
    }

    private void fillTestDatabase() {
        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            long time = System.currentTimeMillis();
            Instance instance = new Instance.Builder()
                    .displayName("InstanceTest")
                    .instanceFilePath(Collect.INSTANCES_PATH + "/InstanceTest_" + time + "/InstanceTest_" + time + ".xml")
                    .jrFormId("instanceTest")
                    .status(InstanceProviderAPI.STATUS_COMPLETE)
                    .lastStatusChangeDate(time)
                    .build();
            instancesDao.saveInstance(instancesDao.getValuesFromInstanceObject(instance));
        }
    }

    private Map<String, String> getTestUploadResult() {
        Map<String, String> result = new HashMap<>();
        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            result.put(String.valueOf(i), "full submission upload was successful!");
        }
        return result;
    }

    private String getExpectedResultMsg() {
        StringBuilder expectedResultMsg = new StringBuilder();
        for (int i = 1; i <= NUMBER_OF_INSTANCES_TO_SEND; i++) {
            expectedResultMsg.append("InstanceTest - Success\n\n");
        }
        return expectedResultMsg.toString().trim();
    }
}
