package org.odk.collect.android.tasks;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.io.File;
import java.util.Arrays;

public class FormLoaderTaskTest {
    private static final String EXTERNAL_CSV_FORM = "external_csv_form.xml";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_CSV_FORM,
                    Arrays.asList("external_csv_cities.csv", "external_csv_countries.csv", "external_csv_neighbourhoods.csv")));

    @Test
    public void loadFormWithSecondaryCSV() throws Exception {
        final String formPath = new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
    }
}
