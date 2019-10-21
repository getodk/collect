package org.odk.collect.android.widgets.interfaces;

public interface GeoWidget extends BinaryWidget {

    void startGeoActivity();

    void updateButtonLabelsAndVisibility(boolean dataAvailable);

    String getAnswerToDisplay(String answer);

    String getDefaultButtonLabel();
}
