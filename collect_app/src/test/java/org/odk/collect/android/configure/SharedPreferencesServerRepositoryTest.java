package org.odk.collect.android.configure;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesServerRepositoryTest {

    private String defaultServer;
    private SharedPreferencesServerRepository repository;

    @Before
    public void setup() {
        defaultServer = "http://default.example";
        repository = new SharedPreferencesServerRepository(defaultServer, initPrefs());
    }

    @Test
    public void getServers_whenEmpty_returnsDefault() {
        assertThat(repository.getServers(), contains(defaultServer));
    }

    @Test
    public void getServers_onlyReturnsTheLast5ServersSaved() {
        repository.save("http://url1.com");
        repository.save("http://url2.com");
        repository.save("http://url3.com");
        repository.save("http://url4.com");
        repository.save("http://url5.com");
        repository.save("http://url6.com");

        assertThat(repository.getServers(), contains(
                "http://url6.com",
                "http://url5.com",
                "http://url4.com",
                "http://url3.com",
                "http://url2.com"
        ));
    }

    @Test
    public void save_whenURLHasAlreadyBeenAdded_movesURLToTop() {
        repository.save("http://url1.com");
        repository.save("http://url2.com");
        repository.save("http://url1.com");

        assertThat(repository.getServers(), contains(
                "http://url1.com",
                "http://url2.com",
                defaultServer
        ));
    }

    @Test
    public void clear_clearsAddedServers() {
        repository.save("http://hello.com");
        repository.save("http://test.com");

        repository.clear();
        assertThat(repository.getServers(), contains(defaultServer));
    }
}