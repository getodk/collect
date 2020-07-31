package org.odk.collect.android.formmanagement.previouslydownloaded;


import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.support.InMemFormsRepository;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerFormsUpdateCheckerTest {

    private InMemFormsRepository formRepository;
    private ServerFormsDetailsFetcher serverFormDetailsFetcher;
    private ServerFormsUpdateChecker checker;

    @Before
    public void setup() {
        formRepository = new InMemFormsRepository();
        serverFormDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
        checker = new ServerFormsUpdateChecker(serverFormDetailsFetcher, formRepository);
    }

    @Test
    public void returnsUpdatedForms() throws Exception {
        List<ServerFormDetails> serverForms = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", "manifest-hash", true, false),
                new ServerFormDetails("form-2", "http://example.com/form-1", null, "form-2", "server", "md5:form-2-hash", "manifest-hash", false, true),
                new ServerFormDetails("form-3", "http://example.com/form-1", null, "form-3", "server", "md5:form-3-hash", "manifest-hash", false, true)
        );
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(serverForms);
        saveFormInRepo(serverForms.get(1));
        saveFormInRepo(serverForms.get(2));

        List<ServerFormDetails> newUpdates = checker.check();
        assertThat(newUpdates, containsInAnyOrder(serverForms.get(1), serverForms.get(2)));
    }

    @Test
    public void whenUpdateHasBeenSeenBefore_doesNotIncludeInUpdates() throws Exception {
        List<ServerFormDetails> serverForms = asList(
                new ServerFormDetails("form-1", "http://example.com/form-1", null, "form-1", "server", "md5:form-1-hash", "manifest-hash", false, true)
        );
        when(serverFormDetailsFetcher.fetchFormDetails()).thenReturn(serverForms);
        saveFormInRepo(serverForms.get(0));

        List<ServerFormDetails> newUpdates = checker.check();
        assertThat(newUpdates, containsInAnyOrder(serverForms.get(0)));

        newUpdates = checker.check();
        assertThat(newUpdates, is(empty()));
    }

    public void saveFormInRepo(ServerFormDetails form) {
        formRepository.save(new Form.Builder()
                .jrFormId(form.getFormId())
                .md5Hash(form.getHash())
                .build()
        );
    }
}