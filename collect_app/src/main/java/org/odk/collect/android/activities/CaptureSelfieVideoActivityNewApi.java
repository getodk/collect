
package org.odk.collect.android.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.fragments.Camera2VideoFragment;
import org.odk.collect.android.utilities.ToastUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CaptureSelfieVideoActivityNewApi extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_selfie_video_new_api);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }
        ToastUtils.showLongToast("Tap to start recording");
    }
}
