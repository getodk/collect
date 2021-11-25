package org.odk.collect.errors

import java.io.Serializable

data class ErrorItem(
    val title: String,
    val secondaryText: String,
    val supportingText: String
) : Serializable
