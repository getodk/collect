package org.odk.collect.android.version;

import androidx.annotation.Nullable;

public class VersionInformation {

    private final VersionDescriptionProvider versionDescriptionProvider;

    @Nullable
    private String[] splitDescription;

    public VersionInformation(VersionDescriptionProvider versionDescriptionProvider) {
        this.versionDescriptionProvider = versionDescriptionProvider;
    }

    public String getSemanticVersion() {
        return getVersionDescriptionComponents()[0];
    }

    @Nullable
    public Integer getBetaNumber() {
        if (isBeta()) {
            String description = versionDescriptionProvider.getVersionDescription();
            return Integer.parseInt(description.split("beta")[1].substring(1, 2));
        } else {
            return null;
        }
    }

    @Nullable
    public String getCommitSHA() {
         String[] components = getVersionDescriptionComponents();

        if (isBeta() && components.length > 3) {
            return components[3];
        } else if (!isBeta() && components.length > 2) {
            return components[2];
        } else {
            return null;
        }
    }

    @Nullable
    public Integer getCommitCount() {
        String[] components = getVersionDescriptionComponents();

        if (isBeta() && components.length > 3) {
            return Integer.parseInt(components[2]);
        } else if (!isBeta() && components.length > 2) {
            return Integer.parseInt(components[1]);
        } else {
            return null;
        }
    }

    public String getVersionToDisplay() {
        if (getBetaNumber() != null) {
            return getSemanticVersion() + " Beta " + getBetaNumber();
        } else {
            return getSemanticVersion();
        }
    }

    public boolean isRelease() {
        return getVersionDescriptionComponents().length == 1;
    }

    public boolean isBeta() {
        return versionDescriptionProvider.getVersionDescription().contains("beta");
    }

    public boolean isDirty() {
        return versionDescriptionProvider.getVersionDescription().contains("dirty");
    }

    private String[] getVersionDescriptionComponents() {
        if (splitDescription == null) {
            splitDescription = versionDescriptionProvider.getVersionDescription().split("-");
        }

        return splitDescription;
    }
}
