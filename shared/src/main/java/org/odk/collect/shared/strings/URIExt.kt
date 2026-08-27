package org.odk.collect.shared.strings

import java.net.URI

fun URI.getQueryParameter(param: String): String? {
    return this.query
        .split("&")
        .associate {
            val (key, value) = it.split("=")
            key to value
        }[param]
}
