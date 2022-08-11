package no.nav.familie.ef.iverksett.iverksetting.domene

import java.util.UUID

data class Brev(
    val behandlingId: UUID,
    val pdf: ByteArray
)
