package org.odk.collect.android.utilities;

import android.widget.Toast;

import org.odk.collect.android.application.Collect;

/**
 * Created by shobhit on 6/3/17.
 */

public class ToastUtils {
    public static void shortDuration(String message){
        Toast.makeText(Collect.getInstance(),message, Toast.LENGTH_SHORT).show();
    }
    public static void longDuration(String message){
        Toast.makeText(Collect.getInstance(),message, Toast.LENGTH_LONG).show();
    }

}
