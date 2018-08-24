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

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;

import timber.log.Timber;

public class ItemsetDao {

    public String getItemLabel(String itemName, String mediaFolderPath, String language) {
        String itemLabel = null;

        ItemsetDbAdapter adapter = new ItemsetDbAdapter();
        File itemsetFile =  new FileUtil().getItemsetFile(mediaFolderPath);
        if (itemsetFile.exists()) {
            adapter.open();

            // name of the itemset table for this form
            String pathHash = ItemsetDbAdapter.getMd5FromString(itemsetFile.getAbsolutePath());
            try {
                String selection = "name=?";
                String[] selectionArgs = {itemName};

                Cursor c = adapter.query(pathHash, selection, selectionArgs);
                if (c != null) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        // apparently you only need the double quotes in the
                        // column name when creating the column with a : included
                        String labelLang = "label" + "::" + language;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            itemLabel = c.getString(c.getColumnIndex("label"));
                        } else {
                            itemLabel = c.getString(c.getColumnIndex(labelLang));
                        }

                    }
                    c.close();
                }
            } catch (SQLiteException e) {
                Timber.i(e);
            } finally {
                adapter.close();
            }
        }

        return itemLabel;
    }
}
