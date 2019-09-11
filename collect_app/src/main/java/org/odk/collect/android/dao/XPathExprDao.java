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

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.RemoteException;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStringLiteral;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.helpers.XPathDatabaseHelper;
import org.odk.collect.android.dto.TreeReferenceString;
import org.odk.collect.android.provider.XPathProviderAPI;

import java.util.ArrayList;
import java.util.List;

public class XPathExprDao {

    private static XPathExprDao xPathExprDao = null;
    private XPathDatabaseHelper xPathDatabaseHelper;

    public XPathExprDao(){
        this.xPathDatabaseHelper = new XPathDatabaseHelper();
    }

    public static XPathExprDao getInstance(){
        if (xPathExprDao == null) {
            return new XPathExprDao();
        } else {
            return xPathExprDao;
        }
    }

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

    public List<String> getColumnsFromDB(){
        List<String> columnNames = new ArrayList<>();
        SQLiteDatabase db = xPathDatabaseHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info("+ XPathDatabaseHelper.XPATH_TABLE_NAME +")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    columnNames.add(name);
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            db.close();
        }
        return columnNames;
    }


    public boolean createColumn(String columnName){

        SQLiteDatabase db = xPathDatabaseHelper.getWritableDatabase();
        try {
            db.execSQL("ALTER TABLE " + XPathDatabaseHelper.XPATH_TABLE_NAME + " ADD COLUMN "+ columnName + " text;");
        }finally {
            db.close();
        }
        return true;
        //TODO: What does cursor return here : still returns true since no error is thrown
    }

    public List<TreeReference> getTreeReferenceMatches(String expression){
        return getNodesetFromCursor(getXPathEvalCursor(expression));
    }

    /**
     *
     * Checks if a leaf node has been indexed
     * @param expressionRef
     * @return
     */
    public int fetchIndexCount(TreeReference expressionRef){
        SQLiteDatabase db = xPathDatabaseHelper.getWritableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.rawQuery("select count(*) from " + XPathDatabaseHelper.XPATH_TABLE_NAME + " where " + XPathProviderAPI.XPathsColumns.EVAL_EXPR

                    + " = \"" + expressionRef.toString(false) +"\";", null);
            return cursor.getCount();
        }
        catch(Exception ex){
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            db.close();
        }
        return  0;
    }

    public IAnswerData queryScalerEqXPathExpression(TreeReference expressionRef){
        SQLiteDatabase db = xPathDatabaseHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String a = "";
            String b = "";
            XPathExpression predicate = expressionRef.getPredicate(expressionRef.size()-2).get(0);
            if(predicate instanceof XPathEqExpr){
                XPathEqExpr eqPredicate = (XPathEqExpr) predicate;
                if(eqPredicate.a instanceof XPathPathExpr && eqPredicate.b instanceof XPathStringLiteral){
                    a =  ((XPathPathExpr) eqPredicate.a).getReference().getNameLast();
                    b =  ((XPathStringLiteral) eqPredicate.b).s;
                }
            }

            cursor = db.rawQuery("select "+ expressionRef.getNameLast() +" from " + XPathDatabaseHelper.XPATH_TABLE_NAME + " where " + a + " = \"" + b +"\";", null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                String value = cursor.getString(cursor.getColumnIndex(expressionRef.getNameLast()));
                return new StringData(value);

            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
            db.close();
        }
        return  null;
    }


    private TreeReference treeReferenceStringToObject(TreeReferenceString treeReferenceString) {
        return null;
    }

    public void flushIndexes(ArrayList<ContentProviderOperation> operations ) throws OperationApplicationException, RemoteException {
        Collect.getInstance().getContentResolver().applyBatch(XPathProviderAPI.XPathsColumns.CONTENT_URI.getAuthority(),
                operations);
    }

    public Cursor getXPathEvalCursor(String expression) {
        String[] selectionArgs;
        String selection;
        selectionArgs = new String[]{expression};
            selection = XPathProviderAPI.XPathsColumns.EVAL_EXPR + "=? ";
        return getXPathEvalCursor(null, selection, selectionArgs, null);
    }

    Cursor getXPathEvalCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver().query(XPathProviderAPI.XPathsColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public void clearCache(){
        Collect.getInstance().getContentResolver().delete(XPathProviderAPI.XPathsColumns.CONTENT_URI, null, null);
    }


}
