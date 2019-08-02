package org.odk.collect.android.support;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class Helpers {

    private Helpers() {

    }

    public static void setupMockReference(String uri, ReferenceManager referenceManager) throws InvalidReferenceException {
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(uri);
        when(referenceManager.deriveReference(uri)).thenReturn(reference);
    }

    public static FormEntryPrompt buildMockForm() {
        FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

        when(formEntryPrompt.getIndex()).thenReturn(mock(FormIndex.class));
        when(formEntryPrompt.getFormElement()).thenReturn(mock(IFormElement.class));

        return formEntryPrompt;
    }
}
