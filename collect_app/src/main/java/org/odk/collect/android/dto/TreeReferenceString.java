package org.odk.collect.android.dto;

public final class TreeReferenceString {

    private final String treeReferenceString;

    private TreeReferenceString(Builder builder) {
        treeReferenceString = builder.treeReferenceString;
    }

    public static class Builder {
        private String treeReferenceString;

        public Builder treeReferenceString(String treeReferenceString) {
            this.treeReferenceString = treeReferenceString;
            return this;
        }

        public TreeReferenceString build() {
            return new TreeReferenceString(this);
        }
    }

}
