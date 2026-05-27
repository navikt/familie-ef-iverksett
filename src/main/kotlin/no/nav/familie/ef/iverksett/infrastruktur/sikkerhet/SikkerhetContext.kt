package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SikkerhetContext {
    fun validerKallKommerFraEfSak(): Boolean = validerKallKommerFra("teamfamilie:familie-ef-sak")

    fun validerKallKommerFraFraProsessering(): Boolean = validerKallKommerFra("teamfamilie:familie-prosessering")

    private fun validerKallKommerFra(forventetApplikasjonsSuffix: String): Boolean {
        val claims = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        val applikasjonsnavn = claims.token.claims["azp_name"]?.toString() ?: ""

        return if (applikasjonsnavn.endsWith(forventetApplikasjonsSuffix)) {
            true
        } else {
            throw ApiFeil("Kall kommer ikke fra: $forventetApplikasjonsSuffix", HttpStatus.FORBIDDEN)
        }
    }
}
