/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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

package org.odk.collect.android.external.handler;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.tasks.SmapRemoteWebServicePostTask;
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;
import org.odk.collect.android.utilities.FormDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Download a media item to be used as a dynamic default value
 */
public class SmapRemoteDataHandlerGetMedia implements IFunctionHandler {

    public static final String HANDLER_NAME = "get_media";


    public SmapRemoteDataHandlerGetMedia() {
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public List<Class[]> getPrototypes() {
        return new ArrayList<Class[]>();
    }

    @Override
    public boolean rawArgs() {
        return true;
    }

    @Override
    public boolean realTime() {
        return false;
    }

    @Override
    public Object eval(Object[] args, EvaluationContext ec) {

        if (args.length < 1) {
            Timber.e("At least 1 arguments is needed to evaluate the %s function", HANDLER_NAME);
            return "";
        }

        Collect app = Collect.getInstance();

        String url = XPathFuncExpr.toString(args[0]);
        String timeoutValue = "0";

        if(url.length() > 0 && url.startsWith("http")) {

            int idx = url.lastIndexOf('/');
            String mediaName = null;
            if (idx > -1) {
                mediaName = url.substring(idx + 1);
            }

            FormController formController = app.getFormController();
            if (formController == null) {
                return null;
            }
            File f = new File(formController.getInstanceFile().getParent() + File.separator + mediaName);

            // Get the file if it does not exist and there is nothing in the cache indicating that an attempt has alreadby been made to get it
            if(!f.exists() && app.getRemoteData(url) == null) {
                app.startRemoteCall(url);
                SmapRemoteWebServiceTask task = new SmapRemoteWebServiceTask();
                task.setSmapRemoteListener(app.getFormEntryActivity());
                task.execute(url, timeoutValue, "false", f.getAbsolutePath(), mediaName);
            } else {
                return mediaName;
            }
        }
        return "";

    }
}
