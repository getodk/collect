package org.odk.collect.android.tasks;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;
//import org.junit.Test;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class TimeParseAndCache {
    //@Test
    public void timeAFile() throws IOException {
        FormController.initializeJavaRosa(null);

        PrintStream p = new PrintStream(new File("timings.tsv"));
        p.println("Title\tChildren\tHash\tRead/Parse\tCache Write\tCache Read");
        String path = "opendatakit/sample-forms/"; // todo change to directory with forms

        StringBuilder errors = new StringBuilder();
        Path dir = Paths.get(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (file.toFile().isFile()) {
                    try {
                        timeOperations(path + file.getFileName().toString(), p);
                    } catch (Exception e) {
                        errors.append(e.getMessage()).append("\n");
                    }
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }

        p.close();
        if (errors.length() > 0) {
            System.err.printf("Errors:\n%s", errors.toString());
        }
    }

    private void timeOperations(String xmlFilename, PrintStream p) throws IOException {
        long start = System.nanoTime();
        String formHash = FileUtils.getMd5Hash(new File(xmlFilename));
        double hash = timeDiff(start);

        start = System.nanoTime();
        FormDef formDefFromXml = getFormDef(xmlFilename);
        assertNotNull(formDefFromXml);
        double readParse = timeDiff(start);

        start = System.nanoTime();
        boolean added = FormLoaderTask.cacheFormDefIfNew(formDefFromXml, xmlFilename, "/tmp");
        assertTrue(added);
        double cacheWrite = timeDiff(start);

        File cachedFormFile = new File("/tmp" + File.separator + formHash + ".formdef");
        assertTrue(cachedFormFile.exists());

        start = System.nanoTime();
        FormDef fd = FormLoaderTask.deserializeFormDef(cachedFormFile);
        assertNotNull(fd);
        double cacheRead = timeDiff(start);

        p.printf("%s\t%d\t%.3f\t%.3f\t%.3f\t%.3f\n",
                formDefFromXml.getTitle(), formDefFromXml.getDeepChildCount(),
                hash, readParse, cacheWrite, cacheRead);

        boolean deleted = cachedFormFile.delete();
        assertTrue(deleted);
    }

    private double timeDiff(long start) {
        return (System.nanoTime() - start) / 1000000D;
    }

    private FormDef getFormDef(String xmlFilename) throws IOException {
        FileInputStream fis = new FileInputStream(xmlFilename);
        FormDef formDefFromXml = XFormUtils.getFormFromInputStream(fis);
        fis.close();
        return formDefFromXml;
    }
}
