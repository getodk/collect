package org.odk.collect.android.storage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.utilities.Clock;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StorageStateProviderTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Clock clock;

    private StorageStateProvider storageStateProvider;

    @Before
    public void setup() {
        when(clock.getCurrentTime()).thenReturn(90000000L);
        storageStateProvider = spy(new StorageStateProvider(clock));
        storageStateProvider.clearLastMigrationAttemptTime();
    }

    @Test
    public void whenScopedStorageIsUsed_shouldNotPerformAutomaticMigration() {
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(true);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(false));
    }

    @Test
    public void whenScopedStorageIsNotUsed_shouldPerformAutomaticMigrationAndRepeatItOnceADay() {
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(false);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(true));
        when(clock.getCurrentTime()).thenReturn(90000000L + 86400000 - 1);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(false));
        when(clock.getCurrentTime()).thenReturn(90000000L + 86400000);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(true));
    }
}
