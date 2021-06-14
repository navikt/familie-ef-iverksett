package no.nav.familie.ef.iverksett.util

import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import java.time.ZonedDateTime
import java.util.UUID

fun opprettBehandlingStatistikkDto(behandlingId: UUID, hendelse: Hendelse): BehandlingStatistikkDto {
    return BehandlingStatistikkDto(behandlingId = UUID.randomUUID(),
                                   personIdent = "aktor",
                                   gjeldendeSaksbehandlerId = "saksbehandler",
                                   saksnummer = "saksnummer",
                                   hendelseTidspunkt = ZonedDateTime.now(),
                                   hendelse = Hendelse.MOTTATT,
                                   behandlingResultat = "",
                                   resultatBegrunnelse = "",
                                   ansvarligEnhet = "ansvarligEnhet",
                                   opprettetEnhet = "opprettetEnhet",
                                   strengtFortroligAdresse = false,
                                   sakYtelse = StønadType.OVERGANGSSTØNAD,
                                   behandlingstype = BehandlingType.BLANKETT)
}