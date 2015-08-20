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

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;

/**
 *  Defines important functions for working with Parent/Child forms.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 20 August 2015
 */
public class FormRelationManager {

    private static final String TAG = "FormRelationManager";

    public static String createInstance(Uri formUri) {
        // STUB
        return null;
    }

    public static void outputOrUpdateChildForms() {
        // STUB
    }

    private class traverseData {
        String parentNode;
        int repeatIndex;
        String parentValue;
        String childNode;
        String childFormId;
    }

    // Update mutable objects:
    //  - XPaths and values for non-repeat saveInstance + child XPath
    //  - non-repeat saveForm + formId + XPath
    //  - XPaths and values and repeat number for repeat saveInstance
    //  - repeat saveForm + formId + XPath + repeat number
    private void traverseInstance(TreeElement te, FormDef fd) {

    }
}
