package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.data.annotation.Id
import java.util.UUID

data class Behandlingsstatistikk(
    @Id
    val id: UUID = UUID.randomUUID(),
    val behandlingId: UUID,
    val behandlingDvh: BehandlingDVH,
    val hendelse: Hendelse
)
