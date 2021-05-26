package no.nav.familie.ef.iverksett.iverksett.hent

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class HentIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun hent(behandlingId: UUID): Iverksett {
        try {
            return hentIverksettStringOgTransformer(behandlingId)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke hente iverksett for behandlingId $behandlingId", ex)
            throw Exception("Feil ved HentIverksett til basen")
        }
    }

    fun hentBrev(behandlingId: UUID): Brev {
        val sql = "select * from brev where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, rowIndex: Int ->
            val behandlingId = resultSet.getString("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(UUID.fromString(behandlingId), pdf)
        } ?: error("Fant ikke brev for behandlingId : ${behandlingId}")
    }

    private fun hentIverksettStringOgTransformer(behandlingId: UUID): Iverksett {

        val sql = "select data from iverksett where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        val json = namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)
            ?: error("Finner ikke iverksett med behandlingId=${behandlingId}")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        return iverksett
    }

}