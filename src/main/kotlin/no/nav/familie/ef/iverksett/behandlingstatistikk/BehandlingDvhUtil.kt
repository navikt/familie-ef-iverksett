package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import java.time.ZonedDateTime

class BehandlingDvhUtil {

    companion object {

        fun byggBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto,
                              behandlingDvhBuilder: BehandlingDvhBuilder.Builder): BehandlingDVH {
            return byggBehandlingDVH(behandlingstatistikk, behandlingDvhBuilder, behandlingstatistikk.hendelseTidspunkt)
        }
        fun byggBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto,
                              behandlingDvhBuilder: BehandlingDvhBuilder.Builder, endretTid: ZonedDateTime): BehandlingDVH {
            return behandlingDvhBuilder.build(
                    behandlingId = behandlingstatistikk.behandlingId.toString(),
                    aktorId = behandlingstatistikk.personIdent,
                    registrertTid = behandlingstatistikk.hendelseTidspunkt,
                    endretTid = endretTid,
                    tekniskTid = ZonedDateTime.now(),
                    behandlingStatus = behandlingstatistikk.hendelse.toString(),
                    opprettetAv = behandlingstatistikk.gjeldendeSaksbehandlerId,
                    saksnummer = behandlingstatistikk.saksnummer,
                    saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId)

        }

    }
}