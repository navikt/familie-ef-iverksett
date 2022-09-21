package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.DelårsperiodeSkoleårSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.PeriodeMedBeløp
import no.nav.familie.ef.iverksett.iverksetting.domene.SkolepengerUtgift
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.eksterne.kontrakter.ef.AktivitetsvilkårBarnetilsyn
import no.nav.familie.eksterne.kontrakter.ef.Studietype
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.SkolepengerStudietype
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.Månedsperiode
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat as VilkårsresultatEksterneKontrakter

internal class VedtakstatistikkMapperTest {

    private val fagsakId: UUID = UUID.randomUUID()
    private val behandlingId: UUID = UUID.randomUUID()
    private val forrigeBehandlingEksternId = 11L
    private val eksternFagsakId = 12L
    private val eksternBehandlingId = 13L
    private val vedtakstidspunkt = LocalDateTime.now()
    private val søker = "01010172272"
    private val barnFnr = "24101576627"
    private val termindato: LocalDate? = LocalDate.now().plusDays(40)

    @Test
    internal fun `skal mappe iverksett til VedtakOvergangsstønadDVH - sjekk alle felter`() {
        val vedtakOvergangsstønadDVH = VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
            IverksettOvergangsstønad(
                fagsak = fagsakdetaljer(),
                behandling = behandlingsdetaljer(),
                søker = Søker(
                    personIdent = søker,
                    barn = listOf(
                        Barn(personIdent = barnFnr, termindato = termindato),
                        Barn(termindato = termindato)
                    ),
                    tilhørendeEnhet = "4489",
                    adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG
                ),
                vedtak = vedtaksdetaljerOvergangsstønad()
            ),
            forrigeBehandlingEksternId
        )
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
        assertThat(vedtakOvergangsstønadDVH.barn).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.barn.first().personIdent).isEqualTo(barnFnr)
        assertThat(vedtakOvergangsstønadDVH.barn.first().termindato).isEqualTo(termindato)
        assertThat(vedtakOvergangsstønadDVH.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.behandlingType.name).isEqualTo(BehandlingType.REVURDERING.name)
        assertThat(vedtakOvergangsstønadDVH.behandlingÅrsak.name).isEqualTo(BehandlingÅrsak.SØKNAD.name)
        assertThat(vedtakOvergangsstønadDVH.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakOvergangsstønadDVH.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.person.personIdent).isEqualTo(søker)
        assertThat(vedtakOvergangsstønadDVH.relatertBehandlingId).isEqualTo(forrigeBehandlingEksternId)
        assertThat(vedtakOvergangsstønadDVH.stønadstype.name).isEqualTo(StønadType.OVERGANGSSTØNAD.name)
        assertThat(vedtakOvergangsstønadDVH.tidspunktVedtak).isEqualTo(vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().fraOgMed).isEqualTo(LocalDate.of(2021, 1, 1))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().tilOgMed).isEqualTo(LocalDate.of(2021, 5, 31))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().inntekt).isEqualTo(300000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().inntektsreduksjon).isEqualTo(11000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().samordningsfradrag).isEqualTo(1000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().beløp).isEqualTo(9000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.delytelseId).isEqualTo("121")
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.klassekode).isEqualTo("EFOG")
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.gjelderPerson.personIdent).isEqualTo(søker)
        assertThat(vedtakOvergangsstønadDVH.vedtak).isEqualTo(Vedtak.INNVILGET)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().fraOgMed).isEqualTo(LocalDate.of(2021, 2, 1))
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().tilOgMed).isEqualTo(LocalDate.of(2021, 3, 31))
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().aktivitet.name).isEqualTo(AktivitetType.IKKE_AKTIVITETSPLIKT.name)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().periodeType.name).isEqualTo(VedtaksperiodeType.PERIODE_FØR_FØDSEL.name)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger).hasSize(12)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.FORUTGÅENDE_MEDLEMSKAP.name)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger.first().resultat.name).isEqualTo(Vilkårsresultat.OPPFYLT.name)
    }

    @Test
    internal fun `Map iverksett til VedtakBarnetilsynDVH - sjekk alle barnetilsyn-spesifikke felter`() {
        val vedtakBarnetilsynDVH = VedtakstatistikkMapper.mapTilVedtakBarnetilsynDVH(
            IverksettBarnetilsyn(
                fagsak = fagsakdetaljer(stønadstype = StønadType.BARNETILSYN),
                behandling = behandlingsdetaljer(),
                søker = Søker(
                    personIdent = søker,
                    barn = listOf(
                        Barn(personIdent = barnFnr),
                        Barn(termindato = termindato)
                    ),
                    tilhørendeEnhet = "4489",
                    adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG
                ),
                vedtak = vedtaksdetaljerBarnetilsyn()
            ),
            null
        )

        assertThat(vedtakBarnetilsynDVH.aktivitetskrav?.name).isEqualTo(AktivitetsvilkårBarnetilsyn.ER_I_ARBEID.name)
        assertThat(vedtakBarnetilsynDVH.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakBarnetilsynDVH.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakBarnetilsynDVH.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().fraOgMed).isEqualTo(LocalDate.of(2021, 1, 1))
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().tilOgMed).isEqualTo(LocalDate.of(2021, 1, 31))
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().utgifter).isEqualTo(1000)
        assertThat(vedtakBarnetilsynDVH.vedtaksperioder.first().antallBarn).isEqualTo(1)
        assertThat(vedtakBarnetilsynDVH.utbetalinger).hasSize(2)
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte).hasSize(1)
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().fraOgMed).isEqualTo(LocalDate.of(2021, 5, 1))
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().tilOgMed).isEqualTo(LocalDate.of(2021, 7, 31))
        assertThat(vedtakBarnetilsynDVH.perioderKontantstøtte.first().beløp).isEqualTo(1000)
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad).hasSize(1)
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().fraOgMed).isEqualTo(LocalDate.of(2021, 6, 1))
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().tilOgMed).isEqualTo(LocalDate.of(2021, 8, 31))
        assertThat(vedtakBarnetilsynDVH.perioderTilleggsstønad.first().beløp).isEqualTo(2000)
    }

    @Test
    internal fun `skal mappe iverksett til VedtakSkolepenger`() {
        val vedtakSkolepenger = VedtakstatistikkMapper.mapTilVedtakSkolepengeDVH(
            IverksettSkolepenger(
                fagsak = fagsakdetaljer(stønadstype = StønadType.BARNETILSYN),
                behandling = behandlingsdetaljer(),
                søker = Søker(
                    personIdent = søker,
                    barn = listOf(
                        Barn(personIdent = barnFnr),
                        Barn(termindato = termindato)
                    ),
                    tilhørendeEnhet = "4489",
                    adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG
                ),
                vedtak = vedtaksdetaljerSkolepenger()
            ),
            null
        )

        assertThat(vedtakSkolepenger.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakSkolepenger.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakSkolepenger.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakSkolepenger.vedtaksperioder).hasSize(1)
        assertThat(vedtakSkolepenger.utbetalinger).hasSize(2)
        assertThat(vedtakSkolepenger.vilkårsvurderinger).hasSameElementsAs(
            lagVilkårsvurderinger().map {
                VilkårsvurderingDto(
                    Vilkår.valueOf(it.vilkårType.name),
                    VilkårsresultatEksterneKontrakter.valueOf(it.resultat.name)
                )
            }
        )

        val forventetSkoleårsperiode = vedtaksdetaljerSkolepenger().vedtaksperioder.first().perioder.first()
        val mappetSkoleårsperiode = vedtakSkolepenger.vedtaksperioder.first().perioder.first()

        assertThat(mappetSkoleårsperiode.datoFra).isEqualTo(forventetSkoleårsperiode.periode.fomDato)
        assertThat(mappetSkoleårsperiode.datoTil).isEqualTo(forventetSkoleårsperiode.periode.tomDato)
        assertThat(mappetSkoleårsperiode.studiebelastning).isEqualTo(forventetSkoleårsperiode.studiebelastning)
        assertThat(mappetSkoleårsperiode.studietype).isEqualTo(Studietype.valueOf(forventetSkoleårsperiode.studietype.name))

        val forventetUtgiftsperiode = vedtaksdetaljerSkolepenger().vedtaksperioder.first().utgiftsperioder.first()
        val mappetUtgiftsperiode = vedtakSkolepenger.vedtaksperioder.first().utgifter.first()

        assertThat(mappetUtgiftsperiode.utgiftsdato).isEqualTo(forventetUtgiftsperiode.utgiftsdato)
        assertThat(mappetUtgiftsperiode.utgiftsbeløp).isEqualTo(forventetUtgiftsperiode.utgifter)
        assertThat(mappetUtgiftsperiode.utbetaltBeløp).isEqualTo(forventetUtgiftsperiode.stønad)
    }

    fun fagsakdetaljer(stønadstype: StønadType = StønadType.OVERGANGSSTØNAD): Fagsakdetaljer =
        Fagsakdetaljer(
            fagsakId = fagsakId,
            eksternId = eksternFagsakId,
            stønadstype = stønadstype
        )

    fun behandlingsdetaljer(): Behandlingsdetaljer =
        Behandlingsdetaljer(
            forrigeBehandlingId = null,
            behandlingId = behandlingId,
            eksternId = eksternBehandlingId,
            behandlingType = BehandlingType.REVURDERING,
            behandlingÅrsak = BehandlingÅrsak.SØKNAD,
            relatertBehandlingId = null,
            vilkårsvurderinger = lagVilkårsvurderinger(),
            aktivitetspliktInntrefferDato = null
        )

    fun tilkjentYtelse(): TilkjentYtelse =
        TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag = null,
            status = TilkjentYtelseStatus.OPPRETTET,
            andelerTilkjentYtelse = listOf(
                AndelTilkjentYtelse(
                    beløp = 9000,
                    periode = Månedsperiode("2021-01" to "2021-05"),
                    inntekt = 300000,
                    samordningsfradrag = 1000,
                    inntektsreduksjon = 11000,
                    periodeId = 1,
                    forrigePeriodeId = null,
                    kildeBehandlingId = behandlingId
                ),
                AndelTilkjentYtelse(
                    beløp = 10000,
                    periode = Månedsperiode("2021-06" to "2021-10"),
                    inntekt = 300000,
                    samordningsfradrag = 0,
                    inntektsreduksjon = 11000,
                    periodeId = 2,
                    forrigePeriodeId = 1,
                    kildeBehandlingId = behandlingId
                )
            ),
            startmåned = YearMonth.now()
        )

    fun vedtaksdetaljerSkolepenger() = VedtaksdetaljerSkolepenger(
        vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtakstidspunkt = LocalDateTime.now(),
        opphørÅrsak = null,
        saksbehandlerId = "A123456",
        beslutterId = "B123456",
        tilkjentYtelse = tilkjentYtelse(),
        vedtaksperioder = listOf(
            VedtaksperiodeSkolepenger(
                perioder = listOf(
                    DelårsperiodeSkoleårSkolepenger(
                        studietype = SkolepengerStudietype.HØGSKOLE_UNIVERSITET,
                        periode = Månedsperiode("2021-01" to "2021-01"),
                        studiebelastning = 100,
                        makssatsForSkoleår = 50000
                    )
                ),
                utgiftsperioder = listOf(
                    SkolepengerUtgift(
                        utgiftsdato = LocalDate.of(2021, 1, 1),
                        utgifter = 5000,
                        stønad = 5000
                    )
                )
            )
        )
    )

    fun vedtaksdetaljerBarnetilsyn() = VedtaksdetaljerBarnetilsyn(
        vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtakstidspunkt = LocalDateTime.now(),
        opphørÅrsak = null,
        saksbehandlerId = "A123456",
        beslutterId = "B123456",
        tilkjentYtelse = tilkjentYtelse(),
        vedtaksperioder = listOf(
            VedtaksperiodeBarnetilsyn(
                periode = Månedsperiode("2021-01" to "2021-01"),
                utgifter = 1000,
                antallBarn = 1
            ),
            VedtaksperiodeBarnetilsyn(
                periode = Månedsperiode("2021-06" to "2021-10"),
                utgifter = 2000,
                antallBarn = 2
            )
        ),
        brevmottakere = Brevmottakere(emptyList()),
        kontantstøtte = listOf(
            PeriodeMedBeløp(
                periode = Månedsperiode("2021-05" to "2021-07"),
                beløp = 1000
            )
        ),
        tilleggsstønad = listOf(
            PeriodeMedBeløp(
                periode = Månedsperiode("2021-06" to "2021-08"),
                beløp = 2000
            )
        )
    )

    fun vedtaksdetaljerOvergangsstønad(): VedtaksdetaljerOvergangsstønad {
        return VedtaksdetaljerOvergangsstønad(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = vedtakstidspunkt,
            opphørÅrsak = null,
            saksbehandlerId = "A123456",
            beslutterId = "B123456",
            tilkjentYtelse = tilkjentYtelse(),
            vedtaksperioder = listOf(
                VedtaksperiodeOvergangsstønad(
                    aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                    periode = Månedsperiode("2021-02" to "2021-03"),
                    periodeType = VedtaksperiodeType.PERIODE_FØR_FØDSEL
                ),
                VedtaksperiodeOvergangsstønad(
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                    periode = Månedsperiode("2021-06" to "2021-10"),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE
                )
            ),
            brevmottakere = Brevmottakere(emptyList())
        )
    }

    private fun lagVilkårsvurderinger(): List<Vilkårsvurdering> = listOf(
        Vilkårsvurdering(
            vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                            SvarId.JA,
                            null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.MEDLEMSKAP_UNNTAK,
                            SvarId.NEI,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.LOVLIG_OPPHOLD,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                            SvarId.JA,
                            null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.OPPHOLD_UNNTAK,
                            SvarId.NEI,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.MOR_ELLER_FAR,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN,
                            SvarId.JA,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SIVILSTAND,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE,
                            SvarId.JA,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SAMLIV,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.LEVER_IKKE_I_EKTESKAPLIGNENDE_FORHOLD,
                            SvarId.JA,
                            null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.LEVER_IKKE_MED_ANNEN_FORELDER,
                            SvarId.JA,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.SKRIFTLIG_AVTALE_OM_DELT_BOSTED,
                            SvarId.NEI,
                            null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.NÆRE_BOFORHOLD,
                            svar = SvarId.NEI,
                            begrunnelse = null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.MER_AV_DAGLIG_OMSORG,
                            svar = SvarId.JA,
                            begrunnelse = null
                        )
                    )
                )
            )
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
                    vurderinger = listOf()
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.NYTT_BARN_SAMME_PARTNER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_FÅTT_ELLER_VENTER_NYTT_BARN_MED_SAMME_PARTNER,
                            SvarId.NEI,
                            null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                            svar = SvarId.NEI,
                            begrunnelse = null
                        )
                    )
                )
            )
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.FYLLER_BRUKER_AKTIVITETSPLIKT,
                            SvarId.JA,
                            null
                        )
                    )
                )
            )
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.TIDLIGERE_VEDTAKSPERIODER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING,
                            SvarId.NEI,
                            null
                        )
                    )
                ),
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_TIDLIGERE_MOTTATT_OVERGANSSTØNAD,
                            SvarId.NEI,
                            null
                        )
                    )
                )
            )
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET_ARBEID,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.ER_I_ARBEID_ELLER_FORBIGÅENDE_SYKDOM,
                            SvarId.ER_I_ARBEID,
                            null
                        )
                    )
                )
            )
        )
    )
}
