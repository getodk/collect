package org.odk.collect.android.http;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import org.odk.collect.android.utilities.FileUtils;
import org.opendatakit.httpclientandroidlib.entity.ContentType;

public class CollectThenSystemContentTypeMapper implements OpenRosaHttpInterface.FileToContentTypeMapper {

    private final MimeTypeMap androidTypeMap;

    public CollectThenSystemContentTypeMapper(MimeTypeMap androidTypeMap) {
        this.androidTypeMap = androidTypeMap;
    }

    @NonNull
    @Override
    public String map(String fileName) {
        ContentType contentType = ContentTypeMapping.of(fileName);

        if (contentType == null) {
            String mime = androidTypeMap.getMimeTypeFromExtension(FileUtils.getFileExtension(fileName));
            if (mime != null) {
                contentType = ContentType.create(mime);
            } else {
                contentType = ContentType.APPLICATION_OCTET_STREAM;
            }
        }

        return contentType.getMimeType();
    }

    private enum ContentTypeMapping {
        XML("xml",  ContentType.TEXT_XML),
        _3GPP("3gpp", ContentType.create("audio/3gpp")),
        _3GP("3gp",  ContentType.create("video/3gpp")),
        AVI("avi",  ContentType.create("video/avi")),
        AMR("amr",  ContentType.create("audio/amr")),
        CSV("csv",  ContentType.create("text/csv")),
        JPG("jpg",  ContentType.create("image/jpeg")),
        MP3("mp3",  ContentType.create("audio/mp3")),
        MP4("mp4",  ContentType.create("video/mp4")),
        OGA("oga",  ContentType.create("audio/ogg")),
        OGG("ogg",  ContentType.create("audio/ogg")),
        OGV("ogv",  ContentType.create("video/ogg")),
        WAV("wav",  ContentType.create("audio/wav")),
        WEBM("webm", ContentType.create("video/webm")),
        XLS("xls",  ContentType.create("application/vnd.ms-excel"));

        private String extension;
        private ContentType contentType;

        ContentTypeMapping(String extension, ContentType contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public static ContentType of(String fileName) {
            String extension = FileUtils.getFileExtension(fileName);

            for (ContentTypeMapping m : ContentTypeMapping.values()) {
                if (m.extension.equals(extension)) {
                    return m.contentType;
                }
            }

            return null;
        }
    }
}
