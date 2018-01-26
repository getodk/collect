package org.odk.collect.android.location.domain.utility;

import android.support.annotation.StringRes;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.utilities.ToastUtils;

import javax.inject.Inject;

@PerActivity
public class ToastUtil {

    @Inject
    ToastUtil() {

    }

    public void showShortToast(@StringRes int stringRes) {
        ToastUtils.showShortToast(stringRes);
    }
}
