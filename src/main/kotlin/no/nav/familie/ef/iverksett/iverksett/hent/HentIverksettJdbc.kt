package no.nav.familie.ef.iverksett.iverksett.hent

import no.nav.familie.ef.iverksett.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.util.getUUID
import no.nav.familie.ef.iverksett.util.queryForJson
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

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
        val sql = "SELECT * FROM brev WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, rowIndex: Int ->
            val behandlingId = resultSet.getUUID("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(behandlingId, pdf)
        } ?: error("Fant ikke brev for behandlingId : ${behandlingId}")
    }

    private fun hentIverksettStringOgTransformer(behandlingId: UUID): Iverksett {

        val sql = "select data from iverksett where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
               ?: error("Finner ikke iverksett med behandlingId=${behandlingId}")
    }

}