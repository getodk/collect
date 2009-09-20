/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FormHandler;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.utilities.FileUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Background task for loading a form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends AsyncTask<String, String, FormHandler> {
    FormLoaderListener mStateListener;


    /**
     * Initialize {@link FormHandler} with {@link FormDef} from binary or from
     * XML. If given an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FormHandler doInBackground(String... path) {

        FormHandler fh = null;
        FormDef fd = null;
        FileInputStream fis = null;

        String formPath = path[0];
        String instancePath = path[1];

        File formXml = new File(formPath);
        File formBin =
                new File(GlobalConstants.CACHE_PATH + FileUtils.getMd5Hash(formXml) + ".formdef");

        if (formBin.exists()) {
            // if we have binary, deserialize binary
            fd = deserializeFormDef(formBin);
            if (fd == null) {
                return null;
            }
        } else {
            // no binary, read from xml
            try {
                fis = new FileInputStream(formXml);
                fd = XFormUtils.getFormFromInputStream(fis);
                if (fd == null) {
                    return null;
                }
                fd.setEvaluationContext(new EvaluationContext());
                fd.initialize(true);
                serializeFormDef(fd, formPath);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // create formhandler from formdef
        fh = new FormHandler(fd);

        // import existing data into formdef
        if (instancePath != null) {
            fh.importData(instancePath);
        }

        // clean up vars
        fis = null;
        fd = null;
        formBin = null;
        formXml = null;
        formPath = null;
        instancePath = null;

        return fh;
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
        JavaRosaServiceProvider.instance().registerPrototypes(GlobalConstants.SERIALIABLE_CLASSES);
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DeserializationException e) {
            e.printStackTrace();
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
        if (FileUtils.createFolder(GlobalConstants.CACHE_PATH)) {

            // calculate unique md5 identifier
            String hash = FileUtils.getMd5Hash(new File(filepath));
            File formDef = new File(GlobalConstants.CACHE_PATH + hash + ".formdef");

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
    protected void onPostExecute(FormHandler fh) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.loadingComplete(fh);
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
}
