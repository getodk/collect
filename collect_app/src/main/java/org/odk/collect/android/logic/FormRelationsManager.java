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

import android.net.Uri;
import android.util.Log;

import org.javarosa.core.model.instance.TreeElement;
import org.odk.collect.android.exception.FormRelationsException;

import java.util.ArrayList;
import java.util.List;

/**
 *  Defines important functions for working with Parent/Child forms.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 24 August 2015
 */
public class FormRelationsManager {

    private static final String TAG = "FormRelationManager";
    private static final boolean LOCAL_LOG = true;

    private static final String SAVE_INSTANCE = "saveInstance";
    private static final String SAVE_FORM = "saveForm";
    private static final String DELETE_FORM = "deleteForm";

    private long parentId;
    private ArrayList<TraverseData> allTraverseData;
    private int maxRepeatIndex;

    public FormRelationsManager(long parentId) {
        this.parentId = parentId;
        allTraverseData = new ArrayList<TraverseData>();
        maxRepeatIndex = 1;
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
        while (leftBracket >= 0) {
            int rightBracket = leftBracket + instanceXpath.substring(leftBracket).indexOf("]");
            try {
                String repeat = instanceXpath.substring(leftBracket+1, rightBracket);
                int potentialRepeat = Integer.parseInt(repeat);
                if (potentialRepeat > 1) {
                    repeatIndex = potentialRepeat;
                    numNonOne += 1;
                    maxRepeatIndex = Math.max(maxRepeatIndex, repeatIndex);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error parsing repeat index to int: \'" + instanceXpath + "\'");
            }
            leftBracket = instanceXpath.substring(rightBracket).indexOf("[");
        }

        if (numNonOne > 1) {
            Log.w(TAG, "Multiple repeats detected in this XPath: \'" + instanceXpath + "\'");
        }

        return repeatIndex;
    }

    public static String createInstance(Uri formUri) {
        // STUB
        return null;
    }

    public static boolean deleteInstance(long instanceId) {
        // STUB
        return true;
    }



    public static void manageChildForms(long parentId, TreeElement instanceRoot) {
        try {
            FormRelationsManager frm = new FormRelationsManager(parentId);
            traverseInstance(instanceRoot, frm);
        } catch (FormRelationsException e) {
            if (DELETE_FORM.equals(e.getMessage())) {
                if (LOCAL_LOG) {
                    Log.d(TAG, "Interrupted to delete instance with id (" + parentId + ")");
                }
                deleteInstance(parentId);
            }
        }
    }

    // Update mutable objects:
    //  - XPaths and values for non-repeat saveInstance + child XPath
    //  - non-repeat saveForm + formId + XPath
    //  - XPaths and values and repeat number for repeat saveInstance
    //  - repeat saveForm + formId + XPath + repeat number
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
