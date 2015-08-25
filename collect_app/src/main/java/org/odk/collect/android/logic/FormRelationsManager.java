/* The MIT License (MIT)
 *
 *       Copyright (c) 2015 PMA2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.odk.collect.android.logic;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FormRelationsDb;
import org.odk.collect.android.exception.FormRelationsException;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *  Defines important functions for working with Parent/Child forms.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 25 August 2015
 */
public class FormRelationsManager {

    private static final String TAG = "FormRelationManager";
    private static final boolean LOCAL_LOG = true;

    private static final String SAVE_INSTANCE = "saveInstance";
    private static final String SAVE_FORM = "saveForm";
    private static final String DELETE_FORM = "deleteForm";

    private static final int NO_ERROR_CODE = 0;
    private static final int PROVIDER_NO_FORM = 1;
    private static final int NO_INSTANCE_NO_FORM = 2;

    private long parentId;
    private ArrayList<TraverseData> allTraverseData;
    private int maxRepeatIndex;

    public FormRelationsManager(long parentId) {
        this.parentId = parentId;
        allTraverseData = new ArrayList<TraverseData>();
        maxRepeatIndex = 0;
    }

    private class TraverseData {
        String attr;
        String attrValue;
        String instanceXpath;
        String instanceValue;
        int repeatIndex;
    }

    // Cleans the input somewhat
    private void addTraverseData(String attr, String attrValue, String instanceXpath,
                                String instanceValue) throws FormRelationsException {
        TraverseData td = new TraverseData();
        if (DELETE_FORM.equals(attr)) {
            throw new FormRelationsException(DELETE_FORM);
        }
        td.attr = attr;
        td.attrValue = attrValue;
        td.instanceXpath = cleanInstanceXpath(instanceXpath);
        td.instanceValue = instanceValue;
        td.repeatIndex = parseInstanceXpath(td.instanceXpath);
        allTraverseData.add(td);
    }

    private String cleanInstanceXpath(String instanceXpath) {
        int firstSlash = instanceXpath.indexOf("/");
        String toReturn = instanceXpath.substring(firstSlash);
        return toReturn;
    }

    // Return repeat count. Assumes that all transferred data is in a repeat,
    // and not shared with other repeats (and not in a repeat, e.g. house number,
    // and repeat for household members). Enforce rules that there should be
    // only one non-"1" index (only one group/repeat) in the xpath.
    private int parseInstanceXpath(String instanceXpath) {
        int repeatIndex = 1;
        int numNonOne = 0;

        int leftBracket = instanceXpath.indexOf("[");
        int safety = 100;
        while (leftBracket >= 0 && safety > 0) {
            int rightBracket = instanceXpath.indexOf("]", leftBracket);
            if (rightBracket < 0) {
                break;
            }
            try {
                String repeat = instanceXpath.substring(leftBracket+1, rightBracket);
                if (LOCAL_LOG) {
                    Log.d(TAG, "Trying to parse \'" + repeat + "\'. Left bracket @" + leftBracket + " and right bracket @" + rightBracket);
                }
                int potentialRepeat = Integer.parseInt(repeat);
                if (potentialRepeat > 1) {
                    repeatIndex = potentialRepeat;
                    numNonOne += 1;
                    maxRepeatIndex = Math.max(maxRepeatIndex, repeatIndex);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error parsing repeat index to int: \'" + instanceXpath + "\'");
            }
            leftBracket = instanceXpath.indexOf("[", rightBracket);
            safety--;
        }

        if (numNonOne > 1) {
            Log.w(TAG, "Multiple repeats detected in this XPath: \'" + instanceXpath + "\'");
        }

        return repeatIndex;
    }

    // Unfortunately, we must violate DRY (don't repeat yourself). Creating an instance
    // from a URI is done at the end of onCreate in FormEntryActivity and in doInBackground
    // of FormLoaderTask, not in a method that can be called.
    /*
     * In order to get the proper instance, we must first load the child form
     * then save it to disk and only _then_ can we edit the .xml file with the
     * appropriate values from the parent form. Much of this is similar to what
     * happens in FormLoaderTask, but we're already in a thread here.
     */
    // Lots of copy and paste
    public static String createInstance(Uri formUri) throws FormRelationsException {
        if (LOCAL_LOG) {
            Log.d(TAG, "Inside createInstance(" + formUri.toString() + ")");
        }
        FormDef fd = null;
        FileInputStream fis = null;
        String errorMsg = null;
        String formPath = "";
        Cursor c = Collect.getInstance().getContentResolver()
                .query(formUri, null, null, null, null);
        if (c != null) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                formPath = c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH));
            }
            c.close();
        }

        if (formPath.equals("")) {
            throw new FormRelationsException();
        }

        FormDef.EvalBehavior mode = AdminPreferencesActivity
                .getConfiguredFormProcessingLogic(Collect.getInstance());
        FormDef.setEvalBehavior(mode);

        File formXml = new File(formPath);
        String formHash = FileUtils.getMd5Hash(formXml);
        File formBin = new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");

        if (formBin.exists()) {
            // if we have binary, deserialize binary
            Log.i(
                    TAG,
                    "Attempting to load " + formXml.getName() + " from cached file: "
                            + formBin.getAbsolutePath());
            fd = FormLoaderTask.deserializeFormDef(formBin);
            if (fd == null) {
                // some error occured with deserialization. Remove the file, and make a
                // new .formdef
                // from xml
                Log.w(
                        TAG,
                        "Deserialization FAILED! Deleting cache file: " +
                                formBin.getAbsolutePath());
                formBin.delete();
            }
        }
        if (fd == null) {
            // no binary, read from xml
            try {
                Log.i(TAG, "Attempting to load from: " + formXml.getAbsolutePath());
                fis = new FileInputStream(formXml);
                fd = XFormUtils.getFormFromInputStream(fis);
                if (fd == null) {
                    errorMsg = "Error reading XForm file";
                } else {
                    FormLoaderTask.serializeFormDef(fd, formPath);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                errorMsg = e.getMessage();
            } catch (XFormParseException e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }

        if (errorMsg != null || fd == null) {
            throw new FormRelationsException();
        }

        // set paths to /sdcard/odk/forms/formfilename-media/
        String formFileName = formXml.getName().substring(0, formXml.getName().lastIndexOf("."));
        File formMediaDir = new File(formXml.getParent(), formFileName + "-media");

        // Skip ExternalDataManaging

        // create FormEntryController from formdef
        FormEntryModel fem = new FormEntryModel(fd);
        FormEntryController fec = new FormEntryController(fem);

        FormController formController = new FormController(formMediaDir, fec, null);

        // this is for preloaders, but gets rid of non-relevant variables. (the
        // boolean is new form).
        fd.initialize(true, new InstanceInitializationFactory());

        String instancePath = "";
        if (formController.getInstancePath() == null) {
            // Create new answer folder.
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SS",
                    Locale.ENGLISH).format(Calendar.getInstance().getTime());
            String file = formPath.substring(formPath.lastIndexOf('/') + 1,
                    formPath.lastIndexOf('.'));
            String path = Collect.INSTANCES_PATH + File.separator + file + "_"
                    + time;
            if (FileUtils.createFolder(path)) {
                formController.setInstancePath(new File(path + File.separator
                        + file + "_" + time + ".xml"));
                instancePath = path + File.separator + file + "_" + time + ".xml";
            }
        }

        exportData(formController);
        updateInstanceDatabase(formUri, instancePath);
        return instancePath;
    }

    /**
     * Writes the data to the sdcard, and updates the instances content
     * provider. In theory we don't have to write to disk, and this is where
     * you'd add other methods.
     */
    public static boolean exportData(FormController formController) {

        ByteArrayPayload payload;
        try {
            payload = formController.getFilledInFormXml();
            // write out xml
            String instancePath = formController.getInstancePath().getAbsolutePath();
            SaveToDiskTask.exportXmlFile(payload, instancePath);

        } catch (IOException e) {
            Log.e(TAG, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Just like the private method inside SaveToDiskTask
    private static Uri updateInstanceDatabase(Uri formUri, String instancePath) {
        if (LOCAL_LOG)  {
            Log.d(TAG, "Inside updateInstanceDatabase");
        }
        ContentValues values = new ContentValues();
        values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
        values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(true));

        // Entry didn't exist, so create it.
        Cursor c = null;
        try {
            // retrieve the form definition...
            c = Collect.getInstance().getContentResolver().query(formUri, null, null, null, null);
            c.moveToFirst();
            String jrformid = c.getString(c.getColumnIndex(FormsColumns.JR_FORM_ID));
            String jrversion = c.getString(c.getColumnIndex(FormsColumns.JR_VERSION));
            String formname = c.getString(c.getColumnIndex(FormsColumns.DISPLAY_NAME));
            String submissionUri = null;
            if (!c.isNull(c.getColumnIndex(FormsColumns.SUBMISSION_URI))) {
                submissionUri = c.getString(c.getColumnIndex(FormsColumns.SUBMISSION_URI));
            }

            // add missing fields into values
            values.put(InstanceColumns.INSTANCE_FILE_PATH, instancePath);
            values.put(InstanceColumns.SUBMISSION_URI, submissionUri);
            // TODO: Could insert instance name here?
            values.put(InstanceColumns.DISPLAY_NAME, formname);
            values.put(InstanceColumns.JR_FORM_ID, jrformid);
            values.put(InstanceColumns.JR_VERSION, jrversion);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        Uri insertedInstance = Collect.getInstance().getContentResolver()
                .insert(InstanceColumns.CONTENT_URI, values);
        if (LOCAL_LOG) {
            Log.d(TAG, "Successfully placed instance \'" + insertedInstance.toString() +
                    "\' into InstanceProvider");
        }
        return insertedInstance;

    }

    public static boolean deleteInstance(long instanceId) {
        // STUB
        return true;
    }

    private void outputOrUpdateChildForms() {
        for (int i = 1; i <= maxRepeatIndex; i++ ) {
            ArrayList<TraverseData> saveFormMapping = new ArrayList<TraverseData>();
            ArrayList<TraverseData> saveInstanceMapping = new ArrayList<TraverseData>();

            for (Iterator<TraverseData> it = allTraverseData.iterator(); it.hasNext(); ) {
                TraverseData td = it.next();
                if (td.repeatIndex == i) {
                    if (SAVE_FORM.equals(td.attr)) {
                        saveFormMapping.add(td);
                    } else if (SAVE_INSTANCE.equals(td.attr)) {
                        saveInstanceMapping.add(td);
                    } else {
                        String m = "Trying to output or update child form. Unexpected attr=\'" +
                                td.attr + "\' @" + td.instanceXpath;
                        Log.w(TAG, m);
                    }
                }
            }

            if (saveFormMapping.isEmpty() && saveInstanceMapping.isEmpty()) {
                Log.i(TAG, "No form relations information for repeat node (" + i + ")");
                continue;
            }

            String childInstancePath = "";
            try {
                childInstancePath = getOrCreateChildForm(saveFormMapping, saveInstanceMapping);
            } catch (FormRelationsException e) {
                String msg = "Exception raised when getting or creating child form for repeat (" +
                        i + ")";

                switch (e.getErrorCode()) {
                    case NO_ERROR_CODE:
                        break;
                    case PROVIDER_NO_FORM:
                        msg = "No form with id \'" + e.getInfo() + "\' in FormProvider for " +
                                "repeat (" + i +")";
                        break;
                    case NO_INSTANCE_NO_FORM:
                        msg = "No child form exists, impossible to create one, no saveForm " +
                                "information in repeat node (" + i + ")!";
                        break;
                }
                Log.w(TAG, msg);
                continue;
            }
        }
    }

    private String getOrCreateChildForm(ArrayList<TraverseData> saveFormMapping,
            ArrayList<TraverseData> saveInstanceMapping) throws FormRelationsException {
        String instancePath = null;

        int repeatIndex = 0;
        if ( !saveFormMapping.isEmpty() ) {
            repeatIndex = saveFormMapping.get(0).repeatIndex;
        } else if ( !saveInstanceMapping.isEmpty() ) {
            repeatIndex = saveInstanceMapping.get(0).repeatIndex;
        }

        long childId = FormRelationsDb.getChild(parentId, repeatIndex);
        if (childId < 0 && saveFormMapping.isEmpty()) {
            throw new FormRelationsException();
        } else if (childId < 0) {
            // There was no child for the given parent form and associated index, so create one.
            String childFormId = saveFormMapping.get(0).attrValue;

            String[] columns = {
                    FormsColumns._ID
            };
            String selection = FormsColumns.JR_FORM_ID + "=?";
            String[] selectionArgs = {
                    childFormId
            };

            Cursor cursor = Collect
                    .getInstance()
                    .getContentResolver()
                    .query(FormsColumns.CONTENT_URI, columns, selection, selectionArgs, null);
            long formId = -1;
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    formId = cursor.getLong(cursor.getColumnIndex(FormsColumns._ID));
                }
                cursor.close();
            }

            if (formId == -1) {
                throw new FormRelationsException(PROVIDER_NO_FORM, childFormId);
            }

            instancePath = createInstance(Uri.withAppendedPath(FormsColumns.CONTENT_URI,
                    String.valueOf(formId)));
        } else {
            // Get old instance

        }

        return instancePath;
    }


    public static void manageChildForms(long parentId, TreeElement instanceRoot) {
        try {
            FormRelationsManager frm = new FormRelationsManager(parentId);
            traverseInstance(instanceRoot, frm);
            frm.outputOrUpdateChildForms();
        } catch (FormRelationsException e) {
            if (DELETE_FORM.equals(e.getMessage())) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Interrupted to delete instance with id (" + parentId + ")");
                }
                deleteInstance(parentId);
            }
        }
    }

    // Update mutable object: FormRelationsManager
    private static void traverseInstance(TreeElement te, FormRelationsManager frm) throws
            FormRelationsException {
        for (int i = 0; i < te.getNumChildren(); i++) {
            TreeElement teChild = te.getChildAt(i);

            String ref = teChild.getRef().toString(true);
            if (ref.contains("@template")) {
                // skip template nodes
                continue;
            }

            checkAttrs(teChild, frm);

            // recurse
            if (teChild.getNumChildren() > 0) {
                traverseInstance(teChild, frm);
            }
        }
    }

    private static void checkAttrs(TreeElement te, FormRelationsManager frm) throws
            FormRelationsException {
        if (te.isRelevant()) {
            List<TreeElement> attrs = te.getBindAttributes();
            for (TreeElement attr : attrs) {
                boolean isFormRelationMaterial = SAVE_INSTANCE.equals(attr.getName()) ||
                        SAVE_FORM.equals(attr.getName()) || DELETE_FORM.equals(attr.getName());
                if (isFormRelationMaterial) {
                    String thisAttr = attr.getName();
                    String attrValue = attr.getAttributeValue();
                    String instanceXpath = te.getRef().toString(true);
                    String instanceValue = null;
                    if (te.getValue() != null) {
                        instanceValue = te.getValue().getDisplayText();
                    }
                    if (LOCAL_LOG) {
                        Log.d(TAG, "@" + instanceXpath + " found " + thisAttr + "=\'" + attrValue +
                                "\' with instance value \'" + instanceValue + "\'");
                    }
                    frm.addTraverseData(thisAttr, attrValue, instanceXpath, instanceValue);
                }
            }
        }
    }
}
