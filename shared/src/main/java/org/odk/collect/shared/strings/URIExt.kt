package org.odk.collect.shared.strings

import java.net.URI

fun URI.getQueryParameter(param: String): String? {
    val query = this.query

    return if (query != null) {
        query
            .split("&")
            .mapNotNull {
                val matchResult = "([^=]+)=(.*)$".toRegex().find(it)
                if (matchResult != null) {
                    val groups = matchResult.groupValues.drop(1)
                    when (groups.size) {
                        2 -> groups[0] to groups[1]
                        else -> null
                    }
                } else {
                    it to ""
                }
            }
            .firstOrNull { it.first == param }?.second
    } else {
        null
    }
}
