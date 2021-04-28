package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import arrow.core.Either
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.KunneIkkeLagreIverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksett
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksett {

    override fun lagre(iverksettJson: String): Either<KunneIkkeLagreIverksett, Int> {
        try {
            return Either.Right(lagreIverksett(iverksettJson))
        } catch (exception: Exception) {
            return Either.Left(KunneIkkeLagreIverksett)
        }
    }

    private fun lagreIverksett(json: String): Int {
        val sql = "insert into iverksett values(:uuid, to_json(:json::json), '1.0')"
        var mapSqlParameterSource = MapSqlParameterSource(mapOf(
            "uuid" to UUID.randomUUID(),
            "json" to json
        ))
        return namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

}