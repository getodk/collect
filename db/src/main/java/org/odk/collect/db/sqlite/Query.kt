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

    fun copyWithMappedColumns(columnMapper: (String) -> String): Query {
        return when (this) {
            is Eq -> Eq(columnMapper(column), value)
            is NotEq -> NotEq(columnMapper(column), value)
            is And -> And(
                queryA.copyWithMappedColumns(columnMapper),
                queryB.copyWithMappedColumns(columnMapper)
            )
            is Or -> Or(
                queryA.copyWithMappedColumns(columnMapper),
                queryB.copyWithMappedColumns(columnMapper)
            )
        }
    }
}
