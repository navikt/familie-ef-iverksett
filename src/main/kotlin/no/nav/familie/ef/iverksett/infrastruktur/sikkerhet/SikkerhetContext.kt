package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object SikkerhetContext {
    fun kallKommerFraEfSak(): Boolean = kallKommerFra("teamfamilie:familie-ef-sak")

    fun kallKommerFraFraProsessering(): Boolean = kallKommerFra("teamfamilie:familie-prosessering")

    private fun kallKommerFra(forventetApplikasjonsSuffix: String): Boolean {
        val claims = SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")

        val applikasjonsnavn = claims.get("azp_name")?.toString() ?: ""

        return applikasjonsnavn.endsWith(forventetApplikasjonsSuffix)
    }
}
