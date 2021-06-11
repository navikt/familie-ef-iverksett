package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil
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
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeDto
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Barn as BarnEkstern

class BehandlingDVHMapper {

    companion object {

        fun map(iverksett: Iverksett): BehandlingDVH {
            return BehandlingDVH(fagsakId = iverksett.fagsak.fagsakId.toString(),
                                 behandlingId = iverksett.behandling.behandlingId.toString(),
                                 relatertBehandlingId = iverksett.behandling.relatertBehandlingId?.toString(),
                                 adressebeskyttelse = iverksett.søker.adressebeskyttelse?.let { Adressebeskyttelse.valueOf(it.name) }
                                                      ?: null,
                                 tidspunktVedtak = iverksett.vedtak.vedtaksdato.atStartOfDay(ZoneId.of("Europe/Paris")),
                                 vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                                 person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                                 barn = iverksett.søker.barn.map { mapTilBarn(it) },
                                 behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                                 behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                                 vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                                 vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak.vedtaksperioder),
                                 utbetalinger = mapTilUtbetaling(iverksett.vedtak,
                                                                 iverksett.fagsak.stønadstype,
                                                                 iverksett.fagsak.eksternId),
                                 aktivitetskrav = Aktivitetskrav(
                                         aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                                         harSagtOppArbeidsforhold = VilkårsvurderingUtil.hentHarSagtOppEllerRedusertFraVurderinger(
                                                 iverksett.behandling.vilkårsvurderinger)
                                 ),
                                 funksjonellId = iverksett.behandling.eksternId.toString())

        }

        private fun mapTilUtbetaling(vedtaksdetaljer: Vedtaksdetaljer,
                                     stønadsType: StønadType,
                                     eksternFagsakId: Long): List<Utbetaling> {
            return vedtaksdetaljer.tilkjentYtelse.andelerTilkjentYtelse.map {
                Utbetaling(
                        beløp = it.beløp,
                        samordningsfradrag = it.samordningsfradrag,
                        inntekt = it.inntekt,
                        inntektsreduksjon = it.inntektsreduksjon,
                        fraOgMed = it.fraOgMed,
                        tilOgMed = it.tilOgMed,
                        Utbetalingsdetalj(gjelderPerson = Person("12345678910"),
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
            return vedtaksperioder.map { VedtaksperiodeDto(it.fraOgMed, it.tilOgMed, it.aktivitet, it.periodeType) }
        }
    }
}