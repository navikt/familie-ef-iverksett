package no.nav.familie.ef.iverksett.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.util.getJson
import no.nav.familie.ef.iverksett.util.queryForJson
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
            return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)!!
        } catch (emptyResultDataAccess: EmptyResultDataAccessException) {
            logger.error("Kunne ikke finne tilkjent ytelse for utbetaling fra basen med behandlingID = ${behandlingId} (nullverdi)")
            return null
        }
    }

    override fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat? {
        val sql = "SELECT journalpostresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)!!
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
                        rs.getJson("tilkjentYtelseForUtbetaling"),
                        rs.getJson("oppdragResultat"),
                        rs.getJson("journalpostResultat"),
                        rs.getJson("vedtaksBrevResultat"))
            }!!
        } catch (ex: EmptyResultDataAccessException) {
            logger.info("Kunne ikke hente IverksettResultat for behandlingId=${behandlingId}")
            return null
        }
    }


}