package no.nav.familie.ef.iverksett.hentIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class HentIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate, val objectMapper: ObjectMapper) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hent(behandlingId: String): Iverksett {
        try {
            return hentIverksettStringOgTransformer(behandlingId)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke hente iverksett for behandlingId ${behandlingId}: ${ex}")
            throw Exception("Feil ved HentIverksett til basen")
        }
    }

    fun hentBrev(behandlingId: String): Brev {
        val sql = "select * from brev where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, rowIndex: Int ->
            val behandlingId = resultSet.getString("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(behandlingId, pdf)
        } ?: error("Fant ikke brev for behandlingId : ${behandlingId}")
    }

    private fun hentIverksettStringOgTransformer(behandlingId: String): Iverksett {

        val sql = "select data from iverksett where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", UUID.fromString(behandlingId))
        val json = namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)
            ?: error("Finner ikke iverksett med behandlingId=${behandlingId}")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        return iverksett
    }

}