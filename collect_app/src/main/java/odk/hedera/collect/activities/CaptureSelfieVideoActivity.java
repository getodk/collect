
package odk.hedera.collect.activities;

/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.os.Bundle;

import org.odk.hedera.collect.R;
import odk.hedera.collect.fragments.Camera2VideoFragment;
import odk.hedera.collect.utilities.ToastUtils;

import static odk.hedera.collect.utilities.PermissionUtils.areCameraAndRecordAudioPermissionsGranted;

public class CaptureSelfieVideoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!areCameraAndRecordAudioPermissionsGranted(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_capture_selfie_video);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }
        ToastUtils.showLongToast(getString(R.string.start_video_capture_instruction));
    }
}
