package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingBarnetilsynDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.PeriodeMedBeløp
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.VedtakOvergangsstønadDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeBarnetilsynDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeOvergangsstønadDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Barn as BarnEkstern
import no.nav.familie.eksterne.kontrakter.ef.StønadType as StønadTypeEkstern

object VedtakstatistikkMapper {

    fun mapTilVedtakBarnetilsynDVH(iverksett: IverksettBarnetilsyn, forrigeIverksettBehandlingEksternId: Long?): BehandlingBarnetilsynDVH {
        return BehandlingBarnetilsynDVH(fagsakId = iverksett.fagsak.eksternId,
                                        behandlingId = iverksett.behandling.eksternId,
                                        relatertBehandlingId = forrigeIverksettBehandlingEksternId,
                                        adressebeskyttelse = iverksett.søker.adressebeskyttelse?.let {
                                            Adressebeskyttelse.valueOf(it.name)
                                        },
                                        tidspunktVedtak = iverksett.vedtak.vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")),
                                        vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map {
                                            mapTilVilkårsvurderinger(it)
                                        },
                                        person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                                        barn = iverksett.søker.barn.map { mapTilBarn(it) },
                                        behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                                        behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                                        vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                                        vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak),
                                        utbetalinger = iverksett.vedtak.tilkjentYtelse?.let {
                                            mapTilUtbetaling(it,
                                                             iverksett.fagsak.stønadstype,
                                                             iverksett.fagsak.eksternId,
                                                             iverksett.søker)
                                        } ?: emptyList(),
                                        aktivitetskrav = Aktivitetskrav(
                                                aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                                                harSagtOppArbeidsforhold = VilkårsvurderingUtil
                                                        .hentHarSagtOppEllerRedusertFraVurderinger(iverksett.behandling.vilkårsvurderinger)
                                        ),
                                        funksjonellId = iverksett.behandling.eksternId,
                                        stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name),
                                        perioderKontantstøtte = iverksett.vedtak.kontantstøtte.map { PeriodeMedBeløp(it.fraOgMed, it.tilOgMed, it.beløp) },
                                        perioderTilleggsstønad = iverksett.vedtak.tilleggsstønad.map { PeriodeMedBeløp(it.fraOgMed, it.tilOgMed, it.beløp) })
    }

    fun mapTilVedtakOvergangsstønadDVH(iverksett: IverksettOvergangsstønad,
                                                   forrigeIverksettBehandlingEksternId: Long?): VedtakOvergangsstønadDVH {
        return VedtakOvergangsstønadDVH(fagsakId = iverksett.fagsak.eksternId,
                                        behandlingId = iverksett.behandling.eksternId,
                                        relatertBehandlingId = forrigeIverksettBehandlingEksternId,
                                        adressebeskyttelse = iverksett.søker.adressebeskyttelse?.let {
                                            Adressebeskyttelse.valueOf(it.name)
                                        },
                                        tidspunktVedtak = iverksett.vedtak.vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")),
                                        vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map {
                                            mapTilVilkårsvurderinger(it)
                                        },
                                        person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                                        barn = iverksett.søker.barn.map { mapTilBarn(it) },
                                        behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                                        behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                                        vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
                                        vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak),
                                        utbetalinger = iverksett.vedtak.tilkjentYtelse?.let {
                                            mapTilUtbetaling(it,
                                                             iverksett.fagsak.stønadstype,
                                                             iverksett.fagsak.eksternId,
                                                             iverksett.søker)
                                        } ?: emptyList(),
                                        aktivitetskrav = Aktivitetskrav(
                                                aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                                                harSagtOppArbeidsforhold = VilkårsvurderingUtil
                                                        .hentHarSagtOppEllerRedusertFraVurderinger(iverksett.behandling.vilkårsvurderinger)
                                        ),
                                        funksjonellId = iverksett.behandling.eksternId,
                                        stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name))
    }

    private fun mapTilUtbetaling(tilkjentYtelse: TilkjentYtelse,
                                 stønadsType: StønadType,
                                 eksternFagsakId: Long,
                                 søker: Søker): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(beløp = it.beløp,
                       samordningsfradrag = it.samordningsfradrag,
                       inntekt = it.inntekt,
                       inntektsreduksjon = it.inntektsreduksjon,
                       fraOgMed = it.fraOgMed,
                       tilOgMed = it.tilOgMed,
                       Utbetalingsdetalj(gjelderPerson = mapTilPerson(personIdent = søker.personIdent),
                                         klassekode = stønadsType.tilKlassifisering(),
                                         delytelseId = eksternFagsakId.toString() + (it.periodeId ?: "")))
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

    private fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerOvergangsstønad): List<VedtaksperiodeOvergangsstønadDto> {
        return vedtaksdetaljer.vedtaksperioder.map {
            VedtaksperiodeOvergangsstønadDto(it.fraOgMed,
                                             it.tilOgMed,
                                             AktivitetType.valueOf(it.aktivitet.name),
                                             VedtaksperiodeType.valueOf(it.periodeType.name))
        }
    }

    private fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerBarnetilsyn): List<VedtaksperiodeBarnetilsynDto> {
        return vedtaksdetaljer.vedtaksperioder.map {
            VedtaksperiodeBarnetilsynDto(it.fraOgMed,
                                         it.tilOgMed,
                                         it.utgifter,
                                         it.antallBarn)
        }
    }
}