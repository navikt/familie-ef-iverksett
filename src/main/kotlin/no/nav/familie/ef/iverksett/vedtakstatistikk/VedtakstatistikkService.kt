package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Inntekt
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering.UGRADERT
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import org.springframework.stereotype.Service
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsvurdering as VilkårsvurderingEkstern

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    val europeiskZoneId = ZoneId.of("Europe/Paris")

    fun sendTilKafka(iverksett: Iverksett) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksett.fagsak.fagsakId.toString(),
                             saksnummer = iverksett.fagsak.eksternId.toString(),
                             behandlingId = iverksett.behandling.behandlingId.toString(),
                             relatertBehandlingId = iverksett.behandling.relatertBehandlingId?.toString(),
                             kode6eller7 = iverksett.søker.adressebeskyttelse?.let { it != UGRADERT } ?: false,
                             tidspunktVedtak = iverksett.vedtak.vedtaksdato.atStartOfDay(europeiskZoneId),
                             vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                             barn = iverksett.søker.barn.map { mapTilPerson(it.personIdent) }, // TODO: Map termindato hvis vi ikke har personident
                             behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                             vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                             utbetalinger = mapTilUtbetaling(iverksett.vedtak.tilkjentYtelse,
                                                             iverksett.fagsak.stønadstype,
                                                             iverksett.fagsak.eksternId),
                             inntekt = iverksett.vedtak.inntekter.map { inntekt ->
                                 Inntekt(
                                         beløp = inntekt.beløp,
                                         samordningsfradrag = inntekt.samordningsfradrag,
                                         fraOgMed = inntekt.fraOgMed,
                                         tilOgMed = inntekt.tilOgMed
                                 )
                             },
                             aktivitetskrav = Aktivitetskrav(harSagtOppArbeidsforhold = hentHarSagtOppEllerRedusertFraVurderinger(
                                     iverksett.behandling.vilkårsvurderinger)),
                             funksjonellId = iverksett.behandling.eksternId.toString())

    }

    private fun hentHarSagtOppEllerRedusertFraVurderinger(vilkårsvurderinger: List<Vilkårsvurdering>): Boolean? {
        val vilkårsvurdering = vilkårsvurderinger.find { it.vilkårType == VilkårType.SAGT_OPP_ELLER_REDUSERT }
                               ?: error("Finner ikke vurderingen for sagt opp eller redusert")

        return if (vilkårsvurdering.resultat == Vilkårsresultat.SKAL_IKKE_VURDERES) {
            null
        } else {
            vilkårsvurdering.delvilkårsvurderinger.flatMap { it.vurderinger }
                    .firstOrNull { it.regelId == RegelId.SAGT_OPP_ELLER_REDUSERT }
                    ?.let { harSagtOppEllerRedusertStilling(it.svar) }
            ?: error("Finner ikke delvilkårsvurderingen for sagt opp eller redusert stilling")
        }
    }

    private fun harSagtOppEllerRedusertStilling(svarId: SvarId?) = when (svarId) {
        SvarId.JA -> true
        SvarId.NEI -> false
        else -> error("Sagt opp eller redusert har bara ja eller nej som svarslaternativ $svarId")

    }

    private fun mapTilUtbetaling(tilkjentYtelse: TilkjentYtelse,
                                 stønadsType: StønadType,
                                 eksternFagsakId: Long): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(
                    beløp = it.periodebeløp.beløp,
                    fraOgMed = it.periodebeløp.fraOgMed,
                    tilOgMed = it.periodebeløp.tilOgMed,
                    Utbetalingsdetalj(klassekode = stønadsType.tilKlassifisering(),
                                      delytelseId = eksternFagsakId.toString() + it.periodeId))
        }
    }


    private fun mapTilPerson(personIdent: String?): Person {
        return Person(personIdent = personIdent)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurdering: Vilkårsvurdering): VilkårsvurderingEkstern {
        return VilkårsvurderingEkstern(
                vilkår = Vilkår.valueOf(vilkårsvurdering.vilkårType.name),
                oppfylt = erVilkårOppfylt(vilkårsvurdering.resultat),
        )
    }

    private fun erVilkårOppfylt(resultat: Vilkårsresultat): Boolean? {
        return when (resultat) {
            Vilkårsresultat.OPPFYLT -> true
            Vilkårsresultat.IKKE_OPPFYLT -> false
            else -> null
        }
    }


}