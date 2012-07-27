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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.FileUtils;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
    private final static String t = "FormLoaderTask";
    /**
     * Classes needed to serialize objects. Need to put anything from JR in here.
     */
    public final static String[] SERIALIABLE_CLASSES = {
    		"org.javarosa.core.services.locale.ResourceFileDataSource", // JavaRosaCoreModule
    		"org.javarosa.core.services.locale.TableLocaleSource", // JavaRosaCoreModule
            "org.javarosa.core.model.FormDef",
			"org.javarosa.core.model.SubmissionProfile", // CoreModelModule
			"org.javarosa.core.model.QuestionDef", // CoreModelModule
			"org.javarosa.core.model.GroupDef", // CoreModelModule
			"org.javarosa.core.model.instance.FormInstance", // CoreModelModule
			"org.javarosa.core.model.data.BooleanData", // CoreModelModule
			"org.javarosa.core.model.data.DateData", // CoreModelModule
			"org.javarosa.core.model.data.DateTimeData", // CoreModelModule
			"org.javarosa.core.model.data.DecimalData", // CoreModelModule
			"org.javarosa.core.model.data.GeoPointData", // CoreModelModule
			"org.javarosa.core.model.data.IntegerData", // CoreModelModule
			"org.javarosa.core.model.data.LongData", // CoreModelModule
			"org.javarosa.core.model.data.MultiPointerAnswerData", // CoreModelModule
			"org.javarosa.core.model.data.PointerAnswerData", // CoreModelModule
			"org.javarosa.core.model.data.SelectMultiData", // CoreModelModule
			"org.javarosa.core.model.data.SelectOneData", // CoreModelModule
			"org.javarosa.core.model.data.StringData", // CoreModelModule
			"org.javarosa.core.model.data.TimeData", // CoreModelModule
			"org.javarosa.core.model.data.UncastData", // CoreModelModule
			"org.javarosa.core.model.data.helper.BasicDataPointer" // CoreModelModule
    };

    private FormLoaderListener mStateListener;
    private String mErrorMsg;
    private final String mInstancePath;
    private final String mXPath;
    private final String mWaitingXPath;
    private boolean pendingActivityResult = false;
    private int requestCode = 0;
    private int resultCode = 0;
    private Intent intent = null;

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
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
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

        if (formBin.exists()) {
            // if we have binary, deserialize binary
            Log.i(
                t,
                "Attempting to load " + formXml.getName() + " from cached file: "
                        + formBin.getAbsolutePath());
            fd = deserializeFormDef(formBin);
            if (fd == null) {
                // some error occured with deserialization. Remove the file, and make a new .formdef
                // from xml
                Log.w(t,
                    "Deserialization FAILED!  Deleting cache file: " + formBin.getAbsolutePath());
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
            }
        }

        if (mErrorMsg != null) {
            return null;
        }

        // new evaluation context for function handlers
        fd.setEvaluationContext(new EvaluationContext(null));

		// create FormEntryController from formdef
        FormEntryModel fem = new FormEntryModel(fd);
        fec = new FormEntryController(fem);

        boolean usedSavepoint = false;
        
        try {
            // import existing data into formdef
            if (mInstancePath != null) {
            	File instance = new File(mInstancePath);
            	File shadowInstance = SaveToDiskTask.savepointFile(instance);
            	if ( shadowInstance.exists() &&
            		 ( shadowInstance.lastModified() > instance.lastModified()) ) {
            		// the savepoint is newer than the saved value of the instance.
            		// use it.
            		usedSavepoint = true;
            		instance = shadowInstance;
           			Log.w(t,"Loading instance from shadow file: " + shadowInstance.getAbsolutePath());
            	}
            	if ( instance.exists() ) {
	                // This order is important. Import data, then initialize.
	                importData(instance, fec);
	                fd.initialize(false);
            	} else {
            		fd.initialize(true);
            	}
            } else {
                fd.initialize(true);
            }
        } catch (RuntimeException e) {
            mErrorMsg = e.getMessage();
            return null;
        }

        // set paths to /sdcard/odk/forms/formfilename-media/
        String formFileName = formXml.getName().substring(0, formXml.getName().lastIndexOf("."));
        File formMediaDir = new File( formXml.getParent(), formFileName + "-media");
        
        // Remove previous forms
        ReferenceManager._().clearSession();

        // This should get moved to the Application Class
        if (ReferenceManager._().getFactories().length == 0) {
            // this is /sdcard/odk
            ReferenceManager._().addReferenceFactory(
                new FileReferenceFactory(Collect.ODK_ROOT));
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

        FormController fc = new FormController(formMediaDir, fec, mInstancePath == null ? null : new File(mInstancePath));
        if ( mXPath != null ) {
        	// we are resuming after having terminated -- set index to this position...
        	FormIndex idx = fc.getIndexFromXPath(mXPath);
    		fc.jumpToIndex(idx);
        }
        if ( mWaitingXPath != null ) {
        	FormIndex idx = fc.getIndexFromXPath(mWaitingXPath);
        	fc.setIndexWaitingForData(idx);
        }
        data = new FECWrapper(fc, usedSavepoint);
        return data;

    }


    public boolean importData(File instanceFile, FormEntryController fec) {
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
            templateRoot.populate(savedRoot, fec.getModel().getForm());

            // populated model to current form
            fec.getModel().getForm().getInstance().setRoot(templateRoot);

            // fix any language issues
            // : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
            if (fec.getModel().getLanguages() != null) {
                fec.getModel()
                        .getForm()
                        .localeChanged(fec.getModel().getLanguage(),
                            fec.getModel().getForm().getLocalizer());
            }

            return true;

        }
    }


    /**
     * Read serialized {@link FormDef} from file and recreate as object.
     * 
     * @param formDef serialized FormDef file
     * @return {@link FormDef} object
     */
    public FormDef deserializeFormDef(File formDef) {

        // TODO: any way to remove reliance on jrsp?

        // need a list of classes that formdef uses
    	// unfortunately, the JR registerModule() functions do more than this.
    	// register just the classes that would have been registered by:
    	// new JavaRosaCoreModule().registerModule();
    	// new CoreModelModule().registerModule();
    	// replace with direct call to PrototypeManager
    	PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
        new XFormsModule().registerModule();
        
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
     * @param filepath path to the form file
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
    protected void onPostExecute(FECWrapper wrapper) {
        synchronized (this) {
            if (mStateListener != null) {
                if (wrapper == null) {
                    mStateListener.loadingError(mErrorMsg);
                } else {
                    mStateListener.loadingComplete(this);
                }
            }
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

    public FormController getFormController() {
    	return ( data != null ) ? data.getController() : null;
    }
    
    public boolean hasUsedSavepoint() {
    	return (data != null ) ? data.hasUsedSavepoint() : false;
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

}
