package no.nav.familie.ef.iverksett.util

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import java.time.ZonedDateTime
import java.util.UUID

fun opprettBehandlingStatistikkDto(behandlingId: UUID, hendelse: Hendelse, fortrolig: Boolean): BehandlingStatistikkDto {
    return BehandlingStatistikkDto(behandlingId = UUID.randomUUID(),
                                   personIdent = "aktor",
                                   gjeldendeSaksbehandlerId = "saksbehandler",
                                   saksnummer = "saksnummer",
                                   hendelseTidspunkt = ZonedDateTime.now(),
                                   hendelse = hendelse,
                                   behandlingResultat = "",
                                   resultatBegrunnelse = "",
                                   ansvarligEnhet = "ansvarligEnhet",
                                   opprettetEnhet = "opprettetEnhet",
                                   strengtFortroligAdresse = fortrolig,
                                   stønadstype = StønadType.OVERGANGSSTØNAD,
                                   behandlingstype = BehandlingType.FØRSTEGANGSBEHANDLING)
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
                         sakYtelse = "sakYtelse"
    )
}