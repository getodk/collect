package org.odk.collect.android.utilities;

import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import org.odk.collect.android.application.Collect;

/**
 * Created by Akshay on 10/11/17.
 */

public class CameraUtils {
      public static boolean isFrontCameraAvailable() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //https://developer.android.com/guide/topics/media/camera.html#check-camera-features
                Camera.CameraInfo ci = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, ci);
                    if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        return true;
                    }
                }
            } else {
                //For API Level >= 21
                try {
                    //https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
                    CameraManager cManager = (CameraManager) Collect.getInstance()
                            .getSystemService(Collect.getInstance().CAMERA_SERVICE);
                    String[] cameraId = cManager.getCameraIdList();
                    for (int j = 0; j < cameraId.length; j++) {
                        CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId[j]);
                        int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                            return true;
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            return false; // No front-facing camera found
        }
}
