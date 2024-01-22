package org.odk.collect.android.instrumented.tasks;

import static org.mockito.Mockito.mock;

import android.app.Application;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.StorageUtils;
import org.odk.collect.android.support.rules.RunnableRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.projects.Project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class FormLoaderTaskTest {

    private final StoragePathProvider storagePathProvider = new StoragePathProvider();

    private static final String SECONDARY_INSTANCE_EXTERNAL_CSV_FORM = "external_csv_form.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILE = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILE = "simple-search-external-csv-fruits.db";

    private final FormEntryControllerFactory formEntryControllerFactory = new FormEntryControllerFactory() {
        @Override
        public FormEntryController create(FormDef formDef, File formMediaDir) {
            return new FormEntryController(new FormEntryModel(formDef));
        }
    };

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new RunnableRule(() -> {
                try {
                    // Set up demo project
                    AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
                    component.projectsRepository().save(Project.Companion.getDEMO_PROJECT());
                    component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);

                    StorageUtils.copyFormToDemoProject(SECONDARY_INSTANCE_EXTERNAL_CSV_FORM, Arrays.asList("external_csv_cities.csv", "external_csv_countries.csv", "external_csv_neighbourhoods.csv"), true);
                    StorageUtils.copyFormToDemoProject(SIMPLE_SEARCH_EXTERNAL_CSV_FORM, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILE), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

    // Validate that importing external data multiple times does not fail due to side effects from import
    @Test
    public void loadSearchFromExternalCSVmultipleTimes() throws Exception {
        final String formPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        final Form form = new FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).get().getOneByPath(formPath);
        final Uri formUri = FormsContract.getUri("DEMO", form.getDbId());

        // initial load with side effects
        FormLoaderTask formLoaderTask = new FormLoaderTask(formUri, FormsContract.CONTENT_ITEM_TYPE, null, null, formEntryControllerFactory, mock());
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.executeSynchronously();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File dbFile = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILE);
        Assert.assertTrue(dbFile.exists());
        long dbLastModified = dbFile.lastModified();

        // subsequent load should succeed despite side effects from import
        formLoaderTask = new FormLoaderTask(formUri, FormsContract.CONTENT_ITEM_TYPE, null, null, formEntryControllerFactory, mock());
        wrapper = formLoaderTask.executeSynchronously();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());
        Assert.assertEquals("expected file modification timestamp to be unchanged", dbLastModified, dbFile.lastModified());
    }
}
