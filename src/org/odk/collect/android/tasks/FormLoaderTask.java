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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.provider.FormsStorage;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
    public final static String[] SERIALIABLE_CLASSES =
        {
                "org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
                "org.javarosa.core.model.QuestionDef", "org.javarosa.core.model.data.DateData",
                "org.javarosa.core.model.data.DateTimeData",
                "org.javarosa.core.model.data.DecimalData",
                "org.javarosa.core.model.data.GeoPointData",
                "org.javarosa.core.model.data.helper.BasicDataPointer",
                "org.javarosa.core.model.data.IntegerData",
                "org.javarosa.core.model.data.MultiPointerAnswerData",
                "org.javarosa.core.model.data.PointerAnswerData",
                "org.javarosa.core.model.data.SelectMultiData",
                "org.javarosa.core.model.data.SelectOneData",
                "org.javarosa.core.model.data.StringData", "org.javarosa.core.model.data.TimeData",
                "org.javarosa.core.services.locale.TableLocaleSource",
                "org.javarosa.xpath.expr.XPathArithExpr", "org.javarosa.xpath.expr.XPathBoolExpr",
                "org.javarosa.xpath.expr.XPathCmpExpr", "org.javarosa.xpath.expr.XPathEqExpr",
                "org.javarosa.xpath.expr.XPathFilterExpr", "org.javarosa.xpath.expr.XPathFuncExpr",
                "org.javarosa.xpath.expr.XPathNumericLiteral",
                "org.javarosa.xpath.expr.XPathNumNegExpr", "org.javarosa.xpath.expr.XPathPathExpr",
                "org.javarosa.xpath.expr.XPathStringLiteral",
                "org.javarosa.xpath.expr.XPathUnionExpr",
                "org.javarosa.xpath.expr.XPathVariableReference"
        };

    FormLoaderListener mStateListener;
    String mErrorMsg;

    protected class FECWrapper {
        FormEntryController controller;


        protected FECWrapper(FormEntryController controller) {
            this.controller = controller;
        }


        protected FormEntryController getController() {
            return controller;
        }


        protected void free() {
            controller = null;
        }
    }

    FECWrapper data;


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FECWrapper doInBackground(String... path) {
        FormEntryController fec = null;
        FormDef fd = null;

        String formPath = path[0];
        String instanceDirPath = path[1];
        File instance = null;
        if (instanceDirPath != null) {
            instance = new File(FileUtils.getInstanceFilePath(instanceDirPath));
        }
        if (formPath == null && instanceDirPath != null) {
            String instanceName = instance.getName();
            this.publishProgress(Collect.getInstance().getString(R.string.load_error_no_form,
                instanceName));
            return null;
        }

        Long keyId = null;
        String formMediaPath = null;
        String displayName = null;
        {
            File form = new File(formPath);
            if (!form.exists()) {
                // specified form file does not exist... log error and return
                Log.w(t, "Form file does not exist: " + form.getAbsolutePath());
                return null;
            }
            String[] projection =
                new String[] {
                        FormsStorage.KEY_ID, FormsStorage.KEY_FORM_MEDIA_PATH,
                        FormsStorage.KEY_DISPLAY_NAME
                };

            FilterUtils.FilterCriteria fc =
                FilterUtils.buildSelectionClause(FormsStorage.KEY_FORM_FILE_PATH, form
                        .getAbsolutePath());

            Cursor c =
                Collect.getInstance().getContentResolver().query(
                    FormsStorage.CONTENT_URI_INFO_DATASET, projection, fc.selection,
                    fc.selectionArgs, null);
            if (c != null && c.moveToNext()) {
                // retrieve display name and keyId from FormsStorage provider...
                keyId = c.getLong(c.getColumnIndex(FormsStorage.KEY_ID));
                formMediaPath = c.getString(c.getColumnIndex(FormsStorage.KEY_FORM_MEDIA_PATH));
                displayName = c.getString(c.getColumnIndex(FormsStorage.KEY_DISPLAY_NAME));
            } else {
                // not in the FormsStorage provider -- add it...
                ContentValues v = new ContentValues();
                v.put(FormsStorage.KEY_FORM_FILE_PATH, form.getAbsolutePath());
                Uri uri =
                    Collect.getInstance().getContentResolver().insert(
                        FormsStorage.CONTENT_URI_INFO_DATASET, v);
                // and now fetch what we added...
                c =
                    Collect.getInstance().getContentResolver().query(uri, projection, null, null,
                        null);
                if (c != null && c.moveToNext()) {
                    // we should have gotten something...
                    keyId = c.getLong(c.getColumnIndex(FormsStorage.KEY_ID));
                    formMediaPath = c.getString(c.getColumnIndex(FormsStorage.KEY_FORM_MEDIA_PATH));
                    displayName = c.getString(c.getColumnIndex(FormsStorage.KEY_DISPLAY_NAME));
                } else {
                    // very weird...
                    Log.e(t, "Form record could not be created: " + form.getAbsolutePath());
                    return null;
                }
            }
        }

        // so, at this point, we have the form recorded in the FormsStorage
        // provider. It may or may not have a jrcache file and that file
        // may or may not be readable by our JR library.

        InputStream cacheStream;
        try {
            cacheStream =
                Collect.getInstance().getContentResolver().openInputStream(
                    ContentUris
                            .withAppendedId(FormsStorage.CONTENT_URI_JRCACHE_FILE_DATASET, keyId));
        } catch (FileNotFoundException e) {
            cacheStream = null;
        } catch (Exception e) {
            mErrorMsg = "Failed to load JRCache file: " + e.getMessage();
            e.printStackTrace();
            return null;
        }

        if (cacheStream != null) {
            // if we have binary, deserialize binary
            try {
                Log.i(t, "Attempting to load " + displayName + " from JRCache file");
                fd = deserializeFormDef(cacheStream);
            } catch (Exception e) {
                // didn't load --
                // the common case here is that the javarosa library that
                // serialized the binary is incompatible with the javarosa
                // library that is now attempting to deserialize it.
            }

            if (fd == null) {
                // some error occured with deserialization. Remove the file, and make a new .formdef
                // from xml
                Log.w(t, "Deserialization FAILED!  Deleting cache file for: " + displayName);
                deleteJRCacheFile(keyId, displayName);
            }
        }

        if (fd == null) {
            // no binary, read from xml
            Log.i(t, "Attempting to load from xml file for: " + displayName);
            InputStream formStream;
            try {
                formStream =
                    Collect.getInstance().getContentResolver().openInputStream(
                        ContentUris.withAppendedId(FormsStorage.CONTENT_URI_FORM_FILE_DATASET,
                            keyId));
            } catch (Exception e) {
                mErrorMsg = "Failed to load xml file: " + e.getMessage();
                e.printStackTrace();
                return null;
            }

            // we have the file stream...
            try {
                fd = XFormUtils.getFormFromInputStream(formStream);
                if (fd == null) {
                    mErrorMsg = "Error reading XForm file";
                } else {
                    // todo -- update to write out JRCache file...
                    serializeFormDef(fd, formPath);
                }
            } catch (XFormParseException e) {
                mErrorMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
                mErrorMsg = e.getMessage();
                e.printStackTrace();
            } finally {
                if (fd == null) {
                    // couldn't load the form --
                    // remove cache reference from file db if it exists
                    deleteJRCacheFile(keyId, displayName);
                    return null;
                }
            }
        }

        // new evaluation context for function handlers
        EvaluationContext ec = new EvaluationContext();
        fd.setEvaluationContext(ec);

        // create FormEntryController from formdef
        FormEntryModel fem = new FormEntryModel(fd);
        fec = new FormEntryController(fem);

        try {
            // import existing data into formdef
            if (instance != null) {
                // This order is important. Import data, then initialize.
                importData(instance.getAbsolutePath(), fec);
                fd.initialize(false);
            } else {
                fd.initialize(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.publishProgress(Collect.getInstance().getString(R.string.load_error, displayName)
                    + " : " + e.getMessage());
            return null;
        }

        Collect.getInstance().registerMediaPath(formMediaPath);

        // clean up vars
        fd = null;
        formPath = null;
        instanceDirPath = null;

        data = new FECWrapper(fec);
        return data;

    }


    private void deleteJRCacheFile(Long keyId, String displayName) {
        try {
            Collect.getInstance().getContentResolver().delete(
                ContentUris.withAppendedId(FormsStorage.CONTENT_URI_JRCACHE_FILE_DATASET, keyId),
                null, null);
            Log.i(t, "JRCache file for: " + displayName + " removed from database");
        } catch (Exception e) {
            Log.i(t, "Failed to remove JRCache file for: " + displayName
                    + " from database (might not have existed...)");
        }
    }


    public boolean importData(String filePath, FormEntryController fec) {
        // convert files into a byte array
        byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));

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
                fec.getModel().getForm().localeChanged(fec.getModel().getLanguage(),
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
    public FormDef deserializeFormDef(InputStream jrcacheFileStream) {

        // need a list of classes that formdef uses
        PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
        FormDef fd = null;
        DataInputStream dis = null;
        try {
            // create new form def
            fd = new FormDef();
            dis = new DataInputStream(jrcacheFileStream);

            // read serialized formdef into new formdef
            fd.readExternal(dis, ExtUtil.defaultPrototypes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fd = null;
        } catch (IOException e) {
            e.printStackTrace();
            fd = null;
        } catch (DeserializationException e) {
            e.printStackTrace();
            fd = null;
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    // ignore...
                }
            }
        }

        return fd;
    }


    /**
     * Write the FormDef to the file system as a binary blog.
     * 
     * @param filepath path to the form file
     */
    public void serializeFormDef(FormDef fd, String filepath) {
        // if cache folder is missing, create it.
        if (FileUtils.createFolder(FileUtils.CACHE_PATH)) {

            // calculate unique md5 identifier
            String hash = FileUtils.getMd5Hash(new File(filepath));
            File formDef = new File(FileUtils.CACHE_PATH + hash + ".formdef");

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
    }


    @Override
    protected void onProgressUpdate(String... values) {
        Toast.makeText(Collect.getInstance().getApplicationContext(), values[0], Toast.LENGTH_LONG)
                .show();
    }


    @Override
    protected void onPostExecute(FECWrapper wrapper) {
        synchronized (this) {
            if (mStateListener != null) {
                if (wrapper == null) {
                    mStateListener.loadingError(mErrorMsg);
                } else {
                    mStateListener.loadingComplete(wrapper.getController());
                }
            }
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }


    public void destroy() {
        if (data != null) {
            data.free();
            data = null;
        }
    }

}
