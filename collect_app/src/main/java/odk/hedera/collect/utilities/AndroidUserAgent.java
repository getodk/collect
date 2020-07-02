package odk.hedera.collect.utilities;

import org.odk.hedera.collect.BuildConfig;

public final class AndroidUserAgent implements UserAgentProvider {

    @Override
    public String getUserAgent() {
        return String.format("%s/%s %s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                System.getProperty("http.agent"));
    }

}
