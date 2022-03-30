package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class BehandlingsstatistikkService(private val behandlingsstatistikkProducer: BehandlingsstatistikkProducer) {

    @Transactional
    fun sendBehandlingstatistikk(behandlingsstatistikkDto: BehandlingsstatistikkDto) {
        val behandlingDVH = mapTilBehandlingDVH(behandlingsstatistikkDto)
        behandlingsstatistikkProducer.sendBehandling(behandlingDVH)
    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingsstatistikkDto): BehandlingDVH {

        val tekniskTid = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        return BehandlingDVH(behandlingId = behandlingstatistikk.eksternBehandlingId,
                             sakId = behandlingstatistikk.eksternFagsakId,
                             personIdent = behandlingstatistikk.personIdent,
                             registrertTid = behandlingstatistikk.behandlingOpprettetTidspunkt
                                             ?: behandlingstatistikk.hendelseTidspunkt,
                             endretTid = behandlingstatistikk.hendelseTidspunkt,
                             tekniskTid = tekniskTid,
                             behandlingStatus = behandlingstatistikk.hendelse.name,
                             opprettetAv = maskerVerdiHvisFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                    behandlingstatistikk.gjeldendeSaksbehandlerId),
                             saksnummer = behandlingstatistikk.eksternFagsakId,
                             mottattTid = behandlingstatistikk.henvendelseTidspunkt,
                             saksbehandler = maskerVerdiHvisFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                      behandlingstatistikk.gjeldendeSaksbehandlerId),
                             opprettetEnhet = maskerVerdiHvisFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                       behandlingstatistikk.opprettetEnhet),
                             ansvarligEnhet = maskerVerdiHvisFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                       behandlingstatistikk.ansvarligEnhet),
                             behandlingMetode = "MANUELL",
                             avsender = "NAV enslig forelder",
                             behandlingType = behandlingstatistikk.behandlingstype.name,
                             sakYtelse = behandlingstatistikk.stønadstype.name,
                             behandlingResultat = behandlingstatistikk.behandlingResultat,
                             resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse,
                             ansvarligBeslutter = if (Hendelse.BESLUTTET == behandlingstatistikk.hendelse && behandlingstatistikk.beslutterId.isNotNullOrEmpty())
                                 maskerVerdiHvisFortrolig(behandlingstatistikk.strengtFortroligAdresse, behandlingstatistikk.beslutterId.toString())
                                                else null,
                             vedtakTid = if (Hendelse.VEDTATT == behandlingstatistikk.hendelse)
                                 behandlingstatistikk.hendelseTidspunkt else null,
                             ferdigBehandletTid = if (Hendelse.FERDIG == behandlingstatistikk.hendelse)
                                 behandlingstatistikk.hendelseTidspunkt else null,
                             totrinnsbehandling = true,
                             sakUtland = "Nasjonal",
                             relatertBehandlingId = behandlingstatistikk.relatertEksternBehandlingId)

    }

    private fun maskerVerdiHvisFortrolig(erStrengtFortrolig: Boolean,
                                         verdi: String): String {
        if (erStrengtFortrolig) {
            return "-5"
        }
        return verdi
    }

    fun String?.isNotNullOrEmpty() = this != null && this.isNotEmpty()

}
