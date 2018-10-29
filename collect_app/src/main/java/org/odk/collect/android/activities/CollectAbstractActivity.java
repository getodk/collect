/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppComponent;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.ThemeUtils;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfStoragePermissionsGranted;

public abstract class CollectAbstractActivity extends AppCompatActivity {

    private boolean isInstanceStateSaved;
    protected ThemeUtils themeUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        themeUtils = new ThemeUtils(this);
        setTheme(this instanceof FormEntryActivity ? themeUtils.getFormEntryActivityTheme() : themeUtils.getAppTheme());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /**
         * If the user has revoked the storage permission then this check ensures the app doesn't
         * quit unexpectedly and starts {@link  StoragePermissionActivity} which informs the user
         * that the app can't function without storage permission and gives an option to re-allow it
         * or ignore the warning which quits the app.
         *
         * Since all of the activities extend {@link CollectAbstractActivity}, so this also removes
         * the overhead of checking for this permission separately in each of the activities.
         *
         * We also finish this activity and later re-create it from within {@link StoragePermissionActivity}
         * if the user decides to re-allow the permission
         */
        if (!checkIfStoragePermissionsGranted(this)) {
            StoragePermissionActivity.startActivity(this, getIntent(), getClass());
            finish();
        } else {
            /*
             * Storage permission is already given to the app.
             * So, we can proceed with using storage related functions
             */
            init();
        }
    }

    /**
     * This method should be overridden wherever storage permission check is required before
     * initializing the activity
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void init() {
    }

    public AppComponent getComponent() {
        return Collect.getInstance().getComponent();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isInstanceStateSaved = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        isInstanceStateSaved = true;
        super.onSaveInstanceState(outState);
    }

    public boolean isInstanceStateSaved() {
        return isInstanceStateSaved;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new LocaleHelper().updateLocale(base));
    }
}
