package org.odk.collect.android.configure;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesServerRepositoryTest {

    private String defaultServer;

    @Before
    public void setup() {
        defaultServer = Collect.getInstance().getString(R.string.default_server_url);
    }

    @Test
    public void clear_clearsAddedServers() {
        SharedPreferencesServerRepository repository = new SharedPreferencesServerRepository();

        repository.save("http://hello.com");
        repository.save("http://test.com");

        repository.clear();
        assertThat(repository.getServers(), containsInAnyOrder(defaultServer));
    }
}