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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.external.ExternalAnswerResolver;
import org.odk.collect.android.external.ExternalDataHandler;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.ExternalDataManagerImpl;
import org.odk.collect.android.external.ExternalDataReader;
import org.odk.collect.android.external.ExternalDataReaderImpl;
import org.odk.collect.android.external.handler.ExternalDataHandlerPull;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ZipUtils;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
  private final static String t = "FormLoaderTask";
  private static final String ITEMSETS_CSV = "itemsets.csv";

  private FormLoaderListener mStateListener;
  private String mErrorMsg;
  private String mInstancePath;
  private final String mXPath;
  private final String mWaitingXPath;
  private boolean pendingActivityResult = false;
  private int requestCode = 0;
  private int resultCode = 0;
  private Intent intent = null;
  private ExternalDataManager externalDataManager;

  protected class FECWrapper {
    FormController controller;
    boolean usedSavepoint;

    protected FECWrapper(FormController controller, boolean usedSavepoint) {
      this.controller = controller;
      this.usedSavepoint = usedSavepoint;
    }

    protected FormController getController() {
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

  public FormLoaderTask(String instancePath, String XPath, String waitingXPath) {
    mInstancePath = instancePath;
    mXPath = XPath;
    mWaitingXPath = waitingXPath;
  }

  /**
   * Initialize {@link FormEntryController} with {@link FormDef} from binary or
   * from XML. If given an instance, it will be used to fill the {@link FormDef}
   * .
   */
  @Override
  protected FECWrapper doInBackground(String... path) {
    FormEntryController fec = null;
    FormDef fd = null;
    FileInputStream fis = null;
    mErrorMsg = null;

    String formPath = path[0];

    File formXml = new File(formPath);
    String formHash = FileUtils.getMd5Hash(formXml);
    File formBin = new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");

    publishProgress(Collect.getInstance().getString(R.string.survey_loading_reading_form_message));

    FormDef.EvalBehavior mode = AdminPreferencesActivity.getConfiguredFormProcessingLogic(Collect.getInstance());
    FormDef.setEvalBehavior(mode);

//    FormDef.setDefaultEventNotifier(new EventNotifier() {
//
//      @Override
//      public void publishEvent(Event event) {
//        Log.d("FormDef", event.asLogLine());
//      }
//    });

    if (formBin.exists()) {
      // if we have binary, deserialize binary
      Log.i(
          t,
          "Attempting to load " + formXml.getName() + " from cached file: "
              + formBin.getAbsolutePath());
      fd = deserializeFormDef(formBin);
      if (fd == null) {
        // some error occured with deserialization. Remove the file, and make a
        // new .formdef
        // from xml
        Log.w(t, "Deserialization FAILED!  Deleting cache file: " + formBin.getAbsolutePath());
        formBin.delete();
      }
    }
    if (fd == null) {
      // no binary, read from xml
      try {
        Log.i(t, "Attempting to load from: " + formXml.getAbsolutePath());
        fis = new FileInputStream(formXml);
        fd = XFormUtils.getFormFromInputStream(fis);
        if (fd == null) {
          mErrorMsg = "Error reading XForm file";
        } else {
          serializeFormDef(fd, formPath);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        mErrorMsg = e.getMessage();
      } catch (XFormParseException e) {
        mErrorMsg = e.getMessage();
        e.printStackTrace();
      } catch (Exception e) {
        mErrorMsg = e.getMessage();
        e.printStackTrace();
      } finally {
        IOUtils.closeQuietly(fis);
      }
    }

    if (mErrorMsg != null || fd == null) {
      return null;
    }

    // set paths to /sdcard/odk/forms/formfilename-media/
    String formFileName = formXml.getName().substring(0, formXml.getName().lastIndexOf("."));
    File formMediaDir = new File(formXml.getParent(), formFileName + "-media");

    externalDataManager = new ExternalDataManagerImpl(formMediaDir);

    // add external data function handlers
    ExternalDataHandler externalDataHandlerPull = new ExternalDataHandlerPull(externalDataManager);
    fd.getEvaluationContext().addFunctionHandler(externalDataHandlerPull);

    try {
      loadExternalData(formMediaDir);
    } catch (Exception e) {
      mErrorMsg = e.getMessage();
      e.printStackTrace();
      return null;
    }

    if (isCancelled()) {
      // that means that the user has cancelled, so no need to go further
      return null;
    }

    // create FormEntryController from formdef
    FormEntryModel fem = new FormEntryModel(fd);
    fec = new FormEntryController(fem);

    boolean usedSavepoint = false;

    try {
      // import existing data into formdef
      if (mInstancePath != null) {
        File instance = new File(mInstancePath);
        File shadowInstance = SaveToDiskTask.savepointFile(instance);
        if (shadowInstance.exists() && (shadowInstance.lastModified() > instance.lastModified())) {
          // the savepoint is newer than the saved value of the instance.
          // use it.
          usedSavepoint = true;
          instance = shadowInstance;
          Log.w(t, "Loading instance from shadow file: " + shadowInstance.getAbsolutePath());
        }
        if (instance.exists()) {
          // This order is important. Import data, then initialize.
          try {
            importData(instance, fec);
            fd.initialize(false, new InstanceInitializationFactory());
          } catch (RuntimeException e) {
            Log.e(t, e.getMessage(), e);

            // SCTO-633
            if (usedSavepoint && !(e.getCause() instanceof XPathTypeMismatchException)) {
              // this means that the .save file is corrupted or 0-sized, so
              // don't use it.
              usedSavepoint = false;
              mInstancePath = null;
              fd.initialize(true, new InstanceInitializationFactory());
            } else {
              // this means that the saved instance is corrupted.
              throw e;
            }
          }
        } else {
          fd.initialize(true, new InstanceInitializationFactory());
        }
      } else {
        fd.initialize(true, new InstanceInitializationFactory());
      }
    } catch (RuntimeException e) {
      Log.e(t, e.getMessage(), e);
      if (e.getCause() instanceof XPathTypeMismatchException) {
        // this is a case of
        // https://bitbucket.org/m.sundt/javarosa/commits/e5d344783e7968877402bcee11828fa55fac69de
        // the data are imported, the survey will be unusable
        // but we should give the option to the user to edit the form
        // otherwise the survey will be TOTALLY inaccessible.
        Log.w(
            t,
            "We have a syntactically correct instance, but the data threw an exception inside JR. We should allow editing.");
      } else {
        mErrorMsg = e.getMessage();
        return null;
      }
    }

    // Remove previous forms
    ReferenceManager._().clearSession();

    // for itemsets.csv, we only check to see if the itemset file has been
    // updated
    File csv = new File(formMediaDir.getAbsolutePath() + "/" + ITEMSETS_CSV);
    String csvmd5 = null;
    if (csv.exists()) {
      csvmd5 = FileUtils.getMd5Hash(csv);
      boolean readFile = false;
      ItemsetDbAdapter ida = new ItemsetDbAdapter();
      ida.open();
      // get the database entry (if exists) for this itemsets.csv, based
      // on the path
      Cursor c = ida.getItemsets(csv.getAbsolutePath());
      if (c != null) {
        if (c.getCount() == 1) {
          c.moveToFirst(); // should be only one, ever, if any
          String oldmd5 = c.getString(c.getColumnIndex("hash"));
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

    // This should get moved to the Application Class
    if (ReferenceManager._().getFactories().length == 0) {
      // this is /sdcard/odk
      ReferenceManager._().addReferenceFactory(new FileReferenceFactory(Collect.ODK_ROOT));
    }

    // Set jr://... to point to /sdcard/odk/forms/filename-media/
    ReferenceManager._().addSessionRootTranslator(
        new RootTranslator("jr://images/", "jr://file/forms/" + formFileName + "-media/"));
    ReferenceManager._().addSessionRootTranslator(
        new RootTranslator("jr://image/", "jr://file/forms/" + formFileName + "-media/"));
    ReferenceManager._().addSessionRootTranslator(
        new RootTranslator("jr://audio/", "jr://file/forms/" + formFileName + "-media/"));
    ReferenceManager._().addSessionRootTranslator(
        new RootTranslator("jr://video/", "jr://file/forms/" + formFileName + "-media/"));

    // clean up vars
    fis = null;
    fd = null;
    formBin = null;
    formXml = null;
    formPath = null;

    FormController fc = new FormController(formMediaDir, fec, mInstancePath == null ? null
        : new File(mInstancePath));
    if (mXPath != null) {
      // we are resuming after having terminated -- set index to this
      // position...
      FormIndex idx = fc.getIndexFromXPath(mXPath);
      fc.jumpToIndex(idx);
    }
    if (mWaitingXPath != null) {
      FormIndex idx = fc.getIndexFromXPath(mWaitingXPath);
      fc.setIndexWaitingForData(idx);
    }
    data = new FECWrapper(fc, usedSavepoint);
    return data;

  }

  @SuppressWarnings("unchecked")
  private void loadExternalData(File mediaFolder) {
    // SCTO-594
    File[] zipFiles = mediaFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().toLowerCase().endsWith(".zip");
      }
    });

    if (zipFiles != null) {
      ZipUtils.unzip(zipFiles);
      for (File zipFile : zipFiles) {
        boolean deleted = zipFile.delete();
        if (!deleted) {
          Log.w(t, "Cannot delete " + zipFile + ". It will be re-unzipped next time. :(");
        }
      }
    }

    File[] csvFiles = mediaFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        String lowerCaseName = file.getName().toLowerCase();
        return lowerCaseName.endsWith(".csv") && !lowerCaseName.equalsIgnoreCase(ITEMSETS_CSV);
      }
    });

    Map<String, File> externalDataMap = new HashMap<String, File>();

    if (csvFiles != null) {

      for (File csvFile : csvFiles) {
        String dataSetName = csvFile.getName().substring(0, csvFile.getName().lastIndexOf("."));
        externalDataMap.put(dataSetName, csvFile);
      }

      if (externalDataMap.size() > 0) {

        publishProgress(Collect.getInstance()
            .getString(R.string.survey_loading_reading_csv_message));

        ExternalDataReader externalDataReader = new ExternalDataReaderImpl(this);
        externalDataReader.doImport(externalDataMap);
      }
    }
  }

  public void publishExternalDataLoadingProgress(String message) {
    publishProgress(message);
  }

  @Override
  protected void onProgressUpdate(String... values) {
    synchronized (this) {
      if (mStateListener != null && values != null) {
        if (values.length == 1) {
          mStateListener.onProgressStep(values[0]);
        }
      }
    }
  }

  public boolean importData(File instanceFile, FormEntryController fec) {
    publishProgress(Collect.getInstance().getString(R.string.survey_loading_reading_data_message));

    // convert files into a byte array
    byte[] fileBytes = FileUtils.getFileAsBytes(instanceFile);

    // get the root of the saved and template instances
    TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
    TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

    // weak check for matching forms
    if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
      Log.e(t, "Saved form instance does not match template form definition");
      return false;
    } else {
      // populate the data model
      TreeReference tr = TreeReference.rootRef();
      tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);

      // Here we set the Collect's implementation of the IAnswerResolver.
      // We set it back to the default after select choices have been populated.
      XFormParser.setAnswerResolver(new ExternalAnswerResolver());
      templateRoot.populate(savedRoot, fec.getModel().getForm());
      XFormParser.setAnswerResolver(new DefaultAnswerResolver());

      // populated model to current form
      fec.getModel().getForm().getInstance().setRoot(templateRoot);

      // fix any language issues
      // :
      // http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
      if (fec.getModel().getLanguages() != null) {
        fec.getModel().getForm()
            .localeChanged(fec.getModel().getLanguage(), fec.getModel().getForm().getLocalizer());
      }

      return true;

    }
  }

  /**
   * Read serialized {@link FormDef} from file and recreate as object.
   *
   * @param formDef
   *          serialized FormDef file
   * @return {@link FormDef} object
   */
  public FormDef deserializeFormDef(File formDef) {

    // TODO: any way to remove reliance on jrsp?
    FileInputStream fis = null;
    FormDef fd = null;
    try {
      // create new form def
      fd = new FormDef();
      fis = new FileInputStream(formDef);
      DataInputStream dis = new DataInputStream(fis);

      // read serialized formdef into new formdef
      fd.readExternal(dis, ExtUtil.defaultPrototypes());
      dis.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fd = null;
    } catch (IOException e) {
      e.printStackTrace();
      fd = null;
    } catch (DeserializationException e) {
      e.printStackTrace();
      fd = null;
    } catch (Exception e) {
      e.printStackTrace();
      fd = null;
    }

    return fd;
  }

  /**
   * Write the FormDef to the file system as a binary blog.
   *
   * @param filepath
   *          path to the form file
   */
  public void serializeFormDef(FormDef fd, String filepath) {
    // calculate unique md5 identifier
    String hash = FileUtils.getMd5Hash(new File(filepath));
    File formDef = new File(Collect.CACHE_PATH + File.separator + hash + ".formdef");

    // formdef does not exist, create one.
    if (!formDef.exists()) {
      FileOutputStream fos;
      try {
        fos = new FileOutputStream(formDef);
        DataOutputStream dos = new DataOutputStream(fos);
        fd.writeExternal(dos);
        dos.flush();
        dos.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();

    if (externalDataManager != null) {
      externalDataManager.close();
    }
  }

  @Override
  protected void onPostExecute(FECWrapper wrapper) {
    synchronized (this) {
      try {
        if (mStateListener != null) {
          if (wrapper == null) {
            mStateListener.loadingError(mErrorMsg);
          } else {
            mStateListener.loadingComplete(this);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setFormLoaderListener(FormLoaderListener sl) {
    synchronized (this) {
      mStateListener = sl;
    }
  }

  public FormController getFormController() {
    return (data != null) ? data.getController() : null;
  }

  public ExternalDataManager getExternalDataManager() {
    return externalDataManager;
  }

  public boolean hasUsedSavepoint() {
    return (data != null) ? data.hasUsedSavepoint() : false;
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( withinTransaction ) {
                ida.commit();
            }
            ida.close();
        }
    }
}
