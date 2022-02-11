package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TekniskOpphør
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.DelvilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.FagsakdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.SøkerDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.ef.iverksett.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.VurderingDto
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun opprettIverksettDto(behandlingId: UUID,
                        behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
                        andelsbeløp: Int = 5000,
                        stønadType: StønadType = StønadType.OVERGANGSSTØNAD): IverksettDto {

    val andelTilkjentYtelse = lagAndelTilkjentYtelseDto(
            beløp = andelsbeløp,
            periodetype = Periodetype.MÅNED,
            fraOgMed = LocalDate.of(2021, 1, 1),
            tilOgMed = LocalDate.of(2021, 12, 31),
            kildeBehandlingId = UUID.randomUUID()
    )
    val tilkjentYtelse = TilkjentYtelseDto(
            andelerTilkjentYtelse = listOf(andelTilkjentYtelse)
    )

    return IverksettDto(
            fagsak = FagsakdetaljerDto(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = stønadType),
            behandling = BehandlingsdetaljerDto(
                    behandlingId = behandlingId,
                    forrigeBehandlingId = null,
                    eksternId = 9L,
                    behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                    behandlingÅrsak = behandlingÅrsak,
                    vilkårsvurderinger = listOf(
                            VilkårsvurderingDto(
                                    vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
                                    resultat = Vilkårsresultat.OPPFYLT,
                                    delvilkårsvurderinger = listOf(
                                            DelvilkårsvurderingDto(
                                                    resultat = Vilkårsresultat.OPPFYLT,
                                                    vurderinger = listOf(
                                                            VurderingDto(
                                                                    regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                                                                    svar = SvarId.JA,
                                                                    begrunnelse = "Nei")
                                                    )
                                            )
                                    )
                            )
                    )
            ),
            søker = SøkerDto(
                    personIdent = "12345678910",
                    barn = emptyList(),
                    tilhørendeEnhet = "4489",
                    adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT
            ),
            vedtak = VedtaksdetaljerDto(
                    resultat = Vedtaksresultat.INNVILGET,
                    vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                    opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                    saksbehandlerId = "A12345",
                    beslutterId = "B23456",
                    tilkjentYtelse = tilkjentYtelse,
                    vedtaksperioder = emptyList()
            )
    )
}

fun opprettAndelTilkjentYtelse() = lagAndelTilkjentYtelse(
        beløp = 5000,
        periodetype = Periodetype.MÅNED,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        inntekt = 100,
        samordningsfradrag = 2,
        inntektsreduksjon = 5
)

fun opprettTilkjentYtelseMedMetadata(behandlingId: UUID,
                                     eksternId: Long,
                                     tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId))
        : TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
            tilkjentYtelse = tilkjentYtelse,
            saksbehandlerId = "saksbehandlerId",
            eksternBehandlingId = eksternId,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = 0,
            personIdent = "12345678910",
            behandlingId = behandlingId,
            vedtaksdato = LocalDate.of(2021, 1, 1))

}

fun opprettTekniskOpphør(behandlingId: UUID, eksternId: Long): TekniskOpphør {
    return TekniskOpphør(behandlingId, opprettTilkjentYtelseMedMetadata(behandlingId, eksternId))
}

fun opprettIverksett(behandlingId: UUID,
                     forrigeBehandlingId: UUID? = null,
                     andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
                     tilbakekreving: Tilbakekrevingsdetaljer? = null): Iverksett {

    val tilkjentYtelse = TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag = null,
            status = TilkjentYtelseStatus.AKTIV,
            andelerTilkjentYtelse = andeler,
            opphørsdato = null
    )

    val behandlingType = forrigeBehandlingId?.let { BehandlingType.REVURDERING } ?: BehandlingType.FØRSTEGANGSBEHANDLING
    return Iverksett(
            fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
            behandling = Behandlingsdetaljer(
                    behandlingId = behandlingId,
                    forrigeBehandlingId = forrigeBehandlingId,
                    eksternId = 9L,
                    behandlingType = behandlingType,
                    behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                    relatertBehandlingId = null,
                    vilkårsvurderinger = listOf(
                            Vilkårsvurdering(
                                    vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
                                    resultat = Vilkårsresultat.OPPFYLT,
                                    delvilkårsvurderinger = listOf(
                                            Delvilkårsvurdering(
                                                    resultat = Vilkårsresultat.OPPFYLT,
                                                    vurderinger = listOf(
                                                            Vurdering(
                                                                    regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                                                                    svar = SvarId.JA,
                                                                    begrunnelse = "Nei")
                                                    )
                                            )
                                    )
                            )
                    )

            ),
            søker = Søker(
                    personIdent = "12345678910",
                    barn = emptyList(),
                    tilhørendeEnhet = "4489",
                    adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT
            ),
            vedtak = Vedtaksdetaljer(
                    vedtaksresultat = Vedtaksresultat.INNVILGET,
                    vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                    opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                    saksbehandlerId = "A12345",
                    beslutterId = "B23456",
                    tilkjentYtelse = tilkjentYtelse,
                    vedtaksperioder = listOf(Vedtaksperiode(fraOgMed = LocalDate.now(),
                                                            tilOgMed = LocalDate.now(),
                                                            aktivitet = AktivitetType.BARNET_ER_SYKT,
                                                            periodeType = VedtaksperiodeType.HOVEDPERIODE)),
                    tilbakekreving = tilbakekreving
            )
    )
}

fun opprettBrev(): Brev {
    return Brev(UUID.fromString("234bed7c-b1d3-11eb-8529-0242ac130003"), ByteArray(256))
}

fun opprettTilkjentYtelse(behandlingId: UUID,
                          andeler: List<AndelTilkjentYtelse> = listOf(opprettTilkjentYtelse())): TilkjentYtelse {
    return TilkjentYtelse(
            id = behandlingId,
            utbetalingsoppdrag = null,
            andelerTilkjentYtelse = andeler,
            opphørsdato = null
    )
}

private fun opprettTilkjentYtelse() = lagAndelTilkjentYtelse(
        beløp = 100,
        Periodetype.MÅNED,
        fraOgMed = LocalDate.parse("2021-01-01"),
        tilOgMed = LocalDate.parse("2021-12-31"),
        periodeId = 1L,
        forrigePeriodeId = 1L,
        inntektsreduksjon = 5,
        samordningsfradrag = 2,
        inntekt = 100
)

fun opprettFrittståendeBrevDto(): FrittståendeBrevDto {
    return FrittståendeBrevDto(
            personIdent = "12345678910",
            eksternFagsakId = 1,
            brevtype = FrittståendeBrevType.INFOBREV_OVERGANGSSTØNAD,
            fil = "fil.pdf".toByteArray(),
            journalførendeEnhet = "4489",
            saksbehandlerIdent = "saksbehandlerIdent"
    )
}

fun opprettTilbakekrevingsdetaljer(): Tilbakekrevingsdetaljer =
        Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel()
        )

fun opprettTilbakekrevingMedVarsel(
        sumFeilutbetaling: BigDecimal = BigDecimal.valueOf(100),
        perioder: List<Periode> = listOf(Periode(
                fom = LocalDate.of(2021, 5, 1),
                tom = LocalDate.of(2021, 6, 30)
        ))) = TilbakekrevingMedVarsel(
        varseltekst = "varseltekst",
        sumFeilutbetaling = sumFeilutbetaling,
        perioder = perioder
)

class IverksettResultatMockBuilder private constructor(

        val tilkjentYtelse: TilkjentYtelse,
        val oppdragResultat: OppdragResultat,
        val journalpostResultat: JournalpostResultat,
        val vedtaksbrevResultat: DistribuerVedtaksbrevResultat
) {

    data class Builder(
            var oppdragResultat: OppdragResultat? = null,
            var journalpostResultat: JournalpostResultat? = null,
            var vedtaksbrevResultat: DistribuerVedtaksbrevResultat? = null,
            var tilbakekrevingResultat: TilbakekrevingResultat? = null
    ) {

        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }
        fun journalPostResultat() = apply { this.journalpostResultat = JournalpostResultat(UUID.randomUUID().toString()) }
        fun vedtaksbrevResultat(behandlingId: UUID) =
                apply { this.vedtaksbrevResultat = DistribuerVedtaksbrevResultat(bestillingId = behandlingId.toString()) }

        fun tilbakekrevingResultat(tilbakekrevingResultat: TilbakekrevingResultat?) =
                apply { this.tilbakekrevingResultat = tilbakekrevingResultat }

        fun build(behandlingId: UUID, tilkjentYtelse: TilkjentYtelse?) =
                IverksettResultat(behandlingId,
                                  tilkjentYtelse,
                                  oppdragResultat,
                                  journalpostResultat,
                                  vedtaksbrevResultat,
                                  tilbakekrevingResultat)
    }
}

