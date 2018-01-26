package org.odk.collect.android.injection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.odk.collect.android.injection.scopes.PerActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * @author James Knight
 */

@Module
public abstract class ActivityModule {

    @Binds
    abstract Activity provideActivity(FragmentActivity activity);

    @Binds
    abstract Context bindContext(Activity activity);

    @Provides
    static FragmentManager provideFragmentManager(@NonNull FragmentActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @Provides
    @PerActivity
    static Bundle provideExtras(FragmentActivity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return Bundle.EMPTY;
        }

        Bundle extras = intent.getExtras();

        return extras != null
                ? extras
                : Bundle.EMPTY;
    }
}
