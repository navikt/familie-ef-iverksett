package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.http.HttpStatus

object SikkerhetContext {
    fun validerKallKommerFraEfSak(): Boolean = validerKallKommerFra("teamfamilie:familie-ef-sak")

    fun validerKallKommerFraFraProsessering(): Boolean = validerKallKommerFra("teamfamilie:familie-prosessering")

    private fun validerKallKommerFra(forventetApplikasjonsSuffix: String): Boolean {
        val claims = SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")
        val applikasjonsnavn = claims.get("azp_name")?.toString() ?: ""

        return if (applikasjonsnavn.endsWith(forventetApplikasjonsSuffix)) {
            true
        } else {
            throw ApiFeil("Kall kommer ikke fra: $forventetApplikasjonsSuffix", HttpStatus.FORBIDDEN)
        }
    }
}
