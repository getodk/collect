package org.odk.collect.android.http;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import org.odk.collect.android.http.ContentTypeMapping;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.utilities.FileUtils;
import org.opendatakit.httpclientandroidlib.entity.ContentType;

public class CollectThenSystemContentTypeMapper implements OpenRosaHttpInterface.FileToContentTypeMapper {

    @NonNull
    @Override
    public String map(String fileName) {
        ContentType contentType = ContentTypeMapping.of(fileName);
        if (contentType == null) {
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(FileUtils.getFileExtension(fileName));
            if (mime != null) {
                contentType = ContentType.create(mime);
            } else {
                contentType = ContentType.APPLICATION_OCTET_STREAM;
            }
        }

        return contentType.getMimeType();
    }
}
