package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.brev.domain.Brevmottaker
import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.DelårsperiodeSkoleårSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.OppgaverForOpprettelse
import no.nav.familie.ef.iverksett.iverksetting.domene.PeriodeMedBeløp
import no.nav.familie.ef.iverksett.iverksetting.domene.SkolepengerUtgift
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.ÅrsakRevurdering
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.DelvilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.FagsakdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettSkolepengerDto
import no.nav.familie.kontrakter.ef.iverksett.OppgaverForOpprettelseDto
import no.nav.familie.kontrakter.ef.iverksett.PeriodeMedBeløpDto
import no.nav.familie.kontrakter.ef.iverksett.SøkerDto
import no.nav.familie.kontrakter.ef.iverksett.TilbakekrevingDto
import no.nav.familie.kontrakter.ef.iverksett.TilbakekrevingMedVarselDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerSkolepengerDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeSkolepengerDto
import no.nav.familie.kontrakter.ef.iverksett.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.VurderingDto
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker as BrevmottakerKontrakter

fun VurderingDto.toDomain(): Vurdering = Vurdering(this.regelId, this.svar, this.begrunnelse)

fun DelvilkårsvurderingDto.toDomain(): Delvilkårsvurdering = Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() })

fun VilkårsvurderingDto.toDomain(): Vilkårsvurdering = Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() })

fun FagsakdetaljerDto.toDomain(): Fagsakdetaljer =
    Fagsakdetaljer(
        fagsakId = this.fagsakId,
        eksternId = this.eksternId,
        stønadstype = this.stønadstype,
    )

fun SøkerDto.toDomain(): Søker =
    Søker(
        personIdent = this.personIdent,
        barn = this.barn.map { it.toDomain() },
        tilhørendeEnhet = this.tilhørendeEnhet,
        adressebeskyttelse = this.adressebeskyttelse,
    )

fun BehandlingsdetaljerDto.toDomain(): Behandlingsdetaljer =
    Behandlingsdetaljer(
        behandlingId = this.behandlingId,
        forrigeBehandlingId = this.forrigeBehandlingId,
        eksternId = this.eksternId,
        behandlingType = this.behandlingType,
        behandlingÅrsak = this.behandlingÅrsak,
        vilkårsvurderinger = this.vilkårsvurderinger.map { it.toDomain() },
        aktivitetspliktInntrefferDato = this.aktivitetspliktInntrefferDato,
        kravMottatt = this.kravMottatt,
        årsakRevurdering = this.årsakRevurdering?.let { ÅrsakRevurdering(it.opplysningskilde, it.årsak) },
        kategori = this.kategori,
    )

fun VedtaksperiodeOvergangsstønadDto.toDomain(): VedtaksperiodeOvergangsstønad =
    VedtaksperiodeOvergangsstønad(
        aktivitet = this.aktivitet,
        periode = this.periode,
        periodeType = this.periodeType,
    )

fun VedtaksperiodeBarnetilsynDto.toDomain(): VedtaksperiodeBarnetilsyn =
    VedtaksperiodeBarnetilsyn(
        periode = this.periode,
        utgifter = this.utgifter,
        antallBarn = this.antallBarn,
    )

fun VedtaksperiodeSkolepengerDto.toDomain(): VedtaksperiodeSkolepenger =
    VedtaksperiodeSkolepenger(
        perioder =
            this.perioder.map {
                DelårsperiodeSkoleårSkolepenger(
                    studietype = it.studietype,
                    periode = it.periode,
                    studiebelastning = it.studiebelastning,
                    makssatsForSkoleår = it.maksSatsForSkoleår,
                )
            },
        utgiftsperioder =
            this.utgiftsperioder.map {
                SkolepengerUtgift(
                    utgiftsdato = it.utgiftsdato,
                    stønad = it.stønad,
                )
            },
    )

fun VedtaksdetaljerOvergangsstønadDto.toDomain(): VedtaksdetaljerOvergangsstønad =
    VedtaksdetaljerOvergangsstønad(
        vedtaksresultat = this.resultat,
        vedtakstidspunkt = this.vedtakstidspunkt,
        opphørÅrsak = this.opphørÅrsak,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.tilkjentYtelse?.toDomain(),
        vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
        tilbakekreving = this.tilbakekreving?.toDomain(),
        brevmottakere = this.brevmottakere.toDomain(),
        avslagÅrsak = this.avslagÅrsak,
        oppgaverForOpprettelse = this.oppgaverForOpprettelse.toDomain(),
        grunnbeløp = this.grunnbeløp,
    )

fun VedtaksdetaljerBarnetilsynDto.toDomain(): VedtaksdetaljerBarnetilsyn =
    VedtaksdetaljerBarnetilsyn(
        vedtaksresultat = this.resultat,
        vedtakstidspunkt = this.vedtakstidspunkt,
        opphørÅrsak = this.opphørÅrsak,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.tilkjentYtelse?.toDomain(),
        vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
        tilbakekreving = this.tilbakekreving?.toDomain(),
        brevmottakere = this.brevmottakere.toDomain(),
        kontantstøtte = this.kontantstøtte.map { it.toDomain() },
        tilleggsstønad = this.tilleggsstønad.map { it.toDomain() },
        oppgaverForOpprettelse = this.oppgaverForOpprettelse.toDomain(),
        avslagÅrsak = this.avslagÅrsak,
    )

fun VedtaksdetaljerSkolepengerDto.toDomain(): VedtaksdetaljerSkolepenger =
    VedtaksdetaljerSkolepenger(
        vedtaksresultat = this.resultat,
        vedtakstidspunkt = this.vedtakstidspunkt,
        opphørÅrsak = this.opphørÅrsak,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.tilkjentYtelse?.toDomain(),
        vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
        tilbakekreving = this.tilbakekreving?.toDomain(),
        brevmottakere = this.brevmottakere.toDomain(),
        begrunnelse = this.begrunnelse,
        avslagÅrsak = this.avslagÅrsak,
    )

fun TilbakekrevingDto.toDomain(): Tilbakekrevingsdetaljer =
    Tilbakekrevingsdetaljer(
        tilbakekrevingsvalg = this.tilbakekrevingsvalg,
        this.tilbakekrevingMedVarsel?.toDomain(),
        this.begrunnelseForTilbakekreving,
    )

fun TilbakekrevingMedVarselDto.toDomain(): TilbakekrevingMedVarsel =
    TilbakekrevingMedVarsel(
        varseltekst = this.varseltekst,
        sumFeilutbetaling = this.sumFeilutbetaling,
        perioder = this.fellesperioder.map { it.toDatoperiode() },
    )

fun List<BrevmottakerKontrakter>.toDomain(): Brevmottakere = Brevmottakere(mottakere = this.map { it.toDomain() })

fun BrevmottakerKontrakter.toDomain(): Brevmottaker =
    Brevmottaker(
        ident = this.ident,
        navn = this.navn,
        identType = this.identType,
        mottakerRolle = this.mottakerRolle,
    )

fun PeriodeMedBeløpDto.toDomain(): PeriodeMedBeløp =
    PeriodeMedBeløp(
        periode = this.periode,
        beløp = this.beløp,
    )

fun IverksettDto.toDomain(): IverksettData =
    when (this) {
        is IverksettOvergangsstønadDto ->
            IverksettOvergangsstønad(
                fagsak = this.fagsak.toDomain(),
                søker = this.søker.toDomain(),
                behandling = this.behandling.toDomain(),
                vedtak = this.vedtak.toDomain(),
            )

        is IverksettBarnetilsynDto ->
            IverksettBarnetilsyn(
                fagsak = this.fagsak.toDomain(),
                søker = this.søker.toDomain(),
                behandling = this.behandling.toDomain(),
                vedtak = this.vedtak.toDomain(),
            )

        is IverksettSkolepengerDto ->
            IverksettSkolepenger(
                fagsak = this.fagsak.toDomain(),
                søker = this.søker.toDomain(),
                behandling = this.behandling.toDomain(),
                vedtak = this.vedtak.toDomain(),
            )

        else -> error("Støtter ikke mapping for ${this.javaClass.simpleName}")
    }

fun OppgaverForOpprettelseDto.toDomain(): OppgaverForOpprettelse = OppgaverForOpprettelse(oppgavetyper = this.oppgavetyper, årForInntektskontrollSelvstendigNæringsdrivende = this.årForInntektskontrollSelvstendigNæringsdrivende)
