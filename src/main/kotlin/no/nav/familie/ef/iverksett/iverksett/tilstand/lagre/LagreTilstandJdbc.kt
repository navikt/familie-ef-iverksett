package no.nav.familie.ef.iverksett.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LagreTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreTilstand {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun opprettTomtResultat(behandlingId: UUID) {
        val sql = "INSERT INTO iverksett_resultat VALUES(:behandlingId, NULL, NULL, NULL, NULL)"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke opprette tomt innslag for iverksett_resultat til basen med behandlingID = ${behandlingId}")
            throw Exception("Feil ved lagring av tilkjent ytelse")
        }
    }

    override fun oppdaterTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse?) {

        val sql =
                "UPDATE iverksett_resultat SET tilkjentytelseforutbetaling = :tilkjentYtelseForUtbetaling::JSON WHERE behandling_id = :behandlingId"
        val tilkjentYtelseForUtbetalingJson = objectMapper.writeValueAsString(tilkjentYtelseForUtbetaling)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
                .addValue("tilkjentYtelseForUtbetaling", tilkjentYtelseForUtbetalingJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
        ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId}")
    }

    override fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val sql =
                "UPDATE iverksett_resultat SET oppdragresultat = :oppdragResultat::JSON WHERE behandling_id = :behandlingId"

        val oppdragResultatJson = objectMapper.writeValueAsString(oppdragResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
                .addValue("oppdragResultat", oppdragResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
        ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId}, oppdragResultatJson : ${oppdragResultatJson}")
    }


    override fun oppdaterJournalpostResultat(behandlingId: UUID, journalPostResultat: JournalpostResultat) {

        val sql =
                "UPDATE iverksett_resultat SET journalpostresultat = :journalpostResultat::JSON WHERE behandling_id = :behandlingId"
        val journalPostResultatJson = objectMapper.writeValueAsString(journalPostResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
                .addValue("journalpostResultat", journalPostResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
        ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId},journalPostResultatJson : ${journalPostResultatJson}")
    }

    override fun oppdaterDistribuerVedtaksbrevResultat(
            behandlingId: UUID,
            distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat
    ) {
        val sql =
                "UPDATE iverksett_resultat SET vedtaksbrevresultat = :distribuerVedtaksbrevResultatJson::JSON WHERE behandling_id = :behandlingId"
        val distribuerVedtaksbrevResultatJson = objectMapper.writeValueAsString(distribuerVedtaksbrevResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
                .addValue("distribuerVedtaksbrevResultatJson", distribuerVedtaksbrevResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
        ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId}, " +
                 "distribuerVedtaksbrevResultatJson : ${distribuerVedtaksbrevResultatJson}")
    }

}