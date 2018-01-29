package org.odk.collect.android;

import android.view.View;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import timber.log.Timber;

/** Times form parsing and caching, and writes to the log with Timber */
public class TimeParseAndCache {
    public static void run(View view) {
        new Thread(() -> {
            StringBuilder errors = new StringBuilder();
            File formDir = new File(Collect.FORMS_PATH);
            if (formDir.exists() && formDir.isDirectory()) {
                Timber.i("Title\tLines\tChildren\tNon-Main Instances\tOutput Fragments\t" +
                        "Triggerables\tHash\tRead/Parse\tCache Write\tCache Read\n");
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
            view.post(() -> ToastUtils.showShortToast(
                    "Form processing timing information has been logged."));

            if (errors.length() > 0) {
                Timber.e("Errors:\n%s", errors.toString());
            }
        }).start();

        ToastUtils.showShortToast("Form loading timing is starting.");
    }

    private static void timeOperations(String xmlFilename) throws IOException {
        // Hashing
        long start = System.nanoTime();
        final String formHash = FileUtils.getMd5Hash(new File(xmlFilename));
        final double md5HashingTime = timeDiff(start);

        // Reading and Parsing
        start = System.nanoTime();
        FormDef formDefFromXml = getFormDef(xmlFilename);
        final double readParseTime = timeDiff(start);

        // Writing to cache
        start = System.nanoTime();
        FormLoaderTask.cacheFormDefIfNew(formDefFromXml, xmlFilename, Collect.CACHE_PATH);
        final double cacheWriteTime = timeDiff(start);

        // Reading from cache
        File cachedFormFile = new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");
        start = System.nanoTime();
        FormLoaderTask.deserializeFormDef(cachedFormFile);
        final double cacheReadTime = timeDiff(start);
        cachedFormFile.delete();

        Timber.i("%s\t%d\t%d\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f\t%.3f\n",
                formDefFromXml.getTitle(), getLineCount(xmlFilename), formDefFromXml.getDeepChildCount(),
                countNonMainInstances(formDefFromXml.getNonMainInstances()),
                formDefFromXml.getOutputFragments().size(),
                formDefFromXml.getFormComplexityMetrics().numTriggerables,
                md5HashingTime, readParseTime, cacheWriteTime, cacheReadTime);
    }

    private static int countNonMainInstances(Enumeration<DataInstance> nonMainInstances) {
        int num = 0;
        while (nonMainInstances.hasMoreElements()) {
            ++num;
            nonMainInstances.nextElement();
        }
        return num;
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
