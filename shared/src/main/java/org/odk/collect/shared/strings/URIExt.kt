package org.odk.collect.shared.strings

import java.net.URI

fun URI.getQueryParameter(param: String): String? {
    val query = this.query

    return if (query != null) {
        query
            .split("&")
            .mapNotNull {
                val split = it.split("=")
                if (split.size == 2) {
                    split[0] to split[1]
                } else {
                    null
                }
            }
            .toMap()[param]
    } else {
        null
    }
}
