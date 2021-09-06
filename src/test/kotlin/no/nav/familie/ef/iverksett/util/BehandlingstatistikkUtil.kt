package no.nav.familie.ef.iverksett.util

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

fun opprettBehandlingsstatistikkDto(behandlingId: UUID, hendelse: Hendelse, fortrolig: Boolean): BehandlingsstatistikkDto {
    return BehandlingsstatistikkDto(behandlingId = behandlingId,
                                    personIdent = "aktor",
                                    gjeldendeSaksbehandlerId = "saksbehandler",
                                    eksternFagsakId = "eksternFagsakId",
                                    hendelseTidspunkt = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")),
                                    hendelse = hendelse,
                                    behandlingResultat = "",
                                    resultatBegrunnelse = "",
                                    ansvarligEnhet = "ansvarligEnhet",
                                    opprettetEnhet = "opprettetEnhet",
                                    strengtFortroligAdresse = fortrolig,
                                    stønadstype = StønadType.OVERGANGSSTØNAD,
                                    behandlingstype = BehandlingType.FØRSTEGANGSBEHANDLING,
                                    relatertBehandlingId = UUID.randomUUID())
}

fun opprettBehandlingDVH(uuid: UUID, hendelse: Hendelse): BehandlingDVH {
    return BehandlingDVH(behandlingId = uuid.toString(),
                         sakId = "saksnummer",
                         personIdent = "12345678910",
                         registrertTid = ZonedDateTime.now(),
                         endretTid = ZonedDateTime.now(),
                         tekniskTid = ZonedDateTime.now(),
                         behandlingStatus = hendelse.name,
                         opprettetAv = "opprettetAv",
                         saksnummer = "123",
                         mottattTid = ZonedDateTime.now(),
                         saksbehandler = "saksbehandler",
                         opprettetEnhet = "opprettetenhet",
                         ansvarligEnhet = "ansvarligenhet",
                         behandlingMetode = "MANUELL",
                         avsender = "NAV enslig forelder",
                         behandlingType = "behandlingtype",
                         sakYtelse = "sakYtelse",
                         totrinnsbehandling = true
    )
}