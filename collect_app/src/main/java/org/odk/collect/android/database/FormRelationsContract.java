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

package org.odk.collect.android.database;

import android.provider.BaseColumns;

/**
 *  Defines all constants needed to manage SQLite database for form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 20 August 2015
 */
public class FormRelationsContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private FormRelationsContract() {}

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "relations.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";

    /**
     *  Inner class that defines the table contents.
     *
     *  In order to maintain a record of parent child form relations,
     *  mapping is:
     *
     *    parent instance id + parent node + parent index
     *                             |
     *                             V
     *           child instance id + child node
     *
     *  Because it implements `BaseColumns`, "_ID" is inherited.
     *
     *  See
     *  [0] http://developer.android.com/training/basics/data-storage/databases.html
     *  [1] http://developer.android.com/reference/android/provider/BaseColumns.html
     */
    public static abstract class FormRelations implements BaseColumns {
        public static final String TABLE_NAME = "relations";
        public static final String COLUMN_PARENT_INSTANCE_ID = "parent_id";
        public static final String COLUMN_PARENT_NODE = "parent_node";
        public static final String COLUMN_PARENT_INDEX = "parent_index";
        public static final String COLUMN_CHILD_INSTANCE_ID = "child_id";
        public static final String COLUMN_CHILD_NODE = "child_node";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" + _ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_PARENT_INSTANCE_ID + INT_TYPE + COMMA_SEP +
                COLUMN_PARENT_NODE + TEXT_TYPE + COMMA_SEP +
                COLUMN_PARENT_INDEX + INT_TYPE + COMMA_SEP +
                COLUMN_CHILD_INSTANCE_ID + INT_TYPE + COMMA_SEP +
                COLUMN_CHILD_NODE + TEXT_TYPE + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
