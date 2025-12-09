package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

object SikkerthetContext {
    private val logger = LoggerFactory.getLogger(SikkerthetContext::class.java)
    private val teamLogsMarker = MarkerFactory.getMarker("TEAM_LOGS")

    fun kallKommerFraEfSak(): Boolean = kallKommerFra("teamfamilie:familie-ef-sak")

    fun kallKommerFraFraProsessering(): Boolean = kallKommerFra("teamfamilie:familie-prosessering")

    private fun kallKommerFra(forventetApplikasjonsSuffix: String): Boolean {
        val claims = SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")

        val applikasjonsnavn = claims.get("azp_name")?.toString() ?: "" // e.g. dev-gcp:some-team:application-name

        logger.info(teamLogsMarker, "Applikasjonsnavn: $applikasjonsnavn")
        return applikasjonsnavn.endsWith(forventetApplikasjonsSuffix)
    }
}
