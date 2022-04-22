package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.PeriodeMedBeløp
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakstatistikkMapperTest {

    val fagsakId: UUID = UUID.randomUUID()
    val behandlingId: UUID = UUID.randomUUID()
    val eksternFagsakId = 1L
    private val eksternBehandlingId = 13L
    private val søker = "01010172272"
    private val barnFnr = "24101576627"
    private val termindato: LocalDate? = LocalDate.now().plusDays(40)

    @Test
    internal fun `skal mappe iverksett til VedtakOvergangsstønadDVH`() {

        val vedtakOvergangsstønadDVH = VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
                IverksettOvergangsstønad(
                        fagsak = fagsakdetaljer(),
                        behandling = behandlingsdetaljer(),
                        søker = Søker(personIdent = søker,
                                      barn = listOf(
                                              Barn(personIdent = barnFnr),
                                              Barn(termindato = termindato),
                                      ),
                                      tilhørendeEnhet = "4489",
                                      adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                        vedtak = vedtaksdetaljerOvergangsstønad()
                ), null)
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(vedtakOvergangsstønadDVH.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakOvergangsstønadDVH.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
    }

    @Test
    internal fun `skal mappe iverksett til VedtakBarnetilsynDVH`() {

        val vedtakBarnetilsynDVH = VedtakstatistikkMapper.mapTilVedtakBarnetilsynDVH(
                IverksettBarnetilsyn(
                        fagsak = fagsakdetaljer(),
                        behandling = behandlingsdetaljer(),
                        søker = Søker(personIdent = søker,
                                      barn = listOf(
                                              Barn(personIdent = barnFnr),
                                              Barn(termindato = termindato),
                                      ),
                                      tilhørendeEnhet = "4489",
                                      adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                        vedtak = vedtaksdetaljerBarnetilsyn()
                ), null)

        assertThat(vedtakBarnetilsynDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(vedtakBarnetilsynDVH.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakBarnetilsynDVH.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakBarnetilsynDVH.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().fraOgMed).isEqualTo(LocalDate.of(2021,1,1))
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().tilOgMed).isEqualTo(LocalDate.of(2021,1,1))
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().utgifter).isEqualTo(1000)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().antallBarn).isEqualTo(1)
        assertThat(vedtakBarnetilsynDVH.utbetalinger).hasSize(2)
        assertThat(vedtakBarnetilsynDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte).hasSize(1)
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().fraOgMed).isEqualTo(LocalDate.of(2021, 5, 1))
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().tilOgMed).isEqualTo(LocalDate.of(2021, 7, 1))
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().beløp).isEqualTo(1000)
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad).hasSize(1)
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().fraOgMed).isEqualTo(LocalDate.of(2021, 6, 1))
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().tilOgMed).isEqualTo(LocalDate.of(2021, 8, 1))
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().beløp).isEqualTo(2000)
    }


    fun fagsakdetaljer(): Fagsakdetaljer =
            Fagsakdetaljer(fagsakId = fagsakId,
                           eksternId = eksternFagsakId,
                           stønadstype = StønadType.OVERGANGSSTØNAD)

    fun behandlingsdetaljer(): Behandlingsdetaljer =
            Behandlingsdetaljer(forrigeBehandlingId = null,
                                behandlingId = behandlingId,
                                eksternId = eksternBehandlingId,
                                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                                behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                                relatertBehandlingId = null,
                                vilkårsvurderinger = lagVilkårsvurderinger(),
                                aktivitetspliktInntrefferDato = null)

    fun tilkjentYtelse(): TilkjentYtelse =
            TilkjentYtelse(
                    id = UUID.randomUUID(),
                    utbetalingsoppdrag = null,
                    status = TilkjentYtelseStatus.OPPRETTET,
                    andelerTilkjentYtelse = listOf(
                            AndelTilkjentYtelse(beløp = 9000,
                                                fraOgMed = LocalDate.of(2021,
                                                                        1,
                                                                        1),
                                                tilOgMed = LocalDate.of(2021,
                                                                        5,
                                                                        31),
                                                periodetype = Periodetype.MÅNED,
                                                inntekt = 300000,
                                                samordningsfradrag = 1000,
                                                inntektsreduksjon = 11000,
                                                periodeId = 1,
                                                forrigePeriodeId = null,
                                                kildeBehandlingId = behandlingId

                            ),
                            AndelTilkjentYtelse(beløp = 10000,
                                                fraOgMed = LocalDate.of(2021,
                                                                        6,
                                                                        1),
                                                tilOgMed = LocalDate.of(2021,
                                                                        10,
                                                                        31),
                                                periodetype = Periodetype.MÅNED,
                                                inntekt = 300000,
                                                samordningsfradrag = 0,
                                                inntektsreduksjon = 11000,
                                                periodeId = 2,
                                                forrigePeriodeId = 1,
                                                kildeBehandlingId = behandlingId)
                    ), startdato = LocalDate.now())

    fun vedtaksdetaljerBarnetilsyn() = VedtaksdetaljerBarnetilsyn(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now(),
            opphørÅrsak = null,
            saksbehandlerId = "A123456",
            beslutterId = "B123456",
            tilkjentYtelse = tilkjentYtelse(),
            vedtaksperioder = listOf(
                    VedtaksperiodeBarnetilsyn(fraOgMed = LocalDate.of(2021, 1, 1),
                                              tilOgMed = LocalDate.of(2021, 1, 1),
                                              utgifter = 1000,
                                              antallBarn = 1),
                    VedtaksperiodeBarnetilsyn(fraOgMed = LocalDate.of(2021, 6, 1),
                                              tilOgMed = LocalDate.of(2021, 10, 31),
                                              utgifter = 2000,
                                              antallBarn = 2)
            ),
            brevmottakere = Brevmottakere(emptyList()),
            kontantstøtte = listOf(PeriodeMedBeløp(LocalDate.of(2021, 5, 1), LocalDate.of(2021, 7, 1), 1000)),
            tilleggsstønad = listOf(PeriodeMedBeløp(LocalDate.of(2021, 6, 1), LocalDate.of(2021, 8, 1), 2000)),
    )

    fun vedtaksdetaljerOvergangsstønad() =
            VedtaksdetaljerOvergangsstønad(
                    vedtaksresultat = Vedtaksresultat.INNVILGET,
                    vedtakstidspunkt = LocalDateTime.now(),
                    opphørÅrsak = null,
                    saksbehandlerId = "A123456",
                    beslutterId = "B123456",
                    tilkjentYtelse = tilkjentYtelse(),
                    vedtaksperioder = listOf(
                            VedtaksperiodeOvergangsstønad(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                                                          fraOgMed = LocalDate.of(2021, 1, 1),
                                                          periodeType = VedtaksperiodeType.PERIODE_FØR_FØDSEL,
                                                          tilOgMed = LocalDate.of(2021, 1, 1)),
                            VedtaksperiodeOvergangsstønad(aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                                                          fraOgMed = LocalDate.of(2021, 6, 1),
                                                          periodeType = VedtaksperiodeType.HOVEDPERIODE,
                                                          tilOgMed = LocalDate.of(2021, 10, 31))
                    ),
                    brevmottakere = Brevmottakere(emptyList()))


    private fun lagVilkårsvurderinger(): List<Vilkårsvurdering> = listOf(
            Vilkårsvurdering(vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                                                                                        SvarId.JA,
                                                                                        null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.MEDLEMSKAP_UNNTAK,
                                                                                        SvarId.NEI,
                                                                                        null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.LOVLIG_OPPHOLD,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                                                                                        SvarId.JA,
                                                                                        null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.OPPHOLD_UNNTAK,
                                                                                        SvarId.NEI,
                                                                                        null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.MOR_ELLER_FAR,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN,
                                                                          SvarId.JA,
                                                                          null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.SIVILSTAND,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE,
                                                                          SvarId.JA,
                                                                          null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.SAMLIV,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.LEVER_IKKE_I_EKTESKAPLIGNENDE_FORHOLD,
                                                                          SvarId.JA,
                                                                          null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.LEVER_IKKE_MED_ANNEN_FORELDER,
                                                                          SvarId.JA,
                                                                          null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.ALENEOMSORG,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.SKRIFTLIG_AVTALE_OM_DELT_BOSTED,
                                                                                        SvarId.NEI,
                                                                                        null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.NÆRE_BOFORHOLD,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.MER_AV_DAGLIG_OMSORG,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null))),
                             )),
            Vilkårsvurdering(vilkårType = VilkårType.ALENEOMSORG,
                             resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
                                                         vurderinger = listOf()))),

            Vilkårsvurdering(vilkårType = VilkårType.NYTT_BARN_SAMME_PARTNER,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(
                                                                 RegelId.HAR_FÅTT_ELLER_VENTER_NYTT_BARN_MED_SAMME_PARTNER,
                                                                 SvarId.NEI,
                                                                 null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.AKTIVITET,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(RegelId.FYLLER_BRUKER_AKTIVITETSPLIKT,
                                                                                        SvarId.JA,
                                                                                        null)))
                             )),
            Vilkårsvurdering(vilkårType = VilkårType.TIDLIGERE_VEDTAKSPERIODER,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING,
                                                                          SvarId.NEI,
                                                                          null))),
                                     Delvilkårsvurdering(Vilkårsresultat.OPPFYLT,
                                                         listOf(Vurdering(RegelId.HAR_TIDLIGERE_MOTTATT_OVERGANSSTØNAD,
                                                                          SvarId.NEI,
                                                                          null)))
                             )),

            )
}