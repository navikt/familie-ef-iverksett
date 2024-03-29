package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingKategori
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
        return BehandlingDVH(
            behandlingId = behandlingstatistikk.eksternBehandlingId,
            sakId = behandlingstatistikk.eksternFagsakId,
            personIdent = behandlingstatistikk.personIdent,
            registrertTid =
                behandlingstatistikk.behandlingOpprettetTidspunkt
                    ?: behandlingstatistikk.hendelseTidspunkt,
            endretTid = behandlingstatistikk.hendelseTidspunkt,
            tekniskTid = tekniskTid,
            behandlingStatus = behandlingstatistikk.hendelse.name,
            opprettetAv =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingstatistikk.strengtFortroligAdresse,
                    behandlingstatistikk.gjeldendeSaksbehandlerId,
                ),
            saksnummer = behandlingstatistikk.eksternFagsakId,
            mottattTid = behandlingstatistikk.henvendelseTidspunkt,
            saksbehandler =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingstatistikk.strengtFortroligAdresse,
                    behandlingstatistikk.gjeldendeSaksbehandlerId,
                ),
            opprettetEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingstatistikk.strengtFortroligAdresse,
                    behandlingstatistikk.opprettetEnhet,
                ),
            ansvarligEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingstatistikk.strengtFortroligAdresse,
                    behandlingstatistikk.ansvarligEnhet,
                ),
            behandlingMetode = behandlingstatistikk.behandlingMetode?.name ?: "MANUELL",
            behandlingÅrsak = behandlingstatistikk.behandlingÅrsak?.name,
            avsender = "NAV enslig forelder",
            behandlingType = behandlingstatistikk.behandlingstype.name,
            sakYtelse = behandlingstatistikk.stønadstype.name,
            behandlingResultat = behandlingstatistikk.behandlingResultat,
            resultatBegrunnelse = behandlingstatistikk.resultatBegrunnelse,
            ansvarligBeslutter =
                if (Hendelse.BESLUTTET == behandlingstatistikk.hendelse && behandlingstatistikk.beslutterId.isNotNullOrEmpty()) {
                    maskerVerdiHvisStrengtFortrolig(
                        behandlingstatistikk.strengtFortroligAdresse,
                        behandlingstatistikk.beslutterId.toString(),
                    )
                } else {
                    null
                },
            vedtakTid =
                if (Hendelse.VEDTATT == behandlingstatistikk.hendelse) {
                    behandlingstatistikk.hendelseTidspunkt
                } else {
                    null
                },
            ferdigBehandletTid =
                if (Hendelse.FERDIG == behandlingstatistikk.hendelse) {
                    behandlingstatistikk.hendelseTidspunkt
                } else {
                    null
                },
            totrinnsbehandling = true,
            sakUtland = mapTilStreng(behandlingstatistikk.kategori),
            relatertBehandlingId = behandlingstatistikk.relatertEksternBehandlingId,
            kravMottatt = behandlingstatistikk.kravMottatt,
            revurderingÅrsak = behandlingstatistikk.årsakRevurdering?.årsak?.name,
            revurderingOpplysningskilde = behandlingstatistikk.årsakRevurdering?.opplysningskilde?.name,
            avslagAarsak = behandlingstatistikk.avslagÅrsak?.name,
        )
    }

    fun String?.isNotNullOrEmpty() = this != null && this.isNotEmpty()

    private fun maskerVerdiHvisStrengtFortrolig(
        erStrengtFortrolig: Boolean,
        verdi: String,
    ): String {
        if (erStrengtFortrolig) {
            return "-5"
        }
        return verdi
    }

    private fun mapTilStreng(kategori: BehandlingKategori?) =
        when (kategori) {
            BehandlingKategori.EØS -> "Utland"
            BehandlingKategori.NASJONAL -> "Nasjonal"
            null -> "Nasjonal"
        }
}
