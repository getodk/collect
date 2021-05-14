package org.odk.collect.android.support;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.testshared.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CollectHelpers {

    private CollectHelpers() {

    }

    public static void overrideReferenceManager(ReferenceManager referenceManager) {
        overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    public static ReferenceManager setupFakeReferenceManager(List<Pair<String, String>> references) throws InvalidReferenceException {
        ReferenceManager referenceManager = mock(ReferenceManager.class);

        for (Pair<String, String> reference : references) {
            createFakeReference(referenceManager, reference.first, reference.second);
        }

        return referenceManager;
    }

    private static String createFakeReference(ReferenceManager referenceManager, String referenceURI, String localURI) throws InvalidReferenceException {
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(localURI);
        when(referenceManager.deriveReference(referenceURI)).thenReturn(reference);

        return localURI;
    }

    public static void overrideAppDependencyModule(AppDependencyModule appDependencyModule) {
        AppDependencyComponent testComponent = DaggerAppDependencyComponent.builder()
                .application(ApplicationProvider.getApplicationContext())
                .appDependencyModule(appDependencyModule)
                .build();
        ((Collect) ApplicationProvider.getApplicationContext()).setComponent(testComponent);
    }

    public static void createThemedContext() {
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);
    }

    public static <T extends FragmentActivity> T createThemedActivity(Class<T> clazz) {
        return RobolectricHelpers.createThemedActivity(clazz, R.style.Theme_Collect_Light);
    }

    public static FragmentActivity createThemedActivity() {
        return createThemedActivity(FragmentActivity.class);
    }

    public static <T extends FragmentActivity> ActivityController<T> buildThemedActivity(Class<T> clazz) {
        ActivityController<T> activity = Robolectric.buildActivity(clazz);
        activity.get().setTheme(R.style.Theme_Collect_Light);

        return activity;
    }
}
