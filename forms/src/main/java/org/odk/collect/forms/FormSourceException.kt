package org.odk.collect.forms

sealed class FormSourceException : Exception() {
    class Unreachable(val serverUrl: String) : FormSourceException()
    class AuthRequired : FormSourceException()
    class FetchError : FormSourceException()
    class SecurityError(val serverUrl: String) : FormSourceException()
    class ServerError(val statusCode: Int, val serverUrl: String) : FormSourceException()
    class ParseError(val serverUrl: String) : FormSourceException()

    // Aggregate 0.9 and prior used a custom API before the OpenRosa standard was in place. Aggregate continued
    // to provide this response to HTTP requests so some custom servers tried to implement it.
    class ServerNotOpenRosaError : FormSourceException()
}
