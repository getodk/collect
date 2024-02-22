/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.tasks;

import static org.odk.collect.android.utilities.FormUtils.setupReferenceManagerForForm;
import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dynamicpreload.ExternalAnswerResolver;
import org.odk.collect.android.dynamicpreload.ExternalDataManager;
import org.odk.collect.android.dynamicpreload.ExternalDataUseCases;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.JavaRosaFormController;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.ExternalizableFormDefCache;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.ZipUtils;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.SchedulerAsyncTaskMimic;
import org.odk.collect.entities.EntitiesRepository;
import org.odk.collect.entities.Entity;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.savepoints.Savepoint;
import org.odk.collect.forms.savepoints.SavepointsRepository;
import org.odk.collect.shared.strings.Md5;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import timber.log.Timber;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends SchedulerAsyncTaskMimic<Void, String, FormLoaderTask.FECWrapper> {
    private static final String ITEMSETS_CSV = "itemsets.csv";

    private FormLoaderListener stateListener;
    private String errorMsg;
    private String warningMsg;
    private String instancePath;
    private final Uri uri;
    private final String uriMimeType;
    private final String xpath;
    private final String waitingXPath;
    private FormEntryControllerFactory formEntryControllerFactory;
    private final EntitiesRepository entitiesRepository;
    private boolean pendingActivityResult;
    private int requestCode;
    private int resultCode;
    private Intent intent;
    private ExternalDataManager externalDataManager;
    private FormDef formDef;
    private Form form;
    private Instance instance;
    private Savepoint savepoint;
    private final SavepointsRepository savepointsRepository;

    @Override
    protected void onPreExecute() {

    }

    public String getInstancePath() {
        return instancePath;
    }

    public Form getForm() {
        return form;
    }

    public Instance getInstance() {
        return instance;
    }

    public static class FECWrapper {
        FormController controller;
        boolean usedSavepoint;

        protected FECWrapper(FormController controller, boolean usedSavepoint) {
            this.controller = controller;
            this.usedSavepoint = usedSavepoint;
        }

        public FormController getController() {
            return controller;
        }

        protected boolean hasUsedSavepoint() {
            return usedSavepoint;
        }

        protected void free() {
            controller = null;
        }
    }

    FECWrapper data;

    public FormLoaderTask(Uri uri, String uriMimeType, String xpath, String waitingXPath, FormEntryControllerFactory formEntryControllerFactory, Scheduler scheduler, EntitiesRepository entitiesRepository, SavepointsRepository savepointsRepository) {
        super(scheduler);
        this.uri = uri;
        this.uriMimeType = uriMimeType;
        this.xpath = xpath;
        this.waitingXPath = waitingXPath;
        this.formEntryControllerFactory = formEntryControllerFactory;
        this.entitiesRepository = entitiesRepository;
        this.savepointsRepository = savepointsRepository;
    }

    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or
     * from XML. If given an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FECWrapper doInBackground(Void... ignored) {
        errorMsg = null;

        if (uriMimeType != null && uriMimeType.equals(InstancesContract.CONTENT_ITEM_TYPE)) {
            instance = new InstancesRepositoryProvider(Collect.getInstance()).get().get(ContentUriHelper.getIdFromUri(uri));
            instancePath = instance.getInstanceFilePath();

            List<Form> candidateForms = new FormsRepositoryProvider(Collect.getInstance()).get().getAllByFormIdAndVersion(instance.getFormId(), instance.getFormVersion());

            form = candidateForms.get(0);
            savepoint = savepointsRepository.get(form.getDbId(), instance.getDbId());
        } else if (uriMimeType != null && uriMimeType.equals(FormsContract.CONTENT_ITEM_TYPE)) {
            form = new FormsRepositoryProvider(Collect.getInstance()).get().get(ContentUriHelper.getIdFromUri(uri));
            if (form == null) {
                Timber.e(new Error("form is null"));
                errorMsg = "This form no longer exists, please email support@getodk.org with a description of what you were doing when this happened.";
                return null;
            }

            savepoint = savepointsRepository.get(form.getDbId(), null);
            instancePath = savepoint != null ? savepoint.getInstanceFilePath() : null;
        }

        if (form.getFormFilePath() == null) {
            Timber.e(new Error("formPath is null"));
            errorMsg = "formPath is null, please email support@getodk.org with a description of what you were doing when this happened.";
            return null;
        }

        final File formXml = new File(form.getFormFilePath());
        final File formMediaDir = FileUtils.getFormMediaDir(formXml);

        unzipMediaFiles(formMediaDir);
        setupReferenceManagerForForm(ReferenceManager.instance(), formMediaDir);

        FormDef formDef = null;
        try {
            formDef = createFormDefFromCacheOrXml(form.getFormFilePath(), formXml);
        } catch (StackOverflowError e) {
            Timber.e(e);
            errorMsg = getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.too_complex_form);
        } catch (Exception | Error e) {
            Timber.w(e);
            errorMsg = "An unknown error has occurred. Please ask your project leadership to email support@getodk.org with information about this form.";
            errorMsg += "\n\n" + e.getMessage();
        }

        if (errorMsg != null || formDef == null) {
            Timber.w("No exception loading form but errorMsg set");
            return null;
        }

        externalDataManager = Collect.getInstance().getExternalDataManager();

        try {
            ExternalDataUseCases.create(formDef, formMediaDir, this::isCancelled, progress -> {
                publishProgress(progress.apply(Collect.getInstance().getResources()));
            });
        } catch (Exception e) {
            Timber.e(e, "Exception thrown while loading external data");
            errorMsg = e.getMessage();
            return null;
        }

        if (isCancelled()) {
            // that means that the user has cancelled, so no need to go further
            return null;
        }

        // create FormEntryController from formdef
        final FormEntryController fec = formEntryControllerFactory.create(formDef, formMediaDir);

        boolean usedSavepoint = false;

        addOfflineEntititesToSecondaryInstances(fec);

        try {
            Timber.i("Initializing form.");
            final long start = System.currentTimeMillis();
            usedSavepoint = initializeForm(formDef, fec);
            Timber.i("Form initialized in %.3f seconds.", (System.currentTimeMillis() - start) / 1000F);
        } catch (IOException | RuntimeException e) {
            Timber.e(e);
            if (e.getCause() instanceof XPathTypeMismatchException) {
                // this is a case of
                // https://bitbucket.org/m
                // .sundt/javarosa/commits/e5d344783e7968877402bcee11828fa55fac69de
                // the data are imported, the survey will be unusable
                // but we should give the option to the user to edit the form
                // otherwise the survey will be TOTALLY inaccessible.
                Timber.w("We have a syntactically correct instance, but the data threw an exception inside JR. We should allow editing.");
            } else {
                errorMsg = e.getMessage();
                return null;
            }
        }

        processItemSets(formMediaDir);

        final FormController fc = new JavaRosaFormController(formMediaDir, fec, instancePath == null ? null
                : new File(instancePath));
        if (xpath != null) {
            // we are resuming after having terminated -- set index to this
            // position...
            FormIndex idx = fc.getIndexFromXPath(xpath);
            if (idx != null) {
                fc.jumpToIndex(idx);
            }
        }
        if (waitingXPath != null) {
            FormIndex idx = fc.getIndexFromXPath(waitingXPath);
            if (idx != null) {
                fc.setIndexWaitingForData(idx);
            }
        }
        data = new FECWrapper(fc, usedSavepoint);
        return data;
    }

    private void addOfflineEntititesToSecondaryInstances(FormEntryController fec) {
        Enumeration<DataInstance> nonMainInstances = fec.getModel().getForm().getNonMainInstances();
        List<DataInstance> entityListInstances = Collections.list(nonMainInstances)
                .stream()
                .filter((instance) -> entitiesRepository.getDatasets().contains(instance.getName()))
                .collect(Collectors.toList());

        entityListInstances.stream().forEach((instance) -> {
            TreeElement root = (TreeElement) instance.getRoot();
            int startingMultiplicity = root.getNumChildren();

            List<Entity> entities = entitiesRepository.getEntities(instance.getName());
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                TreeElement name = new TreeElement("name");
                name.setValue(new StringData(entity.getId()));

                TreeElement label = new TreeElement("label");
                label.setValue(new StringData(entity.getLabel()));

                TreeElement item = new TreeElement("item", startingMultiplicity + i);
                item.addChild(name);
                item.addChild(label);

                root.addChild(item);
            }
        });
    }

    private static void unzipMediaFiles(File formMediaDir) {
        File[] zipFiles = formMediaDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase(Locale.US).endsWith(".zip");
            }
        });

        if (zipFiles != null) {
            ZipUtils.unzip(zipFiles);
            for (File zipFile : zipFiles) {
                boolean deleted = zipFile.delete();
                if (!deleted) {
                    Timber.w("Cannot delete %s. It will be re-unzipped next time. :(", zipFile.toString());
                }
            }
        }
    }

    private FormDef createFormDefFromCacheOrXml(String formPath, File formXml) throws XFormParser.ParseException {
        publishProgress(
                getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_loading_reading_form_message));

        final FormDef formDefFromCache = new ExternalizableFormDefCache().readCache(formXml);
        if (formDefFromCache != null) {
            return formDefFromCache;
        }

        // no binary, read from xml
        Timber.i("Attempting to load from: %s", formXml.getAbsolutePath());
        final long start = System.currentTimeMillis();
        String lastSavedSrc = FileUtils.getOrCreateLastSavedSrc(formXml);
        FormDef formDefFromXml = XFormUtils.getFormFromFormXml(formPath, lastSavedSrc);
        if (formDefFromXml == null) {
            Timber.w("Error reading XForm file");
            errorMsg = "Error reading XForm file";
        } else {
            Timber.i("Loaded in %.3f seconds.",
                    (System.currentTimeMillis() - start) / 1000F);
            formDef = formDefFromXml;

            try {
                new ExternalizableFormDefCache().writeCache(formDef, formXml.getPath());
            } catch (IOException e) {
                Timber.e(e);
            }

            return formDefFromXml;
        }

        return null;
    }

    private void processItemSets(File formMediaDir) {
        // for itemsets.csv, we only check to see if the itemset file has been
        // updated
        final File csv = new File(formMediaDir.getAbsolutePath() + "/" + ITEMSETS_CSV);
        String csvmd5;
        if (csv.exists()) {
            csvmd5 = Md5.getMd5Hash(csv);
            boolean readFile = false;
            final ItemsetDbAdapter ida = new ItemsetDbAdapter();
            ida.open();
            // get the database entry (if exists) for this itemsets.csv, based
            // on the path
            final Cursor c = ida.getItemsets(csv.getAbsolutePath());
            if (c != null) {
                if (c.getCount() == 1) {
                    c.moveToFirst(); // should be only one, ever, if any
                    final String oldmd5 = c.getString(c.getColumnIndex("hash"));
                    if (oldmd5.equals(csvmd5)) {
                        // they're equal, do nothing
                    } else {
                        // the csv has been updated, delete the old entries
                        ida.dropTable(ItemsetDbAdapter.getMd5FromString(csv.getAbsolutePath()),
                                csv.getAbsolutePath());
                        // and read the new
                        readFile = true;
                    }
                } else {
                    // new csv, add it
                    readFile = true;
                }
                c.close();
            }
            ida.close();
            if (readFile) {
                readCSV(csv, csvmd5, ItemsetDbAdapter.getMd5FromString(csv.getAbsolutePath()));
            }
        }
    }

    private boolean initializeForm(FormDef formDef, FormEntryController fec) throws IOException {
        final InstanceInitializationFactory instanceInit = new InstanceInitializationFactory();
        boolean usedSavepoint = false;

        if (instancePath != null) {
            File instanceXml = new File(instancePath);

            if (savepoint != null) {
                final File savepointFile = new File(savepoint.getSavepointFilePath());
                usedSavepoint = true;
                instanceXml = savepointFile;
                Timber.w("Loading instance from savepoint file: %s", savepointFile.getAbsolutePath());
            }

            if (instanceXml.exists()) {
                // This order is important. Import data, then initialize.
                try {
                    Timber.i("Importing data");
                    publishProgress(getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_loading_reading_data_message));
                    importData(instanceXml, fec);
                    formDef.initialize(false, instanceInit);
                } catch (IOException | RuntimeException e) {
                    // Skip a savepoint file that is corrupted or 0-sized
                    if (usedSavepoint && !(e.getCause() instanceof XPathTypeMismatchException)) {
                        usedSavepoint = false;
                        instancePath = null;
                        formDef.initialize(true, instanceInit);
                        Timber.e(e, "Bad savepoint");
                    } else {
                        // The saved instance is corrupted.
                        Timber.e(e, "Corrupt saved instance");
                        throw new RuntimeException("An unknown error has occurred. Please ask your project leadership to email support@getodk.org with information about this form."
                            + "\n\n" + e.getMessage());
                    }
                }
            } else {
                formDef.initialize(true, instanceInit);
            }
        } else {
            formDef.initialize(true, instanceInit);
        }
        return usedSavepoint;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (stateListener != null && values != null) {
                if (values.length == 1) {
                    stateListener.onProgressStep(values[0]);
                }
            }
        }
    }

    // Copied from XFormParser.loadXmlInstance in order to set ExternalAnswerResolver for search()
    public static void importData(File instanceFile, FormEntryController fec) throws IOException, RuntimeException {
        // convert files into a byte array
        byte[] fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(instanceFile);

        // get the root of the saved and template instances
        TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

        // weak check for matching forms
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            Timber.e(new Error("Saved form instance does not match template form definition"));
            return;
        }

        // populate the data model
        TreeReference tr = TreeReference.rootRef();
        tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);

        // Here we set the Collect's implementation of the IAnswerResolver.
        // We set it back to the default after select choices have been populated.
        XFormParser.setAnswerResolver(new ExternalAnswerResolver());
        templateRoot.populate(savedRoot, fec.getModel().getForm());
        XFormParser.setAnswerResolver(new DefaultAnswerResolver());

        // FormInstanceParser.parseInstance is responsible for initial creation of instances. It explicitly sets the
        // main instance name to null so we force this again on deserialization because some code paths rely on the main
        // instance not having a name. Must be before the call on setRoot because setRoot also sets the root's name.
        fec.getModel().getForm().getInstance().setName(null);

        // populated model to current form
        fec.getModel().getForm().getInstance().setRoot(templateRoot);

        // fix any language issues
        // :
        // http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
        if (fec.getModel().getLanguages() != null) {
            fec.getModel().getForm()
                    .localeChanged(fec.getModel().getLanguage(),
                            fec.getModel().getForm().getLocalizer());
        }
        Timber.i("Done importing data");
    }

    @Override
    protected void onCancelled() {
        if (externalDataManager != null) {
            externalDataManager.close();
        }
    }

    @Override
    protected void onPostExecute(FECWrapper wrapper) {
        synchronized (this) {
            try {
                if (stateListener != null) {
                    if (wrapper == null) {
                        stateListener.loadingError(errorMsg);
                    } else {
                        stateListener.loadingComplete(this, formDef, warningMsg);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    public FormController getFormController() {
        return (data != null) ? data.getController() : null;
    }

    public boolean hasUsedSavepoint() {
        return (data != null) && data.hasUsedSavepoint();
    }

    public void destroy() {
        if (data != null) {
            data.free();
            data = null;
        }
    }

    public boolean hasPendingActivityResult() {
        return pendingActivityResult;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setActivityResult(int requestCode, int resultCode, Intent intent) {
        this.pendingActivityResult = true;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.intent = intent;
    }

    private void readCSV(File csv, String formHash, String pathHash) {

        CSVReader reader;
        ItemsetDbAdapter ida = new ItemsetDbAdapter();
        ida.open();
        boolean withinTransaction = false;

        try {
            reader = new CSVReader(new FileReader(csv));

            String[] nextLine;
            String[] columnHeaders = null;
            int lineNumber = 0;
            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    // first line of csv is column headers
                    columnHeaders = nextLine;
                    ida.createTable(formHash, pathHash, columnHeaders,
                            csv.getAbsolutePath());
                    continue;
                }
                // add the rest of the lines to the specified database
                // nextLine[] is an array of values from the line
                // System.out.println(nextLine[4] + "etc...");
                if (lineNumber == 2) {
                    // start a transaction for the inserts
                    withinTransaction = true;
                    ida.beginTransaction();
                }
                ida.addRow(pathHash, columnHeaders, nextLine);

            }
        } catch (IOException | SQLException | CsvValidationException e) {
            warningMsg = e.getMessage();
        } finally {
            if (withinTransaction) {
                ida.commit();
            }
            ida.close();
        }
    }

    public FormDef getFormDef() {
        return formDef;
    }

    public interface FormEntryControllerFactory {
        FormEntryController create(@NonNull FormDef formDef, @NonNull File formMediaDir);
    }
}
