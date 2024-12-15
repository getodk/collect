package org.odk.collect.db.sqlite

sealed class Query(
    val selection: String,
    val selectionArgs: Array<String>
) {
    class Eq(val column: String, val value: String) : Query(
        "$column = ?",
        arrayOf(value)
    )

    class NotEq(val column: String, val value: String) : Query(
        "$column != ?",
        arrayOf(value)
    )

    class And(val queryA: Query, val queryB: Query) : Query(
        "(${queryA.selection} AND ${queryB.selection})",
        queryA.selectionArgs + queryB.selectionArgs
    )

    class Or(val queryA: Query, val queryB: Query) : Query(
        "(${queryA.selection} OR ${queryB.selection})",
        queryA.selectionArgs + queryB.selectionArgs
    )
}
