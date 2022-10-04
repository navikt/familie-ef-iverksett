package no.nav.familie.ef.iverksett.brev.domain

import java.time.LocalDateTime

/**
 * @param map inneholder [DistribuerBrevResultat] per journalpostId
 */
data class DistribuerBrevResultatMap(val map: Map<String, DistribuerBrevResultat> = emptyMap()) {

    operator fun plus(tillegg: Map<String, DistribuerBrevResultat>): DistribuerBrevResultatMap =
        DistribuerBrevResultatMap(map + tillegg)

    fun isNotEmpty() = map.isNotEmpty()
}

data class DistribuerBrevResultat(val bestillingId: String?, val dato: LocalDateTime = LocalDateTime.now())
