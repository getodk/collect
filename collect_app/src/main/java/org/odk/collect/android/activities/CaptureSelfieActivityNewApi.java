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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.Camera2Fragment;
import org.odk.collect.android.utilities.ToastUtils;

import timber.log.Timber;

import static org.odk.collect.android.utilities.PermissionUtils.checkIfCameraPermissionGranted;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CaptureSelfieActivityNewApi extends CollectAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIfCameraPermissionGranted(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_selfie_new_api);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2Fragment.newInstance())
                    .commit();
        }
        ToastUtils.showLongToast(R.string.take_picture_instruction);
    }

    public static boolean isFrontCameraAvailable() {
        try {
            //https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
            CameraManager cameraManager = (CameraManager) Collect.getInstance()
                    .getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                String[] cameraId = cameraManager.getCameraIdList();
                for (String id : cameraId) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return true;
                    }
                }
            }
        } catch (CameraAccessException | NullPointerException e) {
            Timber.e(e);
        }
        return false; // No front-facing camera found
    }
}