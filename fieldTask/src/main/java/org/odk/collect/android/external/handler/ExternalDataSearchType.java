/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.external.handler;

/**
 * Author: Meletis Margaritis
 * Date: 20/05/13
 * Time: 12:22
 */
enum ExternalDataSearchType {

    CONTAINS("contains") {
        @Override
        protected String getSingleLikeArgument(String queriedValue) {
            return '%' + queriedValue + '%';
        }
    },

    MATCHES("matches") {
        @Override
        protected String getSingleLikeArgument(String queriedValue) {
            return queriedValue;
        }
    },

    STARTS("startsWith") {
        @Override
        protected String getSingleLikeArgument(String queriedValue) {
            return queriedValue + '%';
        }
    },

    ENDS("endsWith") {
        @Override
        protected String getSingleLikeArgument(String queriedValue) {
            return '%' + queriedValue;
        }
    };

    private final String keyword;

    ExternalDataSearchType(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public static ExternalDataSearchType getByKeyword(String keyword, ExternalDataSearchType fallback) {
        if (keyword == null) {
            return fallback;
        }
        for (ExternalDataSearchType externalDataSearchType : ExternalDataSearchType.values()) {
            if (externalDataSearchType.getKeyword().trim().toLowerCase().equals(keyword.trim().toLowerCase())) {
                return externalDataSearchType;
            }
        }

        return fallback;
    }

    public String[] constructLikeArguments(String queriedValue, int times) {
        String[] args = new String[times];
        for (int i = 0; i < times; i++) {
            args[i] = getSingleLikeArgument(queriedValue);
        }
        return args;
    }

    protected abstract String getSingleLikeArgument(String queriedValue);
}
