package org.odk.collect.android.utilities;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import org.odk.collect.android.application.Collect;

import timber.log.Timber;

/**
 * Created by Akshay on 10/11/17.
 */
@TargetApi(21)
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
                    CameraManager cameraManager = (CameraManager) Collect.getInstance()
                            .getSystemService(Collect.getInstance().CAMERA_SERVICE);

                    String[] cameraId = cameraManager.getCameraIdList();
                    for (int j = 0; j < cameraId.length; j++) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId[j]);
                        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                            return true;
                        }
                    }
                } catch (CameraAccessException e) {
                    Timber.e(e);
                } catch (NullPointerException e) {
                    Timber.e(e);
                }
            }
            return false; // No front-facing camera found
      }
}
