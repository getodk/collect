package org.odk.collect.android.http;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import org.odk.collect.android.utilities.FileUtils;

public class CollectThenSystemContentTypeMapper implements OpenRosaHttpInterface.FileToContentTypeMapper {

    private final MimeTypeMap androidTypeMap;

    public CollectThenSystemContentTypeMapper(MimeTypeMap androidTypeMap) {
        this.androidTypeMap = androidTypeMap;
    }

    @NonNull
    @Override
    public String map(String fileName) {
        String extension = FileUtils.getFileExtension(fileName);

        String collectContentType = CollectContentTypeMappings.of(extension);
        String androidContentType = androidTypeMap.getMimeTypeFromExtension(extension);

        if (collectContentType != null) {
            return collectContentType;
        } else if (androidContentType != null) {
            return androidContentType;
        } else {
            return "application/octet-stream";
        }
    }

    private enum CollectContentTypeMappings {
        XML("xml",  "text/xml"),
        _3GPP("3gpp", "audio/3gpp"),
        _3GP("3gp",  "video/3gpp"),
        AVI("avi",  "video/avi"),
        AMR("amr",  "audio/amr"),
        CSV("csv",  "text/csv"),
        JPG("jpg",  "image/jpeg"),
        MP3("mp3",  "audio/mp3"),
        MP4("mp4",  "video/mp4"),
        OGA("oga",  "audio/ogg"),
        OGG("ogg",  "audio/ogg"),
        OGV("ogv",  "video/ogg"),
        WAV("wav",  "audio/wav"),
        WEBM("webm", "video/webm"),
        XLS("xls",  "application/vnd.ms-excel");

        private String extension;
        private String contentType;

        CollectContentTypeMappings(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public static String of(String extension) {
            for (CollectContentTypeMappings m : CollectContentTypeMappings.values()) {
                if (m.extension.equals(extension)) {
                    return m.contentType;
                }
            }

            return null;
        }
    }
}
