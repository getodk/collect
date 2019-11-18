package org.odk.collect.android.dto;

/**
 * This class represents a single row from the itemset table which is located in
 * {@link org.odk.collect.android.database.ItemsetDbAdapter#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_transfer_object
 * Objects of this class are created using builder pattern: https://en.wikipedia.org/wiki/Builder_pattern
 */
public class Itemset {
    private final int id;
    private final String hash;
    private final String path;

    private Itemset(Builder builder) {
        id = builder.id;
        hash = builder.hash;
        path = builder.path;
    }

    public static class Builder {
        private int id;
        private String hash;
        private String path;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Itemset build() {
            return new Itemset(this);
        }
    }

    public int getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String getPath() {
        return path;
    }
}
