/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.test;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

public class FormLoadingUtils {
    private static final String FORMS_PATH = Environment.getExternalStorageDirectory().getPath() + "/odk/forms/";
    public static final String ALL_WIDGETS_FORM = "all-widgets.xml";

    private FormLoadingUtils() {
        
    }

    /**
     * Copies a form with the given file name from the given assets folder to the SD Card where it
     * will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename, String formAssetPath) throws IOException {
        String pathname = FORMS_PATH + formFilename;

        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        InputStream inputStream = assetManager.open(formAssetPath + formFilename);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    /**
     * Copies a form with the given file name from the assets root to the SD Card where it
     * will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename) throws IOException {
        copyFormToSdCard(formFilename, "");
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new IntentsTestRule<FormEntryActivity>(FormEntryActivity.class) {
            @Override
            protected Intent getActivityIntent() {
                Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
                intent.putExtra(EXTRA_TESTING_PATH, FORMS_PATH + formFilename);

                return intent;
            }

            @Override
            protected void afterActivityLaunched() {
                this.getActivity().setShouldOverrideAnimations(true);
                super.afterActivityLaunched();
            }
        };
    }
}
