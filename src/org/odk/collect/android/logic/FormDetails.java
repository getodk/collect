package org.odk.collect.android.logic;

import java.io.Serializable;

public class FormDetails implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final String errorStr;

    public final String formName;
    public final String downloadUrl;
    public final String manifestUrl;
    public final String formID;


    public FormDetails(String error) {
        manifestUrl = null;
        downloadUrl = null;
        formName = null;
        formID = null;
        errorStr = error;
    }


    public FormDetails(String name, String url, String manifest, String id) {
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        errorStr = null;
    }

}
