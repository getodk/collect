package org.odk.collect.db.sqlite

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.shared.Query

class SqlQueryTest {
    @Test
    fun `Eq query generates correct selection and arguments`() {
        val query = Query.StringEq("name", "John").toSql()

        assertThat(query.selection, equalTo("name = ?"))
        assertThat(query.selectionArgs, equalTo(arrayOf("John")))
    }

    @Test
    fun `NotEq query generates correct selection and arguments`() {
        val query = Query.StringNotEq("age", "30").toSql()

        assertThat(query.selection, equalTo("age != ?"))
        assertThat(query.selectionArgs, equalTo(arrayOf("30")))
    }

    @Test
    fun `And query generates correct selection and arguments`() {
        val queryA = Query.StringEq("name", "John")
        val queryB = Query.StringNotEq("age", "30")
        val combinedQuery = Query.And(queryA, queryB).toSql()

        assertThat(combinedQuery.selection, equalTo("(name = ? AND age != ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("John", "30")))
    }

    @Test
    fun `Or query generates correct selection and arguments`() {
        val queryA = Query.StringEq("city", "New York")
        val queryB = Query.StringNotEq("country", "Canada")
        val combinedQuery = Query.Or(queryA, queryB).toSql()

        assertThat(combinedQuery.selection, equalTo("(city = ? OR country != ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("New York", "Canada")))
    }

    @Test
    fun `nested And and Or queries generates correct selection and arguments`() {
        val queryA = Query.StringEq("status", "active")
        val queryB = Query.StringNotEq("role", "admin")
        val queryC = Query.StringEq("team", "engineering")

        val combinedQuery = Query.And(Query.Or(queryA, queryB), queryC).toSql()

        assertThat(combinedQuery.selection, equalTo("((status = ? OR role != ?) AND team = ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("active", "admin", "engineering")))
    }
}
