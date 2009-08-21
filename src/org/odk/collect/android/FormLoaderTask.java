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

package org.odk.collect.android;

import android.os.AsyncTask;
import android.util.Log;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xpath.XPathParseTool;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Background task for loading a form. Eventually we're moving this to a service
 * so that creating the formdef object happens automatically in order to
 * dramatically speed form loading.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 
 */
class FormLoaderTask extends AsyncTask<String, String, FormHandler> {
    FormLoaderListener mStateListener;


    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected FormHandler doInBackground(String... path) {

        FormHandler fh = null;
        FormDef form = null;
        FileInputStream fis = null;
        String instancePath = path[1];

        File fx = new File(path[0]);
        String sx = FileUtils.getMd5Hash(fx);
        File fd = new File(SharedConstants.TMP_PATH + sx + ".formdef");
        if (fd.exists()) {
            form = deserializeFormDef(fd);
        } else {
            try {
                fis = new FileInputStream(fx);
                form = XFormUtils.getFormFromInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        if (form == null) {
            return null;
        }

        fh = new FormHandler(form);
        fh.initialize();
        fis = null;
        form = null;

        if (instancePath != null) {
            fh.importData(instancePath);
        }

        return fh;
    }



    public FormDef deserializeFormDef(File formdef) {

        // need a list of classes that formdef uses
        String[] classes =
                {"org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
                        "org.javarosa.core.model.QuestionDef",
                        "org.javarosa.core.model.data.DateData",
                        "org.javarosa.core.model.data.DateTimeData",
                        "org.javarosa.core.model.data.DecimalData",
                        "org.javarosa.core.model.data.GeoPointData",
                        "org.javarosa.core.model.data.helper.BasicDataPointer",
                        "org.javarosa.core.model.data.IntegerData",
                        "org.javarosa.core.model.data.MultiPointerAnswerData",
                        "org.javarosa.core.model.data.PointerAnswerData",
                        "org.javarosa.core.model.data.SelectMultiData",
                        "org.javarosa.core.model.data.SelectOneData",
                        "org.javarosa.core.model.data.StringData",
                        "org.javarosa.core.model.data.TimeData",
                        "org.javarosa.core.model.instance.DataModelTree",
                        "org.javarosa.core.services.locale.TableLocaleSource",
                        "org.javarosa.xpath.expr.XPathArithExpr",
                        "org.javarosa.xpath.expr.XPathBoolExpr",
                        "org.javarosa.xpath.expr.XPathCmpExpr",
                        "org.javarosa.xpath.expr.XPathEqExpr",
                        "org.javarosa.xpath.expr.XPathFilterExpr",
                        "org.javarosa.xpath.expr.XPathFuncExpr",
                        "org.javarosa.xpath.expr.XPathNumericLiteral",
                        "org.javarosa.xpath.expr.XPathNumNegExpr",
                        "org.javarosa.xpath.expr.XPathPathExpr",
                        "org.javarosa.xpath.expr.XPathStringLiteral",
                        "org.javarosa.xpath.expr.XPathUnionExpr",
                        "org.javarosa.xpath.expr.XPathVariableReference"};

        // TODO: any way to remove reliance on jrsp?
        JavaRosaServiceProvider.instance().registerPrototypes(classes);

        FormDef fd = new FormDef();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(formdef);
            DataInputStream dis = new DataInputStream(fis);
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


    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(FormHandler fh) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.loadingComplete(fh);
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        mStateListener = sl;
    }
}
