package org.odk.collect.android.support;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class Helpers {

    private Helpers() {

    }

    public static void setupMockReference(String uri, String referenceURI, ReferenceManager referenceManager) throws InvalidReferenceException {
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(referenceURI);
        when(referenceManager.deriveReference(uri)).thenReturn(reference);
    }
}
