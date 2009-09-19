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

package org.odk.collect.android.logic;


/**
 * The constants used in multiple classes in this application.
 * 
 * @author @author Yaw Anokwa (yanokwa@gmail.com)
 * 
 */
public class GlobalConstants {

    /**
     * Request code for returning image capture data from camera intent.
     */
    public static final int IMAGE_CAPTURE = 1;

    /**
     * Request code for returning image capture data from camera intent.
     */
    public static final int BARCODE_CAPTURE = 2;

    /**
     * Request code for returning audio data from mediarecorder intent.
     */
    public static final int AUDIO_CAPTURE = 3;

    /**
     * Request code for returning video data from mediarecorder intent.
     */
    public static final int VIDEO_CAPTURE = 4;

    /**
     * Answer saved with no errors.
     */
    public static final int ANSWER_OK = 0;

    /**
     * Answer required, but was empty or null.
     */
    public static final int ANSWER_REQUIRED_BUT_EMPTY = 1;

    /**
     * Answer constraint was violated.
     */
    public static final int ANSWER_CONSTRAINT_VIOLATED = 2;

    /**
     * Used to validate and display valid form names.
     */
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

    /**
     * Forms storage path
     */
    public static final String FORMS_PATH = "/sdcard/odk/forms/";

    /**
     * Instances storage path
     */
    public static final String INSTANCES_PATH = "/sdcard/odk/instances/";

    /**
     * Temp path
     */
    public static final String CACHE_PATH = "/sdcard/odk/.cache/";


    /**
     * Identifies the location of the form used to launch form entry
     */
    public static final String KEY_FORMPATH = "formpath";
    public static final String KEY_INSTANCEPATH = "instancepath";
    public static final String KEY_INSTANCES = "instances";
    public static final String KEY_SUCCESS = "success";


    /**
     * How long to wait when opening network connection in milliseconds
     */
    public static final int CONNECTION_TIMEOUT = 10000;

    /**
     * Temporary file
     */
    public static final String IMAGE_PATH = CACHE_PATH + "tmp.bin";

    /**
     * Theme used in entire application
     */
    public static final int APPLICATION_THEME = android.R.style.Theme_Light;

    /**
     * Default font size in entire application
     */
    public final static int APPLICATION_FONTSIZE = 10;

    /**
     * Classes needed to serialize objects
     */
    public final static String[] SERIALIABLE_CLASSES =
            {"org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
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
                    "org.javarosa.core.model.data.StringData",
                    "org.javarosa.core.model.data.TimeData",
                    "org.javarosa.core.model.instance.DataModelTree",
                    "org.javarosa.core.services.locale.TableLocaleSource",
                    "org.javarosa.xpath.expr.XPathArithExpr",
                    "org.javarosa.xpath.expr.XPathBoolExpr",
                    "org.javarosa.xpath.expr.XPathCmpExpr", "org.javarosa.xpath.expr.XPathEqExpr",
                    "org.javarosa.xpath.expr.XPathFilterExpr",
                    "org.javarosa.xpath.expr.XPathFuncExpr",
                    "org.javarosa.xpath.expr.XPathNumericLiteral",
                    "org.javarosa.xpath.expr.XPathNumNegExpr",
                    "org.javarosa.xpath.expr.XPathPathExpr",
                    "org.javarosa.xpath.expr.XPathStringLiteral",
                    "org.javarosa.xpath.expr.XPathUnionExpr",
                    "org.javarosa.xpath.expr.XPathVariableReference"};

}
