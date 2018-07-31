package org.odk.collect.android.preferences;

public enum Transport {
    Sms("sms"),
    Internet("internet"),
    Both("both");

    private String value;

    Transport(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Transport fromPreference(Object text) {
        for (Transport transport : values()) {
            if (transport.value.equalsIgnoreCase((String) text)) {
                return transport;
            }
        }

        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
