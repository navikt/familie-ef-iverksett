package no.nav.familie.ef.iverksett.tilstand.lagre

import no.nav.familie.ef.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class LagreTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreTilstand {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun lagreTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {

        val sql = "insert into iverksett_resultat values(:behandlingId, :tilkjentYtelseForUtbetaling::json, null, null)"
        val tilkjentYtelseForUtbetalingJson = objectMapper.writeValueAsString(tilkjentYtelseForUtbetaling)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("tilkjentYtelseForUtbetaling", tilkjentYtelseForUtbetalingJson)

        try {
            namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        } catch (ex: Exception) {
            secureLogger.error(
                "Kunne ikke lagre tilkjentYtelseForUtbetaling til basen, behandlingID = ${behandlingId}, " +
                        "tilkjentYtelseForUtbetaling = ${tilkjentYtelseForUtbetaling}"
            )
            throw Exception("Feil ved lagring av tilkjent ytelse")
        }
    }

    override fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val sql =
            "update iverksett_resultat set tilkjentYtelseForUtbetaling = :oppdragResultatJson::json where behandling_id = :behandlingId"

        val oppdragResultatJson = objectMapper.writeValueAsString(oppdragResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("oppdragResultatJson", oppdragResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId}, oppdragResultatJson : ${oppdragResultatJson}")
    }


    override fun oppdaterJournalpostResultat(behandlingId: UUID, journalPostResultat: JournalpostResultat) {

        val sql =
            "update iverksett_resultat set journalpostResultat = :journalpostResultat::json where behandling_id = :behandlingId"
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
            "update iverksett_resultat set vedtaksBrevResultat = :distribuerVedtaksbrevResultatJson::json where behandling_id = :behandlingId"
        val distribuerVedtaksbrevResultatJson = objectMapper.writeValueAsString(distribuerVedtaksbrevResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("distribuerVedtaksbrevResultatJson", distribuerVedtaksbrevResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = ${behandlingId}, " +
                             "distribuerVedtaksbrevResultatJson : ${distribuerVedtaksbrevResultatJson}")
    }

}