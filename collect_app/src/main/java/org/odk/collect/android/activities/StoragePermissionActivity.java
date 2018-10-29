/*
 * Copyright (C) 2018 Shobhit Agarwal
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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ThemeUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StoragePermissionActivity extends AppCompatActivity implements PermissionListener {

    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_CLASS = "extra_class";
    public static final String EXTRA_URI = "extra_uri";
    public static final String EXTRA_BUNDLE = "extras_bundle";

    public static void startActivity(Activity activity, Intent callingIntent, Class<?> clazz) {
        Intent intent = new Intent(activity, StoragePermissionActivity.class);
        intent.putExtra(EXTRA_ACTION, callingIntent.getAction());
        intent.putExtra(EXTRA_CLASS, clazz);
        intent.putExtra(EXTRA_URI, callingIntent.getData());

        // sometimes an activity is started along with some extras, so we need to persist those too
        if (callingIntent.getExtras() != null) {
            intent.putExtra(EXTRA_BUNDLE, callingIntent.getExtras());
        }

        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(new ThemeUtils(this).getAppTheme());
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.storage_permission_layout);
        ButterKnife.bind(this);
    }

    @Override
    public void granted() {

        // Create ODK Directories
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            DialogUtils.showDialog(DialogUtils.createErrorDialog(this, e.getMessage(), true), this);
            return;
        }

        Class<?> clazz = (Class<?>) getIntent().getSerializableExtra(EXTRA_CLASS);

        Intent intent = new Intent(this, clazz);

        String action = getIntent().getStringExtra(EXTRA_ACTION);
        if (action != null) {
            intent.setAction(action);
        }

        Uri uri = getIntent().getParcelableExtra(EXTRA_URI);
        if (uri != null) {
            intent.setData(uri);
        }

        Bundle bundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
        if (bundle != null) {
            intent.putExtras(bundle);
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void denied() {
        PermissionUtils.finishAllActivities(this);
    }

    @OnClick({R.id.btnPermission, R.id.btnCancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnPermission:
                PermissionUtils.requestStoragePermissions(this, this);
                break;
            case R.id.btnCancel:
                PermissionUtils.finishAllActivities(this);
                break;
        }
    }
}
