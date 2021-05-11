package no.nav.familie.ef.iverksett.hentIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.hentIverksett.tjeneste.HentIverksett
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

class HentIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate, val objectMapper: ObjectMapper) :
    HentIverksett {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun hent(behandlingsId: String): Iverksett {
        try {
            return hentIverksettStringOgTransformer(behandlingsId)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke hente iverksett for behandlingsId ${behandlingsId}: ${ex}")
            throw Exception("Feil ved HentIverksett til basen")
        }
    }

    override fun hentBrev(behandlingsId: String): Brev {
        val sql = "select * from brev where behandling_id = :behandlingsId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingsId", behandlingsId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, rowIndex: Int ->
            val behandlingId = resultSet.getString("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(behandlingId, pdf)
        } ?: error("Fant ikke brev for behandlingsid : ${behandlingsId}")
    }

    private fun hentIverksettStringOgTransformer(behandlingsId: String): Iverksett {

        val sql = "select data from iverksett where behandling_id = :behandlingsId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingsId", UUID.fromString(behandlingsId))
        val json = namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)
            ?: error("Finner ikke iverksett med behandlingId=${behandlingsId}")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        return iverksett
    }

}