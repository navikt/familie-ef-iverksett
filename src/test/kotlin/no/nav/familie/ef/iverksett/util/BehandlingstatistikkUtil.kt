package no.nav.familie.ef.iverksett.util

import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import java.time.ZonedDateTime
import java.util.UUID

fun opprettBehandlingStatistikkDto(hendelse: Hendelse): BehandlingStatistikkDto {
    return BehandlingStatistikkDto(behandlingId = UUID.randomUUID(),
                                   "12345678910",
                                   "123",
                                   "456",
                                   ZonedDateTime.now(),
                                   hendelse,
                                   "behandlingsResultat",
                                   "resultatBegrunnelse")
}