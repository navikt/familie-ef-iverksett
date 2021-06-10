package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class BehandlingstatistikkService(private val behandlingstatistikkProducer: BehandlingstatistikkProducer,
                                  private val behandlingstatistikkRepository: BehandlingstatistikkRepository) {

    @Transactional
    fun lagreBehandlingstatistikk(behandlingstatistikk: BehandlingStatistikkDto) {
        val behandlingDVH = mapTilBehandlingDVH(behandlingstatistikk)
        behandlingstatistikkRepository.lagre(behandlingstatistikk.behandlingId,
                                             behandlingDVH,
                                             behandlingstatistikk.hendelse)
        behandlingstatistikkProducer.sendBehandling(behandlingDVH)
    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto): BehandlingDVH {

        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> {
                BehandlingDVH(behandlingId = behandlingstatistikk.behandlingId.toString(),
                              sakId = behandlingstatistikk.saksnummer,
                              aktorId = behandlingstatistikk.personIdent,
                              registrertTid = behandlingstatistikk.hendelseTidspunkt,
                              endretTid = behandlingstatistikk.hendelseTidspunkt,
                              tekniskTid = behandlingstatistikk.hendelseTidspunkt,
                              behandlingStatus = Hendelse.MOTTATT.toString(),
                              opprettetAv = behandlingstatistikk.gjeldendeSaksbehandlerId,
                              saksnummer = behandlingstatistikk.saksnummer,
                              mottattTid = behandlingstatistikk.hendelseTidspunkt,
                              saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                              opprettetEnhet = "",
                              ansvarligEnhet = "",
                              behandlingMetode = "MANUELL",
                              avsender = "NAV enslig forelder",
                              behandlingType = "Førstegangsbehandling",
                              sakYtelse = "EFOG"
                )

            }
            Hendelse.PÅBEGYNT -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.MOTTATT)
                behandlingDVH!!.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                     tekniskTid = ZonedDateTime.now(),
                                     saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     behandlingStatus = Hendelse.MOTTATT.toString())

            }
            Hendelse.VEDTATT -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.PÅBEGYNT)
                behandlingDVH!!.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                     vedtakTid = behandlingstatistikk.hendelseTidspunkt,
                                     tekniskTid = ZonedDateTime.now(),
                                     saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     behandlingStatus = Hendelse.VEDTATT.toString())


            }
            Hendelse.BESLUTTET -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
                behandlingDVH!!.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                     vedtakTid = behandlingstatistikk.hendelseTidspunkt,
                                     tekniskTid = ZonedDateTime.now(),
                                     saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     ansvarligBeslutter = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     behandlingStatus = Hendelse.BESLUTTET.toString(),
                                     behandlingResultat = behandlingstatistikk.behandlingResultat,
                                     resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse)
            }
            Hendelse.FERDIG -> {
                val behandlingDVH = behandlingstatistikkRepository.hent(behandlingstatistikk.behandlingId, Hendelse.VEDTATT)
                behandlingDVH!!.copy(endretTid = behandlingstatistikk.hendelseTidspunkt,
                                     tekniskTid = ZonedDateTime.now(),
                                     ferdigBehandletTid = behandlingstatistikk.hendelseTidspunkt,
                                     saksbehandler = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     ansvarligBeslutter = behandlingstatistikk.gjeldendeSaksbehandlerId,
                                     behandlingStatus = Hendelse.BESLUTTET.toString())
            }
        }
    }

}
