package org.odk.collect.android.injection;

import org.odk.collect.android.fragments.DataManagerList;
import org.odk.collect.android.fragments.FormManagerList;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Module for binding injectable Fragments.
 */
@Module
public abstract class FragmentBuilder {

    @PerActivity
    @ContributesAndroidInjector
    abstract ShowQRCodeFragment showQRCodeFragment();

    @PerActivity
    @ContributesAndroidInjector
    abstract FormManagerList formManagerList();

    @PerActivity
    @ContributesAndroidInjector
    abstract DataManagerList dataManagerList();
}
