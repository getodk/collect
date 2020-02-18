package org.odk.collect.android.support;

import androidx.core.util.Pair;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.injection.config.AppDependencyModule;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CollectHelpers {

    private CollectHelpers() {

    }

    public static void overrideReferenceManager(ReferenceManager referenceManager) {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
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
}
