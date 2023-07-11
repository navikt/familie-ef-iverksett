package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.DelårsperiodeSkoleårSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.SkolepengerUtgift
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil
import no.nav.familie.ef.iverksett.util.VilkårsvurderingUtil.hentAktivitetsvilkårBarnetilsyn
import no.nav.familie.ef.iverksett.util.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Delårsperiode
import no.nav.familie.eksterne.kontrakter.ef.PeriodeMedBeløp
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Studietype
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.UtgiftSkolepenger
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.VedtakBarnetilsynDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtakOvergangsstønadDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtakSkolepenger
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeBarnetilsynDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeOvergangsstønadDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeSkolepenger
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.eksterne.kontrakter.ef.ÅrsakRevurdering
import no.nav.familie.kontrakter.felles.ef.StønadType
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.ZoneId
import no.nav.familie.eksterne.kontrakter.ef.Barn as BarnEkstern
import no.nav.familie.eksterne.kontrakter.ef.StønadType as StønadTypeEkstern

object VedtakstatistikkMapper {

    fun mapTilVedtakBarnetilsynDVH(
        iverksett: IverksettBarnetilsyn,
        forrigeIverksettBehandlingEksternId: Long?,
    ): VedtakBarnetilsynDVH {
        return VedtakBarnetilsynDVH(
            fagsakId = iverksett.fagsak.eksternId,
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
                mapTilUtbetaling(
                    it,
                    iverksett.fagsak.stønadstype,
                    iverksett.fagsak.eksternId,
                    iverksett.søker,
                )
            } ?: emptyList(),
            aktivitetskrav = hentAktivitetsvilkårBarnetilsyn(iverksett.behandling.vilkårsvurderinger),
            funksjonellId = iverksett.behandling.eksternId,
            stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name),
            perioderKontantstøtte = iverksett.vedtak.kontantstøtte.map {
                PeriodeMedBeløp(
                    it.periode.fomDato,
                    it.periode.tomDato,
                    it.beløp,
                )
            },
            perioderTilleggsstønad = iverksett.vedtak.tilleggsstønad.map {
                PeriodeMedBeløp(
                    it.periode.fomDato,
                    it.periode.tomDato,
                    it.beløp,
                )
            },
            kravMottatt = iverksett.behandling.kravMottatt,
            årsakRevurdering = mapÅrsakRevurdering(iverksett.behandling),
            avslagÅrsak = iverksett.vedtak.avslagÅrsak?.name,
        )
    }

    private fun mapÅrsakRevurdering(behandlingsdetaljer: Behandlingsdetaljer): ÅrsakRevurdering? =
        behandlingsdetaljer.årsakRevurdering?.let {
            ÅrsakRevurdering(
                opplysningskilde = it.opplysningskilde.name,
                årsak = it.årsak.name,
            )
        }

    fun mapTilVedtakOvergangsstønadDVH(
        iverksett: IverksettOvergangsstønad,
        forrigeIverksettBehandlingEksternId: Long?,
    ): VedtakOvergangsstønadDVH {
        return VedtakOvergangsstønadDVH(
            fagsakId = iverksett.fagsak.eksternId,
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
                mapTilUtbetaling(
                    it,
                    iverksett.fagsak.stønadstype,
                    iverksett.fagsak.eksternId,
                    iverksett.søker,
                )
            } ?: emptyList(),
            aktivitetskrav = Aktivitetskrav(
                aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                harSagtOppArbeidsforhold = VilkårsvurderingUtil
                    .hentHarSagtOppEllerRedusertFraVurderinger(iverksett.behandling.vilkårsvurderinger),
            ),
            funksjonellId = iverksett.behandling.eksternId,
            stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name),
            kravMottatt = iverksett.behandling.kravMottatt,
            årsakRevurdering = mapÅrsakRevurdering(iverksett.behandling),
            avslagÅrsak = iverksett.vedtak.avslagÅrsak?.name,
        )
    }

    private fun mapTilUtbetaling(
        tilkjentYtelse: TilkjentYtelse,
        stønadsType: StønadType,
        eksternFagsakId: Long,
        søker: Søker,
    ): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(
                beløp = it.beløp,
                samordningsfradrag = it.samordningsfradrag,
                inntekt = it.inntekt,
                inntektsreduksjon = it.inntektsreduksjon,
                fraOgMed = it.periode.fomDato,
                tilOgMed = it.periode.tomDato,
                Utbetalingsdetalj(
                    gjelderPerson = mapTilPerson(personIdent = søker.personIdent),
                    klassekode = stønadsType.tilKlassifisering(),
                    delytelseId = eksternFagsakId.toString() + (it.periodeId ?: ""),
                ),
            )
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
            VedtaksperiodeOvergangsstønadDto(
                it.periode.fomDato,
                it.periode.tomDato,
                AktivitetType.valueOf(it.aktivitet.name),
                VedtaksperiodeType.valueOf(it.periodeType.name),
            )
        }
    }

    private fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerBarnetilsyn): List<VedtaksperiodeBarnetilsynDto> {
        return vedtaksdetaljer.vedtaksperioder.map {
            VedtaksperiodeBarnetilsynDto(
                it.periode.fomDato,
                it.periode.tomDato,
                it.utgifter,
                it.antallBarn,
            )
        }
    }

    fun mapTilVedtakSkolepengeDVH(iverksett: IverksettSkolepenger, forrigeBehandlingId: Long?): VedtakSkolepenger {
        return VedtakSkolepenger(
            fagsakId = iverksett.fagsak.eksternId,
            behandlingId = iverksett.behandling.eksternId,
            relatertBehandlingId = forrigeBehandlingId,
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
            utbetalinger = iverksett.vedtak.tilkjentYtelse?.let {
                mapTilUtbetaling(
                    it,
                    iverksett.fagsak.stønadstype,
                    iverksett.fagsak.eksternId,
                    iverksett.søker,
                )
            } ?: emptyList(),
            funksjonellId = iverksett.behandling.eksternId,
            stønadstype = StønadTypeEkstern.valueOf(iverksett.fagsak.stønadstype.name),
            vedtaksperioder = iverksett.vedtak.vedtaksperioder.map {
                VedtaksperiodeSkolepenger(
                    skoleår = it.perioder.first().periode.fomDato.utledSkoleår().value,
                    perioder = it.perioder.map { delårsperiode -> mapTilDelårsperiode(delårsperiode) },
                    utgifter = it.utgiftsperioder.map { utgiftsperiode -> mapTilUtgiftSkolepenger(utgiftsperiode) },
                    maksSatsForSkoleår = it.perioder.first().makssatsForSkoleår,
                )
            },
            vedtaksbegrunnelse = iverksett.vedtak.begrunnelse,
            kravMottatt = iverksett.behandling.kravMottatt,
            årsakRevurdering = mapÅrsakRevurdering(iverksett.behandling),
            avslagÅrsak = iverksett.vedtak.avslagÅrsak?.name,
        )
    }

    private fun mapTilUtgiftSkolepenger(utgiftsperiode: SkolepengerUtgift) =
        UtgiftSkolepenger(
            utgiftsdato = utgiftsperiode.utgiftsdato,
            utbetaltBeløp = utgiftsperiode.stønad,
        )

    private fun mapTilDelårsperiode(delårsperiode: DelårsperiodeSkoleårSkolepenger) =
        Delårsperiode(
            studietype = Studietype.valueOf(delårsperiode.studietype.name),
            datoFra = delårsperiode.periode.fomDato,
            datoTil = delårsperiode.periode.tomDato,
            studiebelastning = delårsperiode.studiebelastning,
        )

    private fun LocalDate.utledSkoleår() = if (this.month > Month.JUNE) Year.of(this.year) else Year.of(this.year - 1)
}
