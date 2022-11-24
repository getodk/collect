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

package org.odk.collect.selfiecamera;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.shared.injection.ObjectProvider;
import org.odk.collect.shared.injection.ObjectProviderHost;
import org.odk.collect.strings.localization.LocalizedActivity;

public class CaptureSelfieActivity extends LocalizedActivity {

    public static final String EXTRA_TMP_IMAGE_PATH = "tmpImagePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectProvider objectProvider = ((ObjectProviderHost) getApplication()).getObjectProvider();
        PermissionsProvider permissionsProvider = objectProvider.provide(PermissionsProvider.class);

        if (!permissionsProvider.isCameraPermissionGranted()) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_selfie);
        if (null == savedInstanceState) {
            Camera2Fragment fragment = new Camera2Fragment();
            Bundle args = new Bundle();
            args.putString(Camera2Fragment.ARG_TMP_IMAGE_PATH, getIntent().getStringExtra(EXTRA_TMP_IMAGE_PATH));
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
        ToastUtils.showLongToast(this, R.string.take_picture_instruction);
    }
}
