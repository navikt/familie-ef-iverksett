package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import arrow.core.Either
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.KunneIkkeLagreIverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksett
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksett {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun lagre(iverksettJson: String): Either<KunneIkkeLagreIverksett, Int> {
        try {
            return Either.Right(lagreIverksett(iverksettJson))
        } catch (exception: Exception) {
            logger.error("Kunne ikke lagre iverksett json : ${exception}")
            return Either.Left(KunneIkkeLagreIverksett)
        }
    }

    private fun lagreIverksett(json: String): Int {
        val sql = "insert into iverksett values(:uuid, to_json(:json::json), '1.0')"
        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "uuid" to UUID.randomUUID(),
                "json" to json
            )
        )
        return namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

    /**
     * TODO : Legge på en til alle fra iverksett til brev (og en transactional)
     */

}