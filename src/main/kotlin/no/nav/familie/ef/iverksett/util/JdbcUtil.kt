package no.nav.familie.ef.iverksett.util

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.sql.ResultSet
import java.util.UUID

fun ResultSet.getUUID(columnLabel: String): UUID = UUID.fromString(this.getString(columnLabel))

inline fun <reified T> ResultSet.getJson(columnLabel: String): T? {
    return objectMapper.readValue<T>(this.getBytes(columnLabel))
}

inline fun <reified T> NamedParameterJdbcTemplate.queryForJson(sql: String, paramSource: SqlParameterSource): T? {
    try {
        val json = this.queryForObject(sql, paramSource, ByteArray::class.java) ?: return null
        return objectMapper.readValue<T>(json)
    } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
        return null
    }
}

inline fun <reified T> NamedParameterJdbcTemplate.queryForNullableObject(sql: String,
                                                                         paramSource: SqlParameterSource,
                                                                         rowMapper: RowMapper<T>): T? {
    try {
        return this.queryForObject(sql, paramSource, rowMapper)
    } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
        return null
    }
}
