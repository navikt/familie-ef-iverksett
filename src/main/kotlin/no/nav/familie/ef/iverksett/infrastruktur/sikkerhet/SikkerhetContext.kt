package no.nav.familie.ef.iverksett.infrastruktur.sikkerhet

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object SikkerhetContext {
    fun kallKommerFraEfSak(): Boolean = kallKommerFra("teamfamilie:familie-ef-sak")

    fun kallKommerFraFraProsessering(): Boolean = kallKommerFra("teamfamilie:familie-prosessering")

    private fun kallKommerFra(forventetApplikasjonsSuffix: String): Boolean {
        return try {
            val validationContext = SpringTokenValidationContextHolder().getTokenValidationContext()
            val claims = validationContext?.getClaims("azuread") ?: return false
            val applikasjonsnavn = claims.get("azp_name")?.toString() ?: ""

            applikasjonsnavn.endsWith(forventetApplikasjonsSuffix)
        } catch (e: Exception) {
            false
        }
    }
}
