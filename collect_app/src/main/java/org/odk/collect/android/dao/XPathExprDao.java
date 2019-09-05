/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.TreeReferenceString;
import org.odk.collect.android.provider.XPathProviderAPI;

import java.util.ArrayList;
import java.util.List;

public class XPathExprDao {

    /**
     * Returns all TreeReferences available through the cursor and closes the cursor.
     */
    private List<TreeReference> getNodesetFromCursor(Cursor cursor) {
        List<TreeReferenceString> treeReferenceStrings = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    //int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                    int treeReferenceStringIndex = cursor.getColumnIndex(XPathProviderAPI.XPathsColumns.TREE_REF);

                    TreeReferenceString treeReferenceString = new TreeReferenceString.Builder()
                            .treeReferenceString(cursor.getString(treeReferenceStringIndex))
                            .build();

                    treeReferenceStrings.add(treeReferenceString);
                }
            } finally {
                cursor.close();
            }
        }
        List<TreeReference> treeReferenceList = new ArrayList<>();
        for(TreeReferenceString treeReferenceString: treeReferenceStrings){
            TreeReference treeReference = treeReferenceStringToObject(treeReferenceString);
            treeReferenceList.add(treeReference);
        }
        return treeReferenceList;
    }

    public List<TreeReference> getTreeReferenceMatches(String expression){
        return getNodesetFromCursor(getXPathEvalCursor(expression));
    }

    public List<TreeReference> getLeafParentMatches(String expression){
        return getNodesetFromCursor(getXPathEvalCursor(expression));
    }

    public List<IAnswerData> getIAnswerDataMatches(String expression){
        return null;//getNodesetFromCursor(getXPathEvalCursor(expression));
    }

    public List<TreeReference> getIAnswerMatche(String expression){
        return getNodesetFromCursor(getXPathEvalCursor(expression));
    }

    private TreeReference treeReferenceStringToObject(TreeReferenceString treeReferenceString) {
        return null;
    }

    public Uri saveTreeReferenceString(ContentValues values) {
        return Collect.getInstance().getContentResolver().insert(XPathProviderAPI.XPathsColumns.CONTENT_URI, values);
    }

    public Cursor getXPathEvalCursor(String expression) {
        String[] selectionArgs;
        String selection;
        selectionArgs = new String[]{expression};
            selection = XPathProviderAPI.XPathsColumns.PRE_EVAL_EXPR + "=? ";
        return getXPathEvalCursor(null, selection, selectionArgs, null);
    }

    Cursor getXPathEvalCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(XPathProviderAPI.XPathsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

}
