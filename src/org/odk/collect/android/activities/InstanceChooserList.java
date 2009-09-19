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

package org.odk.collect.android.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        buildView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {

        // get full path to the instance
        Cursor c = (Cursor) getListAdapter().getItem(position);
        String instancePath = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));

        // create intent for return and store path
        Intent i = new Intent();
        i.putExtra(GlobalConstants.KEY_INSTANCEPATH, instancePath);
        i.putExtra(GlobalConstants.KEY_FORMPATH, getFormPathFromInstancePath(instancePath));

        // return the result to the parent class
        getParent().setResult(RESULT_OK, i);

        // don't close cursor or tab host delays closing
        finish();
    }


    /**
     * Retrieves instance information from {@link FileDbAdapter}, composes and
     * displays each row.
     */
    private void buildView() {

        // retrieve status information from instance. needed for tabs.
        Intent i = getIntent();
        String status = i.getStringExtra(FileDbAdapter.KEY_STATUS);

        // get all instances that match the status.
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        Cursor c = fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, status);
        startManagingCursor(c);

        // create data and views for cursor adapter
        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {android.R.id.text1, android.R.id.text2};

        // render total instance view
        SimpleCursorAdapter instances =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        if (c.getCount() > 0) {
            setListAdapter(instances);
        } else {
            setContentView(R.layout.list_view_empty);
        }

        // cleanup
        fda.close();

    }


    /**
     * Given an instance path, return the full path to the form
     * 
     * @param instancePath full path to the instance
     * @return formPath full path to the form the instance was generated from
     */
    private String getFormPathFromInstancePath(String instancePath) {

        // trim the timestamp
        String regex = "\\_[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\_[0-9]{2}\\-[0-9]{2}\\-[0-9]{2}\\.xml$";
        Pattern pattern = Pattern.compile(regex);
        String formName = pattern.split(instancePath)[0];
        formName = formName.substring(formName.lastIndexOf("/") + 1);

        File xmlFile = new File(GlobalConstants.FORMS_PATH + "/" + formName + ".xml");
        File xhtmlFile = new File(GlobalConstants.FORMS_PATH + "/" + formName + ".xhtml");

        // form is either xml or xhtml file. find the appropriate one.
        if (xmlFile.exists()) {
            return xmlFile.getAbsolutePath();
        } else if (xhtmlFile.exists()) {
            return xhtmlFile.getAbsolutePath();
        } else {
            return null;
        }
    }


}
