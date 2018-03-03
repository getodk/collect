package org.odk.collect.android.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import org.odk.collect.android.application.Collect;

import timber.log.Timber;

public class CameraUtils {
    public static boolean isFrontCameraAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        } else {
            //https://developer.android.com/guide/topics/media/camera.html#check-camera-features
            for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(camNo, camInfo);
                if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return true;
                }
            }
            return false; // No front-facing camera found
        }
    }
}
