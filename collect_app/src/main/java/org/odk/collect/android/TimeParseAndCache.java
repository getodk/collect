package org.odk.collect.android;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import timber.log.Timber;

public class TimeParseAndCache {
    public static void run(String dir) throws IOException {
        StringBuilder errors = new StringBuilder();
        File formDir = new File(Collect.FORMS_PATH);
        if (formDir.exists() && formDir.isDirectory()) {
            for (File file : formDir.listFiles()) {
                // Ignore invisible files that start with periods.
                String name = file.getName();
                if (!name.startsWith(".") && (name.endsWith(".xml") || name.endsWith(".xhtml"))) {
                    try {
                        timeOperations(Collect.FORMS_PATH + File.separator + name);
                    } catch (Exception e) {
                        errors.append(e.getMessage()).append("\n");
                    }
                }
            }
        }

        if (errors.length() > 0) {
            System.err.printf("Errors:\n%s", errors.toString());
        }
    }

    private static void timeOperations(String xmlFilename) throws IOException {
        long lineCount = getLineCount(xmlFilename);

        long start = System.nanoTime();
        String formHash = FileUtils.getMd5Hash(new File(xmlFilename));
        double hash = timeDiff(start);

        start = System.nanoTime();
        FormDef formDefFromXml = getFormDef(xmlFilename);
        double readParse = timeDiff(start);

        start = System.nanoTime();
        FormLoaderTask.cacheFormDefIfNew(formDefFromXml, xmlFilename, Collect.CACHE_PATH);
        double cacheWrite = timeDiff(start);

        File cachedFormFile = new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");

        start = System.nanoTime();
        FormLoaderTask.deserializeFormDef(cachedFormFile);
        double cacheRead = timeDiff(start);

        Timber.i("%s\t%d\t%d\t%.3f\t%.3f\t%.3f\t%.3f\n",
                formDefFromXml.getTitle(), lineCount, formDefFromXml.getDeepChildCount(),
                hash, readParse, cacheWrite, cacheRead);

        cachedFormFile.delete();
    }

    private static double timeDiff(long start) {
        return (System.nanoTime() - start) / 1000000D;
    }

    private static FormDef getFormDef(String xmlFilename) throws IOException {
        FileInputStream fis = new FileInputStream(xmlFilename);
        FormDef formDefFromXml = XFormUtils.getFormFromInputStream(fis);
        fis.close();
        return formDefFromXml;
    }

    private static long getLineCount(String xmlFilename) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(xmlFilename));
        String line;
        long count = 0;
        while ((line = r.readLine()) != null) {
            if (! line.trim().isEmpty()) {
                ++count;
            }
        }
        r.close();
        return count;
    }
}
