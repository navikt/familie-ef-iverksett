package no.nav.familie.ef.iverksett.util

import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.sql.ResultSet
import java.util.UUID

fun ResultSet.getUUID(columnLabel: String): UUID = UUID.fromString(this.getString(columnLabel))

inline fun <reified T> ResultSet.getJson(columnLabel: String): T? {
    return this.getBytes(columnLabel)?.let { ObjectMapperProvider.objectMapper.readValue<T>(it) }
}

inline fun <reified T> NamedParameterJdbcTemplate.queryForJson(
    sql: String,
    paramSource: SqlParameterSource,
): T? {
    try {
        val json = this.queryForObject(sql, paramSource, ByteArray::class.java) ?: return null
        return ObjectMapperProvider.objectMapper.readValue<T>(json)
    } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
        return null
    }
}

inline fun <reified T> NamedParameterJdbcTemplate.queryForNullableObject(
    sql: String,
    paramSource: SqlParameterSource,
    rowMapper: RowMapper<T>,
): T? {
    return try {
        this.queryForObject(sql, paramSource, rowMapper)
    } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
        null
    }
}
