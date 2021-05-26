package no.nav.familie.ef.iverksett.iverksett.domene

import java.util.UUID

data class Brev(
        val behandlingId: UUID,
        val pdf: ByteArray
)