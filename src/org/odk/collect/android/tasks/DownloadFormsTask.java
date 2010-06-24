/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.utilities.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for downloading forms from urls or a formlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a form list. If a hashmap
 * containing form/url pairs is passed, we download those forms.
 * 
 * @author carlhartung
 * 
 */
public class DownloadFormsTask extends
        AsyncTask<HashMap<String, String>, String, HashMap<String, String>> {

    FormDownloaderListener mStateListener;
    ArrayList<String> mDownloadedForms = new ArrayList<String>();


    @Override
    protected HashMap<String, String> doInBackground(HashMap<String, String>... values) {
        FileDbAdapter fda = null;
        if (values != null && values[0].containsKey(FormDownloadList.LIST_URL)) {
            // This gets a list of available forms from the specified server.
            HashMap<String, String> formList = new HashMap<String, String>();
            URL u = null;
            try {
                u = new URL((String) values[0].get(FormDownloadList.LIST_URL));
            } catch (MalformedURLException e) {
                formList.put(FormDownloadList.DL_ERROR, e.getLocalizedMessage());
                e.printStackTrace();
            }

            try {
                // prevent deadlock when connection is invalid
                URLConnection c = u.openConnection();
                c.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
                c.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);

                // write connection to file
                InputStream is = c.getInputStream();

                Document doc = null;
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    doc = db.parse(is);
                } catch (Exception e) {
                    formList.put(FormDownloadList.DL_ERROR, "DocumentBuilderFactory error: "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                }

                // populate HashMap with form names and urls
                int formCount = 0;
                if (doc != null) {
                    NodeList formElements = doc.getElementsByTagName("form");
                    formCount = formElements.getLength();
                    for (int i = 0; i < formCount; i++) {
                        Node n = formElements.item(i);
                        formList.put(n.getChildNodes().item(0).getNodeValue() + ".xml", n
                                .getAttributes().item(0).getNodeValue());
                    }
                }
            } catch (IOException e) {
                formList.put(FormDownloadList.DL_ERROR, e.getLocalizedMessage());
                e.printStackTrace();
            }
            return formList;


        } else {
            // This downloads the selected forms.
            HashMap<String, String> toDownload = values[0];
            String title = null;
            String message = null;
            HashMap<String, String> renamedForms = new HashMap<String, String>();
            boolean error = false;

            HashMap<String, String> result = new HashMap<String, String>();
            ArrayList<String> formNames = new ArrayList<String>(toDownload.keySet());
          
            int total = formNames.size();
            int count = 1;
            fda = new FileDbAdapter();
            fda.open();
            
            for (int i = 0; i < total; i++) {
                String form = formNames.get(i);
                publishProgress(form, new Integer(count).toString(), new Integer(total).toString());
                try {
                    File dl = downloadFile(form, toDownload.get(form));
                    // hash of raw form
                    String hasha = FileUtils.getMd5Hash(dl);
                    Log.e("carl", "file = " + dl.getName() + " and hasha = " + hasha);
                    
                    // if the file already existed, the name will be changed to formname_#
                    if (form.compareTo(dl.getName()) != 0) {
                        // hash of raw form
                        String hash = FileUtils.getMd5Hash(dl);
                        Log.e("carl", "form = " + dl.getName() + " and hash = " + hash);

                        Cursor c = fda.fetchFilesByPath(null, hash);
                        if (c.getCount() > 0) {
                            // db has the hash and this is a duplicate.  the dupliate will be discarded.
                        } else {
                            // the form is new, but the file name was the same.
                            // tell the user we renamed the form.
                            Log.e("carl", "adding " + form + " as " + dl.getName());
                            renamedForms.put(form, dl.getName());
                        }

                        c.close();
                    }
                } catch (IOException e) {
                    message = "File " + form + " failed with error: " + e.getMessage();
                    title = "Error Downloading";
                    e.printStackTrace();
                    error = true;
                    break;
                }

                count++;
            }

            if (!error) {
                // download was complete, add the names of forms that were renamed
                title = "Download complete";
                message = "All downloads completed successfully.";
                Set<String> keys = renamedForms.keySet();
                Iterator<String> i = keys.iterator();
                while (i.hasNext()) {
                    String form = i.next();
                    message += " " + form + " was renamed " + renamedForms.get(form) + ".";
                }

            }

            if (fda != null) {
                // addOrphanForms will remove duplicates, and add new forms to the database
                fda.addOrphanForms();
                fda.close();
            }

            result.put(FormDownloadList.DIALOG_TITLE, title);
            result.put(FormDownloadList.DIALOG_MSG, message);
            result.put(FormDownloadList.FILES_DOWNLOADED, "null");
            return result;
        }
    }


    private File downloadFile(String name, String url) throws IOException {
        // create url
        URL u = null;
        File f = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // prevent deadlock when connection is invalid
            URLConnection c = u.openConnection();
            c.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
            c.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);

            // write connection to file
            InputStream is = c.getInputStream();

            String path = GlobalConstants.FORMS_PATH + name;
            int i = 2;
            int slash = path.lastIndexOf("/") + 1;
            int period = path.lastIndexOf(".") + 1;
            String base = path.substring(0, slash - 1);
            String filename = path.substring(slash, period - 1);
            String ext = path.substring(period);
            f = new File(path);
            while (f.exists()) {
                f = new File(base + "/" + filename + "_" + i + "." + ext);
                i++;
            }

            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return f;
    }


    @Override
    protected void onPostExecute(HashMap<String, String> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.formDownloadingComplete(value);
            }
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0], new Integer(values[1]).intValue(),
                        new Integer(values[2]).intValue());
            }
        }

    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
}
