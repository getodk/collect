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
import org.odk.collect.android.database.FormRelationsDb.MappingData;
import org.odk.collect.android.exception.FormRelationsException;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 *  Defines important functions for working with Parent/Child forms.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Created: 20 August 2015
 *  Last modified: 3 September 2015
 */
public class FormRelationsManager {

    private static final String TAG = "FormRelationsManager";
    private static final boolean LOCAL_LOG = true;

    // Return codes for what to delete
    public static final int NO_DELETE = -1;
    public static final int DELETE_THIS = 0;
    public static final int DELETE_CHILD = 1;

    private static final String SAVE_INSTANCE = "saveInstance";
    private static final String SAVE_FORM = "saveForm";
    private static final String DELETE_FORM = "deleteForm";

    // Error codes for FormRelationsException
    private static final int NO_ERROR_CODE = 0;
    private static final int PROVIDER_NO_FORM = 1;
    private static final int NO_INSTANCE_NO_FORM = 2;
    private static final int NO_REPEAT_NUMBER = 3;
    private static final int PROVIDER_NO_INSTANCE = 4;
    private static final int BAD_XPATH_INSTANCE = 5;

    private long parentId;
    private ArrayList<TraverseData> allTraverseData;
    private int maxRepeatIndex;
    private ArrayList<TraverseData> nonRelevantSaveForm;
    private boolean hasDeleteForm;

    public FormRelationsManager() {
        parentId = -1;
        allTraverseData = new ArrayList<TraverseData>();
        maxRepeatIndex = 0;
        nonRelevantSaveForm = new ArrayList<TraverseData>();
        hasDeleteForm = false;
    }

    public FormRelationsManager(long parentId) {
        this.parentId = parentId;
        allTraverseData = new ArrayList<TraverseData>();
        maxRepeatIndex = 0;
        nonRelevantSaveForm = new ArrayList<TraverseData>();
        hasDeleteForm = false;
    }

    // Called within SaveToDiskTask
    public static void manageFormRelations(long instanceId, TreeElement instanceRoot) {
        manageParentForm(instanceId);
        FormRelationsManager frm = getFormRelationsManager(instanceId, instanceRoot);
        frm.outputOrUpdateChildForms();
        frm.manageDeletions();
    }

    private static boolean manageParentForm(long childId) {
        Long parentId = FormRelationsDb.getParent(childId);
        if (LOCAL_LOG) {
            Log.d(TAG, "Inside manageParentForm. Parent instance id is \'" + parentId + "\'");
        }
        if (parentId < 0) { // No parent form to manage
            return false;
        }

        try {
            String parentInstancePath = getInstancePath(getInstanceUriFromId(parentId));
            String childInstancePath = getInstancePath(getInstanceUriFromId(childId));

            Document parentDocument = getDocument(parentInstancePath);
            Document childDocument = getDocument(childInstancePath);

            XPath xpath = XPathFactory.newInstance().newXPath();
            ArrayList<MappingData> mappings = FormRelationsDb.getMappingsToParent(childId);
            boolean editedParentForm = false;
            for (MappingData mapping : mappings) {
                XPathExpression parentExpression = xpath.compile(mapping.parentNode);
                XPathExpression childExpression = xpath.compile(mapping.childNode);

                Node parentNode = (Node) parentExpression.evaluate(parentDocument,
                        XPathConstants.NODE);
                Node childNode = (Node) childExpression.evaluate(childDocument,
                        XPathConstants.NODE);

                if (null == parentNode || null == childNode) {
                    throw new FormRelationsException(BAD_XPATH_INSTANCE, "Child: " +
                            mapping.childNode + ", Parent: " + mapping.parentNode);
                }

                if ( !childNode.getTextContent().equals(parentNode.getTextContent()) ) {
                    Log.i(TAG, "Found difference updating parent form @ parent node \'" +
                            parentNode.getNodeName() + "\'. Child: \'" +
                            childNode.getTextContent() + "\' <> Parent: \'" +
                            parentNode.getTextContent() + "\'");
                    parentNode.setTextContent(childNode.getTextContent());
                    editedParentForm = true;
                }
            }

            if (editedParentForm) {
                writeDocumentToFile(parentDocument, parentInstancePath);
                ContentValues cv = new ContentValues();
                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
                Collect.getInstance().getContentResolver().update(getInstanceUriFromId(parentId),
                        cv, null, null);
            }
        } catch (FormRelationsException e) {
            if (e.getErrorCode() == PROVIDER_NO_INSTANCE) {
                Log.w(TAG, "Unable to find the instance path for either this form (id=" +
                        childId + ") or its parent (id=" + parentId + ")");
            } else if (e.getErrorCode() == BAD_XPATH_INSTANCE) {
                Log.w(TAG, "Bad XPath from one of child or parent. " + e.getInfo());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return true;
    }

    // call this to test if something should be deleted
    public static FormRelationsManager getFormRelationsManager(TreeElement instanceRoot) {
        FormRelationsManager frm = new FormRelationsManager();
        try {
            traverseInstance(instanceRoot, frm, null);
        } catch (FormRelationsException e) {
            if (DELETE_FORM.equals(e.getMessage())) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Interrupt traverse to delete instance");
                }
                frm.setDeleteForm(true);
            }
        }
        return frm;
    }

    public static FormRelationsManager getFormRelationsManager(long parentId, TreeElement instanceRoot) {
        FormRelationsManager frm = new FormRelationsManager(parentId);
        try {
            traverseInstance(instanceRoot, frm, null);
        } catch (FormRelationsException e) {
            if (DELETE_FORM.equals(e.getMessage())) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Interrupt traverse to delete instance");
                }
                frm.setDeleteForm(true);
            }
        }
        return frm;
    }

    // meant to be called using the intent data that starts FormEntryActivity
    public static FormRelationsManager getFormRelationsManager(Uri formUri, TreeElement instanceRoot) {
        if (LOCAL_LOG) {
            Log.d(TAG, "Inside getFormRelationsManager with uri \'" + formUri.toString() + "\'");
        }
        long instanceId = getIdFromSingleUri(formUri);
        if (LOCAL_LOG) {
            Log.d(TAG, "Determined id to be \'" + instanceId + "\'");
        }
        FormRelationsManager frm = new FormRelationsManager(instanceId);
        try {
            traverseInstance(instanceRoot, frm, null);
        } catch (FormRelationsException e) {
            if (DELETE_FORM.equals(e.getMessage())) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Interrupt traverse to delete instance");
                }
                frm.setDeleteForm(true);
            }
        }
        return frm;
    }

    // called when deleting because saving and saveInstance not relevant
    private void manageDeletions() {
        int deleteWhat = getWhatToDelete();
        if ( deleteWhat == DELETE_THIS ) {
            deleteInstance(parentId);
        } else if ( deleteWhat == DELETE_CHILD ) {
            TreeSet<Integer> allRepeatIndices = new TreeSet<Integer>();
            for (TraverseData td : nonRelevantSaveForm ) {
                allRepeatIndices.add(td.repeatIndex);
            }
            TreeSet<Long> allWaywardChildren = new TreeSet<Long>();
            for (Integer i : allRepeatIndices) {
                Long childInstanceId = FormRelationsDb.getChild(parentId, i);
                if (LOCAL_LOG) {
                    Log.d(TAG, "ParentId(" + parentId + ") + RepeatIndex(" + i + ") + ChildIdFound(" + childInstanceId +")");
                }
                if (childInstanceId != -1) {
                    allWaywardChildren.add(childInstanceId);
                }
            }
            for (Long childInstanceId : allWaywardChildren) {
                deleteInstance(childInstanceId);
            }
        }
    }

    // called when deleting a repeat group
    public static void manageRepeatDelete(long parentId, int repeatIndex) {
        if (parentId >= 0 && repeatIndex >= 0) {
            Long childInstanceId = FormRelationsDb.getChild(parentId, repeatIndex);
            Uri childInstance = getInstanceUriFromId(childInstanceId);
            Collect.getInstance().getContentResolver().delete(childInstance, null, null);
            FormRelationsDb.deleteChild(parentId, repeatIndex);
        }
    }

    private boolean outputOrUpdateChildForms() {
        boolean hasChild = false;
        if ( hasDeleteForm ) { // Children to be deleted. Just return.
            return hasChild;
        }

        for (int i = 1; i <= maxRepeatIndex; i++ ) {
            ArrayList<TraverseData> saveFormMapping = new ArrayList<TraverseData>();
            ArrayList<TraverseData> saveInstanceMapping = new ArrayList<TraverseData>();

            // Build up `saveFormMapping` and `saveInstanceMapping` for repeat index `i`
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

            try {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Calling `getOrCreateChildForm` for index (" + i + ")");
                }
                Uri childInstance = getOrCreateChildForm(saveFormMapping, saveInstanceMapping);

                // Now that we have the URI, the child instance definitely exists. Need to
                // transfer over values that are different.
                hasChild = insertAllIntoChild(saveFormMapping, saveInstanceMapping, childInstance);
            } catch (IOException e) {
                Log.w(TAG, e.getMessage());
                // Log.w(TAG, "Error creating serialized payload");
                e.printStackTrace();
                continue;
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
                    case NO_REPEAT_NUMBER:
                        // This should never happen
                        msg = "No information from form relations to indicate which repeat (" +
                                i + ")";
                        break;
                    case PROVIDER_NO_INSTANCE:
                        // This should never happen
                        msg = "InstanceProvider does not have record of child for repeat (" +
                                i + ")";
                }
                Log.w(TAG, msg);
                continue;
            }
        }

        return hasChild;
    }

    private Uri getOrCreateChildForm(ArrayList<TraverseData> saveFormMapping,
                                     ArrayList<TraverseData> saveInstanceMapping) throws
            FormRelationsException, IOException {
        Uri childInstance;

        int repeatIndex = getRepeatIndex(saveFormMapping, saveInstanceMapping);
        long childId = FormRelationsDb.getChild(parentId, repeatIndex);
        if (LOCAL_LOG) {
            Log.d(TAG, "From relations database, child id is: " + childId);
        }
        if (childId < 0) {
            // There was no child for the given parent form and associated index, so create one.
            String childFormId = getChildFormId(saveFormMapping);

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

            Uri formUri = getFormUriFromId(formId);
            childInstance = createInstance(formUri);
        } else {
            // Get old instance
            childInstance = getInstanceUriFromId(childId);
        }

        return childInstance;
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
    public static Uri createInstance(Uri formUri) throws FormRelationsException, IOException {
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
        Uri createdInstance = updateInstanceDatabase(formUri, instancePath);
        return createdInstance;
    }

    /**
     * Writes the data to the sdcard.
     */
    private static boolean exportData(FormController formController) throws IOException {
        ByteArrayPayload payload;
        payload = formController.getFilledInFormXml();
        // write out xml
        String instancePath = formController.getInstancePath().getAbsolutePath();
        SaveToDiskTask.exportXmlFile(payload, instancePath);
        return true;
    }

    // Just like the private method inside SaveToDiskTask
    private static Uri updateInstanceDatabase(Uri formUri, String instancePath) {
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

    // assumes no more than two generations (no grandparents/grandchildren or beyond).
    public static void deleteInstance(long instanceId) {
        if (LOCAL_LOG) {
            Log.d(TAG, "### deleteInstance(" + instanceId + ")");
        }
        long[] childrenIds = FormRelationsDb.getChildren(instanceId);
        // Delete from relations.db
        FormRelationsDb.deleteAsParent(instanceId);
        FormRelationsDb.deleteAsChild(instanceId);
        // Delete from instance provider
        Uri thisInstance = getInstanceUriFromId(instanceId);
        Collect.getInstance().getContentResolver().delete(thisInstance, null, null);
        for (int i = 0; i < childrenIds.length; i++) {
            Uri childInstance = getInstanceUriFromId(instanceId);
            Collect.getInstance().getContentResolver().delete(childInstance, null, null);
        }
    }

    private boolean insertAllIntoChild(ArrayList<TraverseData> saveFormMapping,
                                    ArrayList<TraverseData> saveInstanceMapping,
                                    Uri childInstance) throws FormRelationsException {
        boolean hasChild = false;

        String childInstancePath = getInstancePath(childInstance);

        try {
            Document document = getDocument(childInstancePath);

            // here we should have a good xml document in DOM
            if (saveFormMapping.size() > 0) {
                TraverseData td = saveFormMapping.get(0);
                ContentValues values = new ContentValues();
                values.put(InstanceColumns.DISPLAY_NAME, td.instanceValue);
                Collect.getInstance().getContentResolver()
                        .update(childInstance, values, null, null);
                if (LOCAL_LOG) {
                    Log.d(TAG, "Updated InstanceProvider to show correct instanceName: " + td.instanceValue);
                }
            }

            int repeatIndex = getRepeatIndex(saveFormMapping, saveInstanceMapping);
            long childId = getIdFromSingleUri(childInstance);
            boolean isInstanceModified = false;
            for (TraverseData td : saveInstanceMapping) {
                try {
                    boolean isThisModified = insertIntoChild(td, document);
                    if (isThisModified) {
                        checkCopyBinaryFile(td, childInstancePath);
                    }
                    isInstanceModified = isInstanceModified || isThisModified;
                    hasChild = true;
                } catch (FormRelationsException e) {
                    if (e.getErrorCode() == BAD_XPATH_INSTANCE) {
                        Log.w(TAG, "Unable to insert value \'" + td.instanceValue +
                                "\' into child at " + e.getInfo());
                    }
                }

                updateRelationsDatabase(parentId, td.instanceXpath, repeatIndex, childId,
                        td.attrValue, td.repeatableNode);
            }

            if (isInstanceModified) {
                // only need to update xml if something changed
                writeDocumentToFile(document, childInstancePath);

                // Set status to incomplete
                ContentValues values = new ContentValues();
                values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
                Collect.getInstance().getContentResolver()
                        .update(childInstance, values, null, null);

                if (LOCAL_LOG) {
                    Log.d(TAG, "Rewrote child instance because of changes at " + childInstancePath);
                }
            }
        } catch (FormRelationsException e) {
            throw e;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasChild;
    }

    // return true iff updated
    private static boolean insertIntoChild(TraverseData td, Document document) throws
            XPathExpressionException, FormRelationsException {
        boolean isModified = false;
        String childInstanceValue = td.instanceValue;
        if (null != childInstanceValue) {
            // extract nodes using xpath
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expression;

            String childInstanceXpath = td.attrValue;

            expression = xpath.compile(childInstanceXpath);
            Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
            if ( null == node ) {
                throw new FormRelationsException(BAD_XPATH_INSTANCE, childInstanceXpath);
            }
            if ( !node.getTextContent().equals(childInstanceValue) ) {
                Log.v(TAG, "Found difference saving child form @ child node \'" + node.getNodeName() +
                        "\'. Child: \'" + node.getTextContent() +
                        "\' <> Parent: \'" + childInstanceValue + "\'");
                node.setTextContent(childInstanceValue);
                isModified = true;
            }
        }
        return isModified;
    }

    private static boolean removeFromDocument(String xpathStr, Document document) throws
            XPathExpressionException, FormRelationsException {
        boolean isModified = false;
        if ( null != xpathStr ) {
            // extract nodes using xpath
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expression;

            expression = xpath.compile(xpathStr);
            Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
            if ( null == node ) {
                throw new FormRelationsException(BAD_XPATH_INSTANCE, xpathStr);
            }

            if (LOCAL_LOG) {
                Log.i(TAG, "removeFromDocument -- attempting to delete: " + xpathStr);
            }

            Node removeNode = node.getParentNode();
            removeNode.removeChild(node);
            isModified = true;
        }
        return isModified;
    }

    // Called from within DeleteInstancesTask
    // Remove the repeat node in the parent document.
    // Write the parent to disk
    // Remove from relations.db
    public static void removeAllReferences(long instanceId) {
        boolean isParentModified = false;
        long parentId = FormRelationsDb.getParent(instanceId);
        if (parentId != -1) {
            try {
                String parentPath = getInstancePath(getInstanceUriFromId(parentId));
                Document parentDocument = getDocument(parentPath);
                String repeatXpathToRemove = FormRelationsDb.getRepeatable(parentId, instanceId);
                isParentModified = removeFromDocument(repeatXpathToRemove, parentDocument);
                if (isParentModified) {
                    writeDocumentToFile(parentDocument, parentPath);
                }
            } catch (FormRelationsException e) {
                if ( e.getErrorCode() == PROVIDER_NO_INSTANCE ) {
                    Log.w(TAG, "Removing all references for " + instanceId +
                            ". Parent in relations.db, but no parent in InstanceProvider");
                } else if ( e.getErrorCode() == BAD_XPATH_INSTANCE ) {
                    Log.w(TAG, "Unable to remove node @" + e.getInfo() +
                            " from parent of instance (" + instanceId + ")");
                } else {
                    Log.w(TAG, "OTHER FORMRELATIONSEXCEPTION in removeAllReferences: " +
                            e.getErrorCode());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (isParentModified) {
                    int repeatIndex = FormRelationsDb.getRepeatIndex(parentId, instanceId);
                    FormRelationsDb.deleteChild(parentId, repeatIndex);
                } else {
                    FormRelationsDb.deleteAsChild(instanceId);
                }
            }
        }
        FormRelationsDb.deleteAsParent(instanceId);
    }

    private static void writeDocumentToFile(Document document, String path) throws
            FileNotFoundException, TransformerException {
        // there's a bug in streamresult that replaces spaces in the
        // filename with %20
        // so we use a fileoutput stream
        // http://stackoverflow.com/questions/10301674/save-file-in-android-with-spaces-in-file-name
        File outputFile = new File(path);
        FileOutputStream fos = new FileOutputStream(outputFile);
        Result output = new StreamResult(fos);
        Source input = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(input, output);
    }

    private static boolean updateRelationsDatabase(long parentId, String parentXpath,
                                                   int repeatIndex, long childId,
                                                   String childXpath, String repeatableNode) {
        // First check if row exists.
        boolean rowExists = FormRelationsDb.isRowExists(String.valueOf(parentId), parentXpath,
                String.valueOf(repeatIndex), String.valueOf(childId), childXpath, repeatableNode);
        if ( !rowExists ) {
            if (LOCAL_LOG) {
                Log.v(TAG, "Inserting (parentId=" + parentId + ", parentNode=" + parentXpath +
                        ", index=" + repeatIndex + ", childId=" + childId + ", childNode=" +
                        childXpath + ", repeatableNode=" + repeatableNode +
                        ") into relations database");
            }

            // Otherwise add
            FormRelationsDb.insert(String.valueOf(parentId), parentXpath,
                    String.valueOf(repeatIndex), String.valueOf(childId), childXpath,
                    repeatableNode);
            rowExists = true;
        }
        return rowExists;
    }

    private boolean checkCopyBinaryFile(TraverseData td, String childInstancePath) throws
            FormRelationsException {
        boolean toReturn = false;
        String childInstanceValue = td.instanceValue;
        if (childInstanceValue.endsWith(".jpg") || childInstanceValue.endsWith(".jpeg") ||
                childInstanceValue.endsWith(".3gpp") || childInstanceValue.endsWith(".3gp") ||
                childInstanceValue.endsWith(".mp4") || childInstanceValue.endsWith(".png")) {
            // more?

            Uri parentInstance = getInstanceUriFromId(parentId);
            String parentInstancePath = getInstancePath(parentInstance);
            toReturn = copyBinaryFile(parentInstancePath, childInstancePath, childInstanceValue);
        }
        return toReturn;
    }

    private static boolean copyBinaryFile(String parentInstancePath, String childInstancePath,
                                          String filename) {
        File parentFile = new File(parentInstancePath);
        File childFile = new File(childInstancePath);

        File parentImage = new File(parentFile.getParent() + "/" + filename);
        File childImage = new File(childFile.getParent() + "/" + filename);

        if (LOCAL_LOG) {
            Log.d(TAG, "copyBinaryFile \'" + filename + "\' from " +
                    parentFile.getParent() + " to " + childFile.getParent());
        }

        FileUtils.copyFile(parentImage, childImage);
        return true;
    }

    private static long getIdFromSingleUri(Uri instance) {
        long id = -1;
        if (Collect.getInstance().getContentResolver().getType(instance)
                .equals(InstanceColumns.CONTENT_ITEM_TYPE)) {
            String idStr = instance.getLastPathSegment();
            id = Long.parseLong(idStr);
        } else { // if uri is for a form
            // first try to find by looking up absolute path
            FormController formController = Collect.getInstance().getFormController();
            String[] projection = {
                    InstanceColumns._ID
            };
            String selection = InstanceColumns.INSTANCE_FILE_PATH + "=?";
            String instancePath = formController.getInstancePath().getAbsolutePath();
            String[] selectionArgs = {
                    instancePath
            };
            Cursor c = Collect.getInstance().getContentResolver()
                    .query(InstanceColumns.CONTENT_URI, projection, selection, selectionArgs, null);
            if ( c != null ) {
                if ( c.getCount() > 0 ) {
                    c.moveToFirst();
                    id = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                }
                c.close();
            }
        }
        return id;
    }

    private static Uri getInstanceUriFromId(long id) {
        return Uri.withAppendedPath(InstanceColumns.CONTENT_URI, String.valueOf(id));
    }

    private static Uri getFormUriFromId(long id) {
        return Uri.withAppendedPath(FormsColumns.CONTENT_URI, String.valueOf(id));
    }

    private int getRepeatIndex(ArrayList<TraverseData> saveFormMapping, ArrayList<TraverseData>
            saveInstanceMapping) throws FormRelationsException {
        int repeatIndex = 0;
        if ( !saveFormMapping.isEmpty() ) {
            repeatIndex = saveFormMapping.get(0).repeatIndex;
        } else if ( !saveInstanceMapping.isEmpty() ) {
            repeatIndex = saveInstanceMapping.get(0).repeatIndex;
        }

        if (repeatIndex == 0) {
            throw new FormRelationsException(NO_REPEAT_NUMBER);
        }

        return repeatIndex;
    }

    private String getChildFormId(ArrayList<TraverseData> saveFormMapping) throws
            FormRelationsException{
        if (saveFormMapping.isEmpty()) {
            throw new FormRelationsException(NO_INSTANCE_NO_FORM);
        }
        String childFormId = saveFormMapping.get(0).attrValue;
        return childFormId;
    }

    private static String getInstancePath(Uri oneInstance) throws FormRelationsException {
        String[] projection = {
                InstanceColumns.INSTANCE_FILE_PATH
        };
        Cursor childCursor = Collect.getInstance().getContentResolver()
                .query(oneInstance, projection, null, null, null);
        if (null == childCursor || childCursor.getCount() < 1) {
            throw new FormRelationsException(PROVIDER_NO_INSTANCE);
        }
        // If URI is for table, not for row, potential error (only getting the first row).
        childCursor.moveToFirst();
        String instancePath = childCursor.getString(childCursor.getColumnIndex(
                InstanceColumns.INSTANCE_FILE_PATH));
        childCursor.close();
        return instancePath;
    }

    private class TraverseData {
        String attr;
        String attrValue;
        String instanceXpath;
        String instanceValue;
        int repeatIndex;
        String repeatableNode;
    }

    // Cleans the input somewhat
    private void addTraverseData(String attr, String attrValue, String instanceXpath,
                                 String instanceValue, String repeatableNode,
                                 boolean isRelevant) throws FormRelationsException {
        TraverseData td = new TraverseData();
        td.attr = attr;
        td.attrValue = attrValue;
        td.instanceXpath = cleanInstanceXpath(instanceXpath);
        td.instanceValue = instanceValue;
        td.repeatIndex = parseInstanceXpath(td.instanceXpath);
        td.repeatableNode = cleanInstanceXpath(repeatableNode);
        if (isRelevant) {
            if ( DELETE_FORM.equals(attr) ) {
                throw new FormRelationsException(DELETE_FORM);
            }
            allTraverseData.add(td);
        } else if ( SAVE_FORM.equals(td.attr) ) {
            nonRelevantSaveForm.add(td);
        }

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
                int potentialRepeat = Integer.parseInt(repeat);
                if (potentialRepeat > 1) {
                    repeatIndex = potentialRepeat;
                    numNonOne += 1;
                }
                maxRepeatIndex = Math.max(maxRepeatIndex, repeatIndex);
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

    // Update mutable object: FormRelationsManager
    private static void traverseInstance(TreeElement te, FormRelationsManager frm,
                                         String repeatableNode) throws FormRelationsException {
        for (int i = 0; i < te.getNumChildren(); i++) {
            TreeElement teChild = te.getChildAt(i);

            String ref = teChild.getRef().toString(true);
            if (ref.contains("@template")) {
                // skip template nodes
                continue;
            }


            if (teChild.isRepeatable()) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "In repeatable node @" + ref + " with index [" +
                            teChild.getMult() + "]");
                }
                repeatableNode = ref;
            }

            checkAttrs(teChild, frm, repeatableNode);

            // recurse
            if (teChild.getNumChildren() > 0) {
                traverseInstance(teChild, frm, repeatableNode);
            }
        }
    }

    private static void checkAttrs(TreeElement te, FormRelationsManager frm,
                                   String repeatableNode) throws FormRelationsException {
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
                frm.addTraverseData(thisAttr, attrValue, instanceXpath, instanceValue,
                        repeatableNode, te.isRelevant());
            }
        }
    }

    private static Document getDocument(String path) throws ParserConfigurationException,
            SAXException, IOException {
        File outputFile = new File(path);
        InputStream inputStream = new FileInputStream(outputFile);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource inputSource = new InputSource(reader);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(inputSource);
        return document;
    }

    public int getHowManyToDelete() {
        int howMany = 0;
        if ( hasDeleteForm ) {
            howMany++;
            howMany += FormRelationsDb.getChildren(parentId).length;
        } else {
            TreeSet<Integer> allRepeatIndices = new TreeSet<Integer>();
            for (TraverseData td : nonRelevantSaveForm ) {
                allRepeatIndices.add(td.repeatIndex);
            }
            for (Integer i : allRepeatIndices) {
                if (FormRelationsDb.getChild(parentId, i) != -1) {
                    howMany++;
                }
            }
        }
        return howMany;
    }

    // called when removing a repeat.
    public int getHowManyToDelete(int repeatIndex) {
        int howMany = 0;
        if ( hasDeleteForm ) {
            howMany++;
            howMany += FormRelationsDb.getChildren(parentId).length;
        } else {
            TreeSet<Integer> allRepeatIndices = new TreeSet<Integer>();
            allRepeatIndices.add(repeatIndex);
            for (TraverseData td : nonRelevantSaveForm ) {
                allRepeatIndices.add(td.repeatIndex);
            }
            for (Integer i : allRepeatIndices) {
                if (FormRelationsDb.getChild(parentId, i) != -1) {
                    howMany++;
                }
            }
        }
        return howMany;
    }

    public int getWhatToDelete() {
        int returnCode = NO_DELETE;
        if ( hasDeleteForm ) {
            returnCode = DELETE_THIS;
        } else if ( parentId != -1 ) {
            TreeSet<Integer> allRepeatIndices = new TreeSet<Integer>();
            for (TraverseData td : nonRelevantSaveForm ) {
                allRepeatIndices.add(td.repeatIndex);
            }
            for (Integer i : allRepeatIndices) {
                if (FormRelationsDb.getChild(parentId, i) != -1) {
                    returnCode = DELETE_CHILD;
                    break;
                }
            }
        }
        return returnCode;
    }

    public int getWhatToDelete(int repeatIndex) {
        int returnCode = NO_DELETE;
        if ( hasDeleteForm ) {
            returnCode = DELETE_THIS;
        } else if ( parentId != -1 ) {
            TreeSet<Integer> allRepeatIndices = new TreeSet<Integer>();
            allRepeatIndices.add(repeatIndex);
            for (TraverseData td : nonRelevantSaveForm ) {
                allRepeatIndices.add(td.repeatIndex);
            }
            for (Integer i : allRepeatIndices) {
                if (FormRelationsDb.getChild(parentId, i) != -1) {
                    returnCode = DELETE_CHILD;
                    break;
                }
            }
        }
        return returnCode;
    }

    public void setDeleteForm(boolean val) {
        hasDeleteForm = val;
    }

    public void setParentId(long id) {
        parentId = id;
    }

    public long getParentId() {
        return parentId;
    }

    public boolean getDeleteForm() {
        return hasDeleteForm;
    }
}
