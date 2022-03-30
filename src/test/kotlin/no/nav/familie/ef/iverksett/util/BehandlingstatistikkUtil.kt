package no.nav.familie.ef.iverksett.util

import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

fun opprettBehandlingsstatistikkDto(behandlingId: UUID, hendelse: Hendelse, fortrolig: Boolean): BehandlingsstatistikkDto {
    return BehandlingsstatistikkDto(behandlingId = behandlingId,
                                    eksternBehandlingId = 654L,
                                    personIdent = "aktor",
                                    gjeldendeSaksbehandlerId = "saksbehandler",
                                    beslutterId = "beslutterId",
                                    eksternFagsakId = 123L,
                                    hendelseTidspunkt = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")),
                                    hendelse = hendelse,
                                    behandlingResultat = "",
                                    resultatBegrunnelse = "",
                                    ansvarligEnhet = "ansvarligEnhet",
                                    opprettetEnhet = "opprettetEnhet",
                                    strengtFortroligAdresse = fortrolig,
                                    stønadstype = StønadType.OVERGANGSSTØNAD,
                                    behandlingstype = BehandlingType.FØRSTEGANGSBEHANDLING,
                                    relatertBehandlingId = UUID.randomUUID(),
                                    relatertEksternBehandlingId = null)
}
