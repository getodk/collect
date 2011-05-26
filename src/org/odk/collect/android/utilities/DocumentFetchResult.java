package org.odk.collect.android.utilities;

import org.kxml2.kdom.Document;
 
public class DocumentFetchResult {
        public final String errorMessage;
        public final int responseCode;
        public final Document doc;
        public final boolean isOpenRosaResponse;


        public DocumentFetchResult(String msg, int response) {
            responseCode = response;
            errorMessage = msg;
            doc = null;
            isOpenRosaResponse = false;
        }


        public DocumentFetchResult(Document doc, boolean isOpenRosaResponse) {
            responseCode = 0;
            errorMessage = null;
            this.doc = doc;
            this.isOpenRosaResponse = isOpenRosaResponse;
        }
    }