package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.BehandlingKategori
import no.nav.familie.kontrakter.ef.iverksett.BehandlingMetode
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsstatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class BehandlingsstatistikkService(
    private val behandlingsstatistikkProducer: BehandlingsstatistikkProducer,
) {
    @Transactional
    fun sendBehandlingstatistikk(behandlingsstatistikkDto: BehandlingsstatistikkDto) {
        val behandlingDVH = mapTilBehandlingDVH(behandlingsstatistikkDto)
        behandlingsstatistikkProducer.sendBehandling(behandlingDVH)
    }

    fun mapGOmregningIverksettingTilBehandlingDVH(iverksett: IverksettOvergangsstønad): BehandlingDVH {
        val zoneId = ZoneId.of("Europe/Oslo")
        val tekniskTid = ZonedDateTime.now(zoneId)
        return BehandlingDVH(
            behandlingId = iverksett.behandling.eksternId,
            sakId = iverksett.fagsak.eksternId,
            personIdent = iverksett.søker.personIdent,
            registrertTid = iverksett.vedtak.vedtakstidspunkt.atZone(zoneId),
            endretTid = iverksett.vedtak.vedtakstidspunkt.atZone(zoneId),
            tekniskTid = tekniskTid,
            behandlingStatus = Hendelse.FERDIG.name,
            opprettetAv =
                maskerVerdiHvisStrengtFortrolig(
                    iverksett.søker.adressebeskyttelse.erStrengtFortrolig(),
                    iverksett.vedtak.saksbehandlerId,
                ),
            saksnummer = iverksett.fagsak.eksternId,
            mottattTid = iverksett.vedtak.vedtakstidspunkt.atZone(zoneId),
            saksbehandler =
                maskerVerdiHvisStrengtFortrolig(
                    iverksett.søker.adressebeskyttelse.erStrengtFortrolig(),
                    iverksett.vedtak.saksbehandlerId,
                ),
            opprettetEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    iverksett.søker.adressebeskyttelse.erStrengtFortrolig(),
                    MASKINELL_JOURNALFOERENDE_ENHET,
                ),
            ansvarligEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    iverksett.søker.adressebeskyttelse.erStrengtFortrolig(),
                    MASKINELL_JOURNALFOERENDE_ENHET,
                ),
            behandlingMetode = BehandlingMetode.BATCH.name,
            behandlingÅrsak = iverksett.behandling.behandlingÅrsak.name,
            avsender = "NAV enslig forelder",
            behandlingType = iverksett.behandling.behandlingType.name,
            sakYtelse = iverksett.fagsak.stønadstype.name,
            behandlingResultat = iverksett.vedtak.vedtaksresultat.name,
            resultatBegrunnelse = "G-omregning",
            ansvarligBeslutter = null,
            vedtakTid = iverksett.vedtak.vedtakstidspunkt.atZone(zoneId),
            ferdigBehandletTid = iverksett.vedtak.vedtakstidspunkt.atZone(zoneId),
            totrinnsbehandling = false,
            sakUtland = mapTilStreng(iverksett.behandling.kategori),
            relatertBehandlingId = iverksett.behandling.forrigeBehandlingEksternId,
            kravMottatt = null,
            revurderingÅrsak = BehandlingÅrsak.G_OMREGNING.name,
            revurderingOpplysningskilde = null,
            avslagAarsak = null,
        )
    }

    private fun mapTilBehandlingDVH(behandlingsstatistikkDto: BehandlingsstatistikkDto): BehandlingDVH {
        val tekniskTid = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        return BehandlingDVH(
            behandlingId = behandlingsstatistikkDto.eksternBehandlingId,
            sakId = behandlingsstatistikkDto.eksternFagsakId,
            personIdent = behandlingsstatistikkDto.personIdent,
            registrertTid =
                behandlingsstatistikkDto.behandlingOpprettetTidspunkt
                    ?: behandlingsstatistikkDto.hendelseTidspunkt,
            endretTid = behandlingsstatistikkDto.hendelseTidspunkt,
            tekniskTid = tekniskTid,
            behandlingStatus = behandlingsstatistikkDto.hendelse.name,
            opprettetAv =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingsstatistikkDto.strengtFortroligAdresse,
                    behandlingsstatistikkDto.gjeldendeSaksbehandlerId,
                ),
            saksnummer = behandlingsstatistikkDto.eksternFagsakId,
            mottattTid = behandlingsstatistikkDto.henvendelseTidspunkt,
            saksbehandler =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingsstatistikkDto.strengtFortroligAdresse,
                    behandlingsstatistikkDto.gjeldendeSaksbehandlerId,
                ),
            opprettetEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingsstatistikkDto.strengtFortroligAdresse,
                    behandlingsstatistikkDto.opprettetEnhet,
                ),
            ansvarligEnhet =
                maskerVerdiHvisStrengtFortrolig(
                    behandlingsstatistikkDto.strengtFortroligAdresse,
                    behandlingsstatistikkDto.ansvarligEnhet,
                ),
            behandlingMetode = behandlingsstatistikkDto.behandlingMetode?.name ?: "MANUELL",
            behandlingÅrsak = behandlingsstatistikkDto.behandlingÅrsak.name,
            avsender = "NAV enslig forelder",
            behandlingType = behandlingsstatistikkDto.behandlingstype.name,
            sakYtelse = behandlingsstatistikkDto.stønadstype.name,
            behandlingResultat = behandlingsstatistikkDto.behandlingResultat,
            resultatBegrunnelse = behandlingsstatistikkDto.resultatBegrunnelse,
            ansvarligBeslutter =
                if (Hendelse.BESLUTTET == behandlingsstatistikkDto.hendelse && behandlingsstatistikkDto.beslutterId.isNotNullOrEmpty()) {
                    maskerVerdiHvisStrengtFortrolig(
                        behandlingsstatistikkDto.strengtFortroligAdresse,
                        behandlingsstatistikkDto.beslutterId.toString(),
                    )
                } else {
                    null
                },
            vedtakTid =
                if (Hendelse.VEDTATT == behandlingsstatistikkDto.hendelse) {
                    behandlingsstatistikkDto.hendelseTidspunkt
                } else {
                    null
                },
            ferdigBehandletTid =
                if (Hendelse.FERDIG == behandlingsstatistikkDto.hendelse) {
                    behandlingsstatistikkDto.hendelseTidspunkt
                } else {
                    null
                },
            totrinnsbehandling =
                if (Hendelse.BESLUTTET == behandlingsstatistikkDto.hendelse) {
                    true
                } else {
                    false
                },
            sakUtland = mapTilStreng(behandlingsstatistikkDto.kategori),
            relatertBehandlingId = behandlingsstatistikkDto.relatertEksternBehandlingId,
            kravMottatt = behandlingsstatistikkDto.kravMottatt,
            revurderingÅrsak = behandlingsstatistikkDto.årsakRevurdering?.årsak?.name,
            revurderingOpplysningskilde = behandlingsstatistikkDto.årsakRevurdering?.opplysningskilde?.name,
            avslagAarsak = behandlingsstatistikkDto.avslagÅrsak?.name,
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

    companion object {
        const val MASKINELL_JOURNALFOERENDE_ENHET = "9999"
    }
}

fun AdressebeskyttelseGradering?.erStrengtFortrolig() = this == AdressebeskyttelseGradering.STRENGT_FORTROLIG || this == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
