package org.odk.collect.android.support;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CollectHelpers {

    private CollectHelpers() {

    }

    public static void setupFakeReferenceManager(ReferenceManager referenceManager) throws InvalidReferenceException {
        when(referenceManager.deriveReference(any())).thenThrow(InvalidReferenceException.class);
    }

    public static String createFakeReference(ReferenceManager referenceManager, String referenceURI) throws InvalidReferenceException {
        String localURI = UUID.randomUUID().toString();
        return createFakeReference(referenceManager, referenceURI, localURI);
    }

    public static String createFakeReference(ReferenceManager referenceManager, String referenceURI, String localURI) throws InvalidReferenceException {
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(localURI);
        when(referenceManager.deriveReference(referenceURI)).thenReturn(reference);

        return localURI;
    }

    public static String createFakeBitmapReference(ReferenceManager referenceManager, String referenceURI, String bitmapName) throws InvalidReferenceException, IOException {
        return createFakeReference(
                referenceManager,
                referenceURI,
                File.createTempFile(bitmapName, ".bmp").getAbsolutePath()
        );
    }
}
