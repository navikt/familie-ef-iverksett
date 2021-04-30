package no.nav.familie.ef.iverksett.domene

data class AndelTilkjentYtelse(
    val periodebeløp: Periodebeløp,
    val personIdent: String,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
    val stønadsType: StønadsType
)
