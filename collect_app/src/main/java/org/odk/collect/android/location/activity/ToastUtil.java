package org.odk.collect.android.location.activity;

import android.support.annotation.StringRes;

import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.utilities.ToastUtils;

import javax.inject.Inject;

@PerApplication
public class ToastUtil {

    @Inject
    ToastUtil() {

    }

    public void showShortToast(@StringRes int stringRes) {
        ToastUtils.showShortToast(stringRes);
    }
}
