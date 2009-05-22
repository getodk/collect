package org.google.android.odk;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

abstract class FileChooser extends ListActivity {

    private final String t = "File Chooser";

    protected ArrayList<String> mFileList;
    protected File mRoot;
    private boolean mRadio;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setTheme(SharedConstants.APPLICATION_THEME);

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        setContentView(R.layout.filelister);


    }


    public void initialize(String title, String path, Boolean radio) {
        setTitle(getString(R.string.app_name) + " > " + title);
        mRadio = radio;
        mFileList = new ArrayList<String>();
        if (getDirectory(path)) {
            getFiles(mRoot);
            displayFiles();
        }

    }


    public void refreshRoot() {
        getFiles(mRoot);
        displayFiles();
    }


    private boolean getDirectory(String path) {

        TextView tv = (TextView) findViewById(R.id.filelister_message);

        // check to see if there's an sd card.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            tv.setText(getString(R.string.sdcard_error));
            return false;
        }

        // if storage directory does not exist, create it.
        boolean made = true;
        mRoot = new File(path);
        if (!mRoot.exists()) {
            made = mRoot.mkdirs();
        }

        if (!made) {
            tv.setText(getString(R.string.directory_error, path));
            return false;
        } else {
            return true;
        }
    }



    private void getFiles(File f) {
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
                getFiles(child);
            }
            return;
        }
        String filename = f.getName();
        if (filename.matches(SharedConstants.VALID_FILENAME)) {
            mFileList.add(filename);
        }
    }



    /**
     * Opens the directory, puts valid files in array adapter for display
     */
    private void displayFiles() {

        ArrayAdapter<String> fileAdapter;
        Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);

        if (mRadio) {
            getListView().setItemsCanFocus(false);
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            fileAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                            mFileList);
        } else {
            fileAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFileList);
        }

        setListAdapter(fileAdapter);

    }


    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File f = new File(mRoot + "/" + mFileList.get(position));

        Intent i = new Intent();
        i.putExtra(SharedConstants.FILEPATH_KEY, f.getAbsolutePath());
        setResult(RESULT_OK, i);

        finish();
    }


}
