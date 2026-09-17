package org.odk.collect.shared.strings

import java.net.URI
import java.net.URLDecoder

fun URI.getQueryParameter(param: String): String? {
    val query = this.rawQuery

    return if (query != null) {
        val paramRegex = "([^=]+)=(.*)$".toRegex()
        query
            .split("&")
            .map {
                val matchResult = paramRegex.find(it)
                if (matchResult != null) {
                    val groups = matchResult.groupValues.drop(1)
                    groups[0] to URLDecoder.decode(groups[1], "UTF-8")
                } else {
                    it to ""
                }
            }
            .firstOrNull { it.first == param }?.second
    } else {
        null
    }
}
