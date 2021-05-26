package no.nav.familie.ef.iverksett.iverksett.tilstand.hent

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class HentTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : HentTilstand {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        val sql = "SELECT tilkjentytelseforutbetaling FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            val tilkjentYtelseJson =
                    namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)!!
            return objectMapper.readValue<TilkjentYtelse>(tilkjentYtelseJson)
        } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
            logger.error("Kunne ikke finne tilkjent ytelse for utbetaling fra basen med behandlingID = ${behandlingId} (nullverdi)")
            return null
        }
    }

    override fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat? {
        val sql = "SELECT journalpostresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            val journalpostResultat =
                    namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)!!
            return objectMapper.readValue<JournalpostResultat>(journalpostResultat)
        } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
            logger.error("Kunne ikke finne journalpost for behandlingID = ${behandlingId}")
            return null
        }
    }

    override fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        val sql = "SELECT * FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { rs: ResultSet, _: Int ->
                IverksettResultat(
                        UUID.fromString(rs.getString("behandling_id")),
                        rs.getString("tilkjentYtelseForUtbetaling")?.let { objectMapper.readValue<TilkjentYtelse>(it) },
                        rs.getString("oppdragResultat")?.let { objectMapper.readValue<OppdragResultat>(it) },
                        rs.getString("journalpostResultat")?.let { objectMapper.readValue<JournalpostResultat>(it) },
                        rs.getString("vedtaksBrevResultat")?.let { objectMapper.readValue<DistribuerVedtaksbrevResultat>(it) })
            }!!
        } catch (ex: EmptyResultDataAccessException) {
            logger.info("Kunne ikke hente IverksettResultat for behandlingId=${behandlingId}")
            return null
        }
    }


}