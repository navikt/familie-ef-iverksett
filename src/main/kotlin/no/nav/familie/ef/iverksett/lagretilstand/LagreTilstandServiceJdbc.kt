package no.nav.familie.ef.iverksett.lagretilstand

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class LagreTilstandServiceJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreTilstand {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun lagreOppdragResultat(behandlingId: String, oppdragResultatJson: String) {

        val sql = "insert into iverksett_resultat values(:behandlingId, null, :oppdragResultatJson::json, null)"
        val mapSqlParameterSource =
            MapSqlParameterSource(
                mapOf(
                    "behandlingId" to behandligId,
                    "oppdragResultatJson" to oppdragResultatJson
                )
            )
        try {
            namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke lagre oppdragResultatJson til basen, behandlingID : ${behandlingId}, oppdragResultatJson : ${oppdragResultatJson}")
        }
    }

    override fun lagreJournalPostResultat(behandlingId: String, journalPostResultatJson: String) {

        val sql =
            "update iverksett_resultat set journalpostResultat = :journalpostResultat::json where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "journalpostResultat" to journalPostResultatJson
            )
        )
        try {
            namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke lagre journalPostResultatJson til basen, behandlingID : ${behandlingId}, journalPostResultatJson : ${journalPostResultatJson}")
        }
    }

    override fun lagreTilkjentYtelseForUtbetaling(behandlingId: String, tilkjentYtelseForUtbetaling: String) {
        val sql =
            "update iverksett_resultat set tilkjentYtelseForUtbetaling = :tilkjentYtelseForUtbetaling::json where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "tilkjentYtelseForUtbetaling" to tilkjentYtelseForUtbetaling
            )
        )
        try {
            namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke lagre tilkjentYtelseForUtbetaling til basen, behandlingID : ${behandlingId}, tilkjentYtelseForUtbetaling : ${tilkjentYtelseForUtbetaling}")
        }
    }

}