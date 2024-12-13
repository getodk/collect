package org.odk.collect.db.sqlite

data class Query(
    val selection: String,
    val selectionArgs: Array<String>
)
