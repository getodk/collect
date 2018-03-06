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

    private static String formHash;
    private static FormDef form;

    public void run(View view) {
        new Thread(null, () -> {
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
                            timeOperations(view, name);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        }, getClass().getSimpleName()).start();

        ToastUtils.showShortToast("Form loading timing is starting.");
    }

    private void timeOperations(View view, String xmlFilename) throws IOException {
        final String fullXmlFilename = Collect.FORMS_PATH + File.separator + xmlFilename;
        final Timer t = new Timer(view, xmlFilename);

        final double md5HashingTime = t.time("Hashing", () ->
                formHash = FileUtils.getMd5Hash(new File(fullXmlFilename)));

        final double readParseTime = t.time("Reading and Parsing", () ->
                form = getFormDef(fullXmlFilename));

        final double cacheWriteTime = t.time("Writing to cache", () ->
                FormLoaderTask.cacheFormDefIfNew(form, fullXmlFilename, Collect.CACHE_PATH));

        final double cacheReadTime = t.time("Reading from cache", () -> {
            File cachedFormFile =
                    new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");
            FormLoaderTask.deserializeFormDef(cachedFormFile);
            cachedFormFile.delete();
        });

        Timber.i("%s\t%d\t%d\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f\t%.3f\n",
                form.getTitle(), getLineCount(fullXmlFilename),
                form.getDeepChildCount(),
                countNonMainInstances(form.getNonMainInstances()),
                form.getOutputFragments().size(),
                form.getFormComplexityMetrics().numTriggerables,
                md5HashingTime, readParseTime, cacheWriteTime, cacheReadTime);
    }

    /** Times operations */
    private class Timer {
        final View view;
        final String filename;

        Timer(View view, String filename) {
            this.view = view;
            this.filename = filename;
        }

        /** Returns the number of milliseconds consumed by the runnable */
        double time(String activity, Runnable runnable) {
            String message = activity + " " + filename;
            Timber.d(message);
            view.post(() -> ToastUtils.showShortToast(message));

            long start = System.nanoTime();
            runnable.run();
            return timeDiffMs(start);
        }
    }

    private int countNonMainInstances(Enumeration<DataInstance> nonMainInstances) {
        int num = 0;
        while (nonMainInstances.hasMoreElements()) {
            ++num;
            nonMainInstances.nextElement();
        }
        return num;
    }

    private double timeDiffMs(long start) {
        return (System.nanoTime() - start) / 1000000D;
    }

    private FormDef getFormDef(String xmlFilename) {
        try {
            FileInputStream fis = new FileInputStream(xmlFilename);
            FormDef formDefFromXml = XFormUtils.getFormFromInputStream(fis);
            fis.close();
            return formDefFromXml;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getLineCount(String xmlFilename) throws IOException {
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
