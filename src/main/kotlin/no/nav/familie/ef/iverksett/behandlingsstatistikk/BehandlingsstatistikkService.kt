package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.util.toJson
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class BehandlingsstatistikkService(private val kafkaOperations: KafkaOperations<String, String>,
                                   @Value("\${ENSLIG_FORSORGER_BEHANDLING_TOPIC}") val topic: String) {



    @Transactional
    fun sendBehandlingstatistikk(behandlingsstatistikkDto: BehandlingsstatistikkDto) {
        val behandlingDvh = mapTilBehandlingDVH(behandlingsstatistikkDto)
        kafkaOperations.send(topic, behandlingDvh.behandlingId.toString(), behandlingDvh.toJson())
                .addCallback(BehandlingsstatistikkCallback(behandlingDvh))
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
                             opprettetAv = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                 behandlingstatistikk.gjeldendeSaksbehandlerId),
                             saksnummer = behandlingstatistikk.eksternFagsakId,
                             mottattTid = behandlingstatistikk.henvendelseTidspunkt,
                             saksbehandler = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                   behandlingstatistikk.gjeldendeSaksbehandlerId),
                             opprettetEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                    behandlingstatistikk.opprettetEnhet),
                             ansvarligEnhet = sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                                    behandlingstatistikk.ansvarligEnhet),
                             behandlingMetode = "MANUELL",
                             avsender = "NAV enslig forelder",
                             behandlingType = behandlingstatistikk.behandlingstype.name,
                             sakYtelse = behandlingstatistikk.stønadstype.name,
                             behandlingResultat = behandlingstatistikk.behandlingResultat,
                             resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse,
                             ansvarligBeslutter = if (Hendelse.BESLUTTET == behandlingstatistikk.hendelse)
                                 sjekkStrengtFortrolig(behandlingstatistikk.strengtFortroligAdresse,
                                                       behandlingstatistikk.gjeldendeSaksbehandlerId) else null,
                             vedtakTid = if (Hendelse.VEDTATT == behandlingstatistikk.hendelse)
                                 behandlingstatistikk.hendelseTidspunkt else null,
                             ferdigBehandletTid = if (Hendelse.FERDIG == behandlingstatistikk.hendelse)
                                 behandlingstatistikk.hendelseTidspunkt else null,
                             totrinnsbehandling = true,
                             sakUtland = "Nasjonal",
                             relatertBehandlingId = behandlingstatistikk.relatertEksternBehandlingId)

    }

    private fun sjekkStrengtFortrolig(erStrengtFortrolig: Boolean,
                                      verdi: String): String {
        if (erStrengtFortrolig) {
            return "-5"
        }
        return verdi
    }

}
