package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksperiode
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class BehandlingDVHMapperTest {

    val fagsakId = UUID.randomUUID()
    val behandlingId = UUID.randomUUID()
    val eksternFagsakId = 1L
    val eksternBehandlingId = 13L
    val søker = "01010172272"
    val barnFnr = "24101576627"
    val termindato = LocalDate.now().plusDays(40)


    @Test
    internal fun `skal mappe iverksett til dvh verdier`() {

        val behandlingDVH = BehandlingDVHMapper.map(
                Iverksett(fagsak = Fagsakdetaljer(fagsakId = fagsakId,
                                                  eksternId = eksternFagsakId,
                                                  stønadstype = StønadType.OVERGANGSSTØNAD),
                          behandling = Behandlingsdetaljer(forrigeBehandlingId = null,
                                                           behandlingId = behandlingId,
                                                           eksternId = eksternBehandlingId,
                                                           behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                                                           behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                                                           relatertBehandlingId = null,
                                                           vilkårsvurderinger = lagVilkårsvurderinger(),
                                                           aktivitetspliktInntrefferDato = null),
                          søker = Søker(personIdent = søker,
                                        barn = listOf(
                                                Barn(personIdent = barnFnr),
                                                Barn(termindato = termindato),
                                        ),
                                        tilhørendeEnhet = "4489",
                                        adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG),
                          vedtak = Vedtaksdetaljer(vedtaksresultat = Vedtaksresultat.INNVILGET,
                                                   vedtaksdato = LocalDate.now(),
                                                   opphørÅrsak = null,
                                                   saksbehandlerId = "A123456",
                                                   beslutterId = "B123456",
                                                   tilkjentYtelse = TilkjentYtelse(
                                                           id = UUID.randomUUID(),
                                                           utbetalingsoppdrag = null,
                                                           status = TilkjentYtelseStatus.OPPRETTET,
                                                           andelerTilkjentYtelse = listOf(
                                                                   AndelTilkjentYtelse(beløp = 9000,
                                                                                       fraOgMed = LocalDate.of(2021, 1, 1),
                                                                                       tilOgMed = LocalDate.of(2021, 5, 31),
                                                                                       periodetype = Periodetype.MÅNED,
                                                                                       inntekt = 300000,
                                                                                       samordningsfradrag = 1000,
                                                                                       inntektsreduksjon = 11000,
                                                                                       periodeId = 1,
                                                                                       forrigePeriodeId = null,
                                                                                       kildeBehandlingId = behandlingId

                                                                   ),
                                                                   AndelTilkjentYtelse(beløp = 10000,
                                                                                       fraOgMed = LocalDate.of(2021, 6, 1),
                                                                                       tilOgMed = LocalDate.of(2021, 10, 31),
                                                                                       periodetype = Periodetype.MÅNED,
                                                                                       inntekt = 300000,
                                                                                       samordningsfradrag = 0,
                                                                                       inntektsreduksjon = 11000,
                                                                                       periodeId = 2,
                                                                                       forrigePeriodeId = 1,
                                                                                       kildeBehandlingId = behandlingId)
                                                           )),
                                                   vedtaksperioder = listOf(
                                                           Vedtaksperiode(aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                                                                          fraOgMed = LocalDate.of(2021, 1, 1),
                                                                          periodeType = VedtaksperiodeType.PERIODE_FØR_FØDSEL,
                                                                          tilOgMed = LocalDate.of(2021, 1, 1)),
                                                           Vedtaksperiode(aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                                                                          fraOgMed = LocalDate.of(2021, 6, 1),
                                                                          periodeType = VedtaksperiodeType.HOVEDPERIODE,
                                                                          tilOgMed = LocalDate.of(2021, 10, 31))
                                                   ))

                ),
                TilkjentYtelse(andelerTilkjentYtelse = listOf(AndelTilkjentYtelse(beløp = 8000,
                                                                                  fraOgMed = LocalDate.of(2021, 1, 1),
                                                                                  tilOgMed = LocalDate.of(2021, 5, 31),
                                                                                  periodetype = Periodetype.MÅNED,
                                                                                  inntekt = 400000,
                                                                                  samordningsfradrag = 1000,
                                                                                  inntektsreduksjon = 11000,
                                                                                  periodeId = 1,
                                                                                  forrigePeriodeId = null,
                                                                                  kildeBehandlingId = behandlingId
                ),
                                                              AndelTilkjentYtelse(beløp = 30000,
                                                                                  fraOgMed = LocalDate.of(2021, 6, 1),
                                                                                  tilOgMed = LocalDate.of(2021, 10, 31),
                                                                                  periodetype = Periodetype.MÅNED,
                                                                                  inntekt = 400000,
                                                                                  samordningsfradrag = 0,
                                                                                  inntektsreduksjon = 11000,
                                                                                  periodeId = 2,
                                                                                  forrigePeriodeId = 1,
                                                                                  kildeBehandlingId = behandlingId)))
        )

        assertThat(behandlingDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(behandlingDVH.fagsakId).isEqualTo(fagsakId.toString())
        assertThat(behandlingDVH.behandlingId).isEqualTo(behandlingId.toString())
        assertThat(behandlingDVH.funksjonellId).isEqualTo(eksternBehandlingId.toString())
        assertThat(behandlingDVH.vedtaksperioder).hasSize(2)
        assertThat(behandlingDVH.utbetalinger).hasSize(2)
        assertThat(behandlingDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
    }

    private fun lagVilkårsvurderinger(): List<Vilkårsvurdering> = listOf(
            Vilkårsvurdering(vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.MEDLEMSKAP_UNNTAK,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.LOVLIG_OPPHOLD,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.OPPHOLD_UNNTAK,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.MOR_ELLER_FAR,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.SIVILSTAND,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.SAMLIV,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.LEVER_IKKE_I_EKTESKAPLIGNENDE_FORHOLD,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.LEVER_IKKE_MED_ANNEN_FORELDER,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null)))
                             )),

            Vilkårsvurdering(vilkårType = VilkårType.ALENEOMSORG,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.SKRIFTLIG_AVTALE_OM_DELT_BOSTED,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null))),
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
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.HAR_FÅTT_ELLER_VENTER_NYTT_BARN_MED_SAMME_PARTNER,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null)))
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
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.FYLLER_BRUKER_AKTIVITETSPLIKT,
                                                                                        svar = SvarId.JA,
                                                                                        begrunnelse = null)))
                             )),
            Vilkårsvurdering(vilkårType = VilkårType.TIDLIGERE_VEDTAKSPERIODER,
                             resultat = Vilkårsresultat.OPPFYLT,
                             delvilkårsvurderinger = listOf(
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null))),
                                     Delvilkårsvurdering(resultat = Vilkårsresultat.OPPFYLT,
                                                         vurderinger = listOf(Vurdering(regelId = RegelId.HAR_TIDLIGERE_MOTTATT_OVERGANSSTØNAD,
                                                                                        svar = SvarId.NEI,
                                                                                        begrunnelse = null)))
                             )),

            )
}