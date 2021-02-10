package org.odk.collect.android.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AMRAppender implements AudioFileAppender {

    @Override
    public void append(File one, File two) throws IOException {
        FileOutputStream fos = new FileOutputStream(one, true);
        FileInputStream fis = new FileInputStream(two);

        byte[] fileContent = new byte[(int) two.length()];
        fis.read(fileContent);

        byte[] headerlessFileContent = new byte[fileContent.length - 6];
        if (fileContent.length - 6 >= 0) {
            System.arraycopy(fileContent, 6, headerlessFileContent, 0, fileContent.length - 6);
        }

        fileContent = headerlessFileContent;
        fos.write(fileContent);
    }
}
