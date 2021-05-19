package no.nav.familie.ef.iverksett.tilstand.hent

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class HentTilstandJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : HentTilstand {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse {
        val sql = "select tilkjentYtelseForUtbetaling from iverksett_resultat where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        try {
            val tilkjentYtelseJson: String =
                namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource, String::class.java)!!
            return objectMapper.readValue<TilkjentYtelse>(tilkjentYtelseJson)
        } catch (ex: Exception) {
            secureLogger.error("Kunne ikke hente tilkjentYtelseForUtbetaling fra basen, behandlingID = ${behandlingId}")
            throw Exception("Kunne ikke hente tilkjent ytelse")
        }
    }
}