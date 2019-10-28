package org.odk.collect.android.support;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class Helpers {

    private Helpers() {

    }

    public static String createMockReference(ReferenceManager referenceManager, String uri) throws InvalidReferenceException {
        String referenceURI = UUID.randomUUID().toString();

        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(referenceURI);
        when(referenceManager.deriveReference(uri)).thenReturn(reference);

        return referenceURI;
    }
}
