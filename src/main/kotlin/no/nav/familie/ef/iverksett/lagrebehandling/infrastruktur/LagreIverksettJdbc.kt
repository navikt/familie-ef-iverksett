package no.nav.familie.ef.iverksett.lagrebehandling.infrastruktur

import arrow.core.Either
import no.nav.familie.ef.iverksett.lagrebehandling.tjeneste.KunneIkkeLagreBehandling
import no.nav.familie.ef.iverksett.lagrebehandling.tjeneste.Lagrebehandling
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class LagreIverksettJdbc(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : Lagrebehandling {

    override fun lagre(iverksettJson: String): Either<KunneIkkeLagreBehandling, Int> {
        try {
            return Either.Right(lagreIverksett(iverksettJson))
        } catch (exception: Exception) {
            return Either.Left(KunneIkkeLagreBehandling)
        }
    }

    private fun lagreIverksett(json: String): Int {
        var sql = ""

        var params = MapSqlParameterSource()

        // Execute

        return 0
    }

}