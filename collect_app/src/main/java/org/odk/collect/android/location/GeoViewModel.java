package org.odk.collect.android.location;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;


@PerViewModel
public class GeoViewModel extends RxMVVMViewModel {

    private BehaviorRelay<Boolean> isReloadEnabled = BehaviorRelay.createDefault(false);
    private BehaviorRelay<Boolean> isShowEnabled = BehaviorRelay.createDefault(false);

    @Inject
    GeoViewModel() {

    }

    Observable<Boolean> isReloadEnabled() {
        return isReloadEnabled.hide();
    }

    Observable<Boolean> isShowEnabled() {
        return isShowEnabled.hide();
    }
}
