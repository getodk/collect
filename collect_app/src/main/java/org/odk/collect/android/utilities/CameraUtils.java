package org.odk.collect.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.view.Surface;

import org.odk.collect.android.application.Collect;

import timber.log.Timber;

public class CameraUtils {

    private CameraUtils() {

    }

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

    public static Camera getCameraInstance(Activity activity, int cameraId) {
        Camera camera = Camera.open(cameraId);
        camera.setDisplayOrientation(90);

        // Set the rotation of the camera which the output picture need.
        Camera.Parameters parameters = camera.getParameters();
        int rotation = getRotationInt(activity.getWindowManager().getDefaultDisplay().getRotation());
        parameters.setRotation(calcCameraRotation(cameraId, rotation));
        camera.setParameters(parameters);

        return camera;
    }

    private static int getRotationInt(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                Timber.e(new IllegalArgumentException(), "Invalid rotation");
                return -1;
        }
    }

    public static int getFrontCameraId() {
        for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camNo, camInfo);

            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camNo;
            }
        }
        Timber.w("No Available front camera");
        return -1;
    }

    public static int calcCameraRotation(int cameraId, int screenOrientationDegrees) {
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, camInfo);
        return (camInfo.orientation + screenOrientationDegrees) % 360;
    }
}
