package no.nav.familie.ef.iverksett.domene

import org.springframework.data.relational.core.mapping.Embedded

data class AndelTilkjentYtelse(
        @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "periodebelop_")
        val periodebeløp: Periodebeløp,
        val personIdent: String,
        val periodeId: Long? = null,
        val forrigePeriodeId: Long? = null,
        val stønadsType: StønadsType
)

enum class StønadsType {
    OVERGANGSSTØNAD,
    BARNETILSYN,
    SKOLEPENGER
}