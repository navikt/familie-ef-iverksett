package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Barn as BarnEkstern
import no.nav.familie.eksterne.kontrakter.ef.StønadType as StønadTypeEkstern

class BehandlingDVHMapper {

    companion object {

        fun map(iverksett: Iverksett, tilkjentYtelse: TilkjentYtelse): BehandlingDVH {
            return BehandlingDVH(fagsakId = iverksett.fagsak.fagsakId.toString(),
                                 behandlingId = iverksett.behandling.behandlingId.toString(),
                                 relatertBehandlingId = iverksett.behandling.relatertBehandlingId?.toString(),
                                 adressebeskyttelse = iverksett.søker.adressebeskyttelse?.let { Adressebeskyttelse.valueOf(it.name) },
                                 tidspunktVedtak = iverksett.vedtak.vedtaksdato.atStartOfDay(ZoneId.of("Europe/Oslo")),
                                 vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                                 person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                                 barn = iverksett.søker.barn.map { mapTilBarn(it) },
                                 behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                                 behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                                 vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                                 vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak.vedtaksperioder),
                                 utbetalinger = mapTilUtbetaling(tilkjentYtelse,
                                                                 iverksett.fagsak.stønadstype,
                                                                 iverksett.fagsak.eksternId,
                                                                 iverksett.søker),
                                 aktivitetskrav = Aktivitetskrav(
                                         aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                                         harSagtOppArbeidsforhold = VilkårsvurderingUtil.hentHarSagtOppEllerRedusertFraVurderinger(
                                                 iverksett.behandling.vilkårsvurderinger)
                                 ),
                                 funksjonellId = iverksett.behandling.eksternId.toString(),
                                 stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name))

        }

        private fun mapTilUtbetaling(tilkjentYtelse: TilkjentYtelse,
                                     stønadsType: StønadType,
                                     eksternFagsakId: Long,
                                     søker: Søker): List<Utbetaling> {
            return tilkjentYtelse.andelerTilkjentYtelse.map {
                Utbetaling(
                        beløp = it.beløp,
                        samordningsfradrag = it.samordningsfradrag,
                        inntekt = it.inntekt,
                        inntektsreduksjon = it.inntektsreduksjon,
                        fraOgMed = it.fraOgMed,
                        tilOgMed = it.tilOgMed,
                        Utbetalingsdetalj(gjelderPerson = mapTilPerson(personIdent = søker.personIdent),
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

        private fun mapTilVilkårsvurderinger(vilkårsvurdering: Vilkårsvurdering): VilkårsvurderingDto {
            return VilkårsvurderingDto(
                    vilkår = Vilkår.valueOf(vilkårsvurdering.vilkårType.name),
                    resultat = Vilkårsresultat.valueOf(vilkårsvurdering.resultat.name),
            )
        }

        private fun mapToVedtaksperioder(vedtaksperioder: List<Vedtaksperiode>): List<VedtaksperiodeDto> {
            return vedtaksperioder.map {
                VedtaksperiodeDto(it.fraOgMed,
                                  it.tilOgMed,
                                  AktivitetType.valueOf(it.aktivitet.name),
                                  VedtaksperiodeType.valueOf(it.periodeType.name))
            }
        }
    }
}