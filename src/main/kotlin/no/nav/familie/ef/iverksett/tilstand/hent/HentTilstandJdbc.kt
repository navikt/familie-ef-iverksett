package no.nav.familie.ef.iverksett.tilstand.hent

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.domene.*
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class HentTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : HentTilstand {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        val sql = "select tilkjentytelseforutbetaling from iverksett_resultat where behandling_id = :behandlingId"
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
        val sql = "select journalpostResultat from iverksett_resultat where behandling_id = :behandlingId"
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

    override fun hentIverksettResultat(behandlingId: UUID): IverksettResultat {
        val sql = "select * from iverksett_resultat where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { rs: ResultSet, _: Int ->
            IverksettResultat(
                UUID.fromString(rs.getString("behandling_id")),
                objectMapper.readValue<TilkjentYtelse>(rs.getString("tilkjentYtelseForUtbetaling")),
                rs.getString("oppdragResultat")?.let { objectMapper.readValue<OppdragResultat>(it) },
                rs.getString("journalpostResultat")?.let { objectMapper.readValue<JournalpostResultat>(it) },
                rs.getString("vedtaksBrevResultat")?.let { objectMapper.readValue<DistribuerVedtaksbrevResultat>(it) })
        }!!
    }
}