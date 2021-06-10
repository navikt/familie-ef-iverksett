package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil.hentHarSagtOppEllerRedusertFraVurderinger
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vedtaksperiode
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.kontrakter.ef.felles.StønadType
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsvurdering as VilkårsvurderingEkstern
import no.nav.familie.eksterne.kontrakter.ef.Barn as BarnEkstern

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    val europeiskZoneId = ZoneId.of("Europe/Paris")

    fun sendTilKafka(iverksett: Iverksett) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksett.fagsak.fagsakId.toString(),
                             behandlingId = iverksett.behandling.behandlingId.toString(),
                             relatertBehandlingId = iverksett.behandling.relatertBehandlingId?.toString(),
                             adressebeskyttelse = Adressebeskyttelse.valueOf(iverksett.søker.adressebeskyttelse!!.name), //TODO nullable
                             tidspunktVedtak = iverksett.vedtak.vedtaksdato.atStartOfDay(europeiskZoneId),
                             vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                             barn = iverksett.søker.barn.map { mapTilBarn(it) },
                             behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                             vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                             vedtaksperioder = emptyList(), //TODO
                             utbetalinger = mapTilUtbetaling(iverksett.vedtak,
                                                             iverksett.fagsak.stønadstype,
                                                             iverksett.fagsak.eksternId), //TODO
                             aktivitetskrav = Aktivitetskrav(
                                     aktivitetspliktInntrefferDato = LocalDate.now(), //TODO time
                                     harSagtOppArbeidsforhold = hentHarSagtOppEllerRedusertFraVurderinger(iverksett.behandling.vilkårsvurderinger)
                             ),
                             funksjonellId = iverksett.behandling.eksternId.toString())

    }

    private fun mapTilUtbetaling(vedtaksdetaljer: Vedtaksdetaljer,
                                 stønadsType: StønadType,
                                 eksternFagsakId: Long): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(
                    beløp = it.beløp,
                    samordningsfradrag = 0 ,//TODO iverksettDTO
                            inntekt =0,//
                    inntektsreduksjon = 0,//
                    fraOgMed = it.fraOgMed,
                    tilOgMed = it.tilOgMed,
                    Utbetalingsdetalj(gjelderPerson = Person(),
                            klassekode = stønadsType.tilKlassifisering(),
                                      delytelseId = eksternFagsakId.toString() + it.periodeId))
        }
    }


    private fun mapTilPerson(personIdent: String?): Person {
        return Person(personIdent = personIdent)
    }

    private fun mapTilBarn(barn: Barn): BarnEkstern {
        return BarnEkstern(personIdent = barn.personIdent, termindato = barn.termindato)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurdering: Vilkårsvurdering): VilkårsvurderingEkstern {
        return VilkårsvurderingEkstern(
                vilkår = Vilkår.valueOf(vilkårsvurdering.vilkårType.name),
                resultat = Vilkårsresultat.valueOf(vilkårsvurdering.resultat.name),
        )
    }

}