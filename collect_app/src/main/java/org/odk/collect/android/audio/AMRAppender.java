package org.odk.collect.android.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AMRAppender implements AudioFileAppender {

    public static final int AMR_HEADER_BYTES = 6;

    @Override
    public void append(File one, File two) throws IOException {
        FileOutputStream fos = new FileOutputStream(one, true);
        FileInputStream fis = new FileInputStream(two);

        byte[] fileContent = new byte[(int) two.length()];
        fis.read(fileContent);

        byte[] headerlessFileContent = new byte[fileContent.length - AMR_HEADER_BYTES];
        if (fileContent.length - AMR_HEADER_BYTES >= 0) {
            System.arraycopy(fileContent, AMR_HEADER_BYTES, headerlessFileContent, 0, fileContent.length - AMR_HEADER_BYTES);
        }

        fileContent = headerlessFileContent;
        fos.write(fileContent);
    }
}
