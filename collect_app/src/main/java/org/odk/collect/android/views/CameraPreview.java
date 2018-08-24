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

package org.odk.collect.android.views;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import timber.log.Timber;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder holder;
    private final Camera camera;
    private final Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera == null) {
                ((Activity) context).finish();
                return;
            }
            camera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(camera);
            camera.startPreview();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.release();
                Timber.i("Camera released");
            } catch (Exception e) {
                Timber.d("Camera has been already released");
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.holder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            Timber.e(e);
        }
        try {
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void setCameraDisplayOrientation(Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(1, info);
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}