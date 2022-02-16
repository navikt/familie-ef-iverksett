package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import org.opentest4j.ValueWrapper
import java.time.LocalDate
import java.util.UUID

internal class UtbetalingsoppdragGeneratorTest {

    @Test
    fun csvTest() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/Sekvens1.csv"))
    }

    @Test
    fun `Revurdering med 0 beløp beholder periodId til neste behandling med periode med beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_med_0beløp.csv"))
    }

    @Test
    fun `Revurdering bak i tiden før vi hadde beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_bak_i_tiden.csv"))
    }

    @Test
    fun `Revurdering med opphørsdato bak i tiden, før vi hadde beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphør_bak_i_tiden.csv"))
    }

    @Test
    fun `Revurdering med 0 beløp beholder periodId når man har flere perioder`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_med_0beløp_flere_perioder.csv"))
    }

    @Test
    fun `Revurdering bak i tiden med beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_bak_i_tiden_med_beløp.csv"))
    }

    @Test
    fun `Revurdering frem i tiden med 0-beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_frem_i_tiden_med_0beløp.csv"))
    }

    @Test
    fun `Revurdering frem i tiden med beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_frem_i_tiden_med_beløp.csv"))
    }

    @Test
    fun `Revurdering med 0 beløp beholder periodId når man har flere perioder, men også endring i tidligere beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_med_0beløp_flere_perioder_overlapp.csv"))
    }

    @Test
    fun `Revurdering med 0 beløp beholder periodId når man har flere perioder, før tidligere periode`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_med_0beløp_flere_perioder_overlapp_før_tidiligere_periode.csv"))
    }

    @Test
    fun `Revurdering med opphør bak i tiden, samt ny periode frem i tiden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphør_bak_i_tiden_med_periode.csv"))
    }

    @Test
    fun `Førstegangsbehandling med 0 i beløp, med revurderinger med 0 i beløp`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/førstegangsbehandling_med_0beløp_revurderinger_med_0beløp.csv"))
    }

    @Test
    fun `Har en periode og får en endring mitt i perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/1_periode_får_en_endring_i_perioden.csv"))
    }

    @Test
    fun `Har to perioder og får en endring i første perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/2_perioder_får_en_endring_i_første_perioden.csv"))
    }

    @Test
    fun `Har to perioder og får en endring som har startdato før første perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/2_perioder_får_ny_periode_før_første_periode.csv"))
    }

    @Test
    fun `Har to perioder og får en endring for start og sluttdatot i andre perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/2_perioder_får_en_endring_i_andre_perioden.csv"))
    }

    @Test
    fun `Har tre perioder og får en endring i første perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/3_perioder_får_en_endring_i_første_perioden.csv"))
    }

    @Test
    fun `Har tre perioder og får en endring i andre perioden`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/3_perioder_får_en_endring_i_andre_perioden.csv"))
    }

    @Test
    fun `Har en perioder, legger til en ny periode som er lik den forrige`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/1_periode_får_en_lik_periode.csv"))
    }

    @Test
    fun `Har en perioder, legger til en andre`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/1_periode_får_en_ny_perioden.csv"))
    }

    @Test
    fun `Har to perioder, legger til en tredje, endrer på den andre`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/2_perioder_får_ny_periode_og_endring_i_andre_perioden.csv"))
    }

    @Test
    fun `Har en periode og får ett opphør`() {
        TestOppdragRunner.run(javaClass.getResource("/oppdrag/1_periode_får_ett_opphør.csv"))
    }

    @Test
    fun `Har 2 perioder og får en endring på andre perioden men har feil behandlingId i testen`() {
        val catchThrowable = catchThrowable {
            TestOppdragRunner.run(javaClass.getResource(
                    "/oppdrag/2_periode_får_en_endring_i_andre_perioden_feiler_pga_feil_behandling_id.csv"))
        }
        assertThat(catchThrowable)
                .hasMessageContaining("Feiler for gruppe med indeks 1 ==> ")
                .isInstanceOf(AssertionFailedError::class.java)

        assertExpectedOgActualErLikeUtenomFeltSomFeiler(catchThrowable, "kildeBehandlingId")
    }

    @Test
    fun `Har 2 perioder og får en endring på andre perioden men har feil periodeId i testen`() {
        val catchThrowable = catchThrowable {
            TestOppdragRunner.run(javaClass.getResource(
                    "/oppdrag/2_periode_får_en_endring_i_andre_perioden_feiler_pga_feil_periode_id.csv"))
        }
        assertThat(catchThrowable)
                .hasMessageContaining("Feiler for gruppe med indeks 1 ==> ")
                .isInstanceOf(AssertionFailedError::class.java)

        assertExpectedOgActualErLikeUtenomFeltSomFeiler(catchThrowable, "periodeId")
    }

    @Nested
    inner class Opphørsdato {

        @Test
        internal fun `opphørsdato etter forrige andel sin opphørsdato er ikke gyldig`() {
            assertThatThrownBy {
                TestOppdragRunner.run(javaClass.getResource("/oppdrag/opphør_etter_andel_sitt_dato_feiler.csv"))
            }.hasMessageContaining("Kan ikke sette opphør etter dato på første perioden")
        }

        @Test
        fun `revurdering uten opphørsdato når forrige revurdering har opphørsdato er ikke gyldig`() {
            assertThatThrownBy {
                TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_mangler_opphørsdato.csv"))
            }.hasMessageContaining("Må ha med opphørsdato hvis man tidligere opphørt")
        }

        @Test
        fun `opphørsdato etter tidligere opphørsdato er ikke gyldig`() {
            assertThatThrownBy {
                TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_etter_tidligere.csv"))
            }.hasMessageContaining("kan ikke være etter forrigeOpphørsdato")
        }

        @Test
        fun `opphørsdato før tidligere skal sende nytt opphørsdato til oppdrag`() {
            TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_før_tidligere_opphørsdato.csv"))
        }

        @Test
        fun `opphørsdato er den samme som tidligere - skal ikke sende opphørsdato på nytt når det finnes endringer senere i tiden`() {
            TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_før_tidligere_opphørsdato.csv"))
        }

        @Test
        fun `opphør en tidligere periode, når opphørsdato allerede finnes, men skal då sende opphørsdato till oppdrag for den andelen som opphører`() {
            TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_samme_med_opphør_senere.csv"))
        }

        @Test
        fun `har opphørsdato, sender ny tilkjent ytelse uten andeler - opphører fra første tidligere andelen`() {
            TestOppdragRunner.run(javaClass.getResource("/oppdrag/revurdering_opphørsdato_samme_med_opphør_senere.csv"))
        }
    }

    @Test
    fun `Andeler med behandlingId, periodeId og forrigePeriodeId blir oppdaterte i lagTilkjentYtelseMedUtbetalingsoppdrag`() {
        val behandlingA = UUID.randomUUID()
        val behandlingB = UUID.randomUUID()
        val andel1 = opprettAndel(2,
                                  LocalDate.of(2020, 1, 1),
                                  LocalDate.of(2020, 12, 31)) // endres ikke, beholder kildeBehandlingId
        val andel2 = opprettAndel(2, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)) // endres i behandling b
        val andel3 = opprettAndel(2, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)) // ny i behandling b
        val førsteTilkjentYtelse =
                lagTilkjentYtelseMedUtbetalingsoppdrag(opprettTilkjentYtelseMedMetadata(behandlingA, andel1, andel2))

        assertFørsteBehandling(førsteTilkjentYtelse, behandlingA)

        val nyePerioder = opprettTilkjentYtelseMedMetadata(behandlingB,
                                                           andel1,
                                                           andel2.copy(tilOgMed = andel2.tilOgMed.minusMonths(2)),
                                                           andel3)
        val utbetalingsoppdragB = lagTilkjentYtelseMedUtbetalingsoppdrag(nyePerioder, førsteTilkjentYtelse)

        assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(utbetalingsoppdragB, behandlingA, behandlingB)
    }

    private fun assertExpectedOgActualErLikeUtenomFeltSomFeiler(catchThrowable: Throwable?,
                                                                feltSomSkalFiltreres: String) {
        val assertionFailedError = catchThrowable as AssertionFailedError
        val actual = filterAwayBehandlingId(assertionFailedError.actual, feltSomSkalFiltreres)
        val expected = filterAwayBehandlingId(assertionFailedError.expected, feltSomSkalFiltreres)
        assertThat(actual).isEqualTo(expected)
    }

    private fun filterAwayBehandlingId(valueWrapper: ValueWrapper, feltSomSkalFiltreres: String) =
            valueWrapper.stringRepresentation
                    .split("\n")
                    .filterNot { it.contains(feltSomSkalFiltreres) }
                    .joinToString("\n")

    private fun assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(utbetalingsoppdragB: TilkjentYtelse,
                                                                               behandlingA: UUID?,
                                                                               behandlingB: UUID?) {
        assertAndel(andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[0],
                    expectedPeriodeId = 1,
                    expectedForrigePeriodeId = null,
                    expectedKildeBehandlingId = behandlingA)
        assertAndel(andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[1],
                    expectedPeriodeId = 3,
                    expectedForrigePeriodeId = 2,
                    expectedKildeBehandlingId = behandlingB)
        assertAndel(andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[2],
                    expectedPeriodeId = 4,
                    expectedForrigePeriodeId = 3,
                    expectedKildeBehandlingId = behandlingB)
    }

    private fun assertFørsteBehandling(førsteTilkjentYtelse: TilkjentYtelse,
                                       behandlingA: UUID?) {
        assertAndel(andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[0],
                    expectedPeriodeId = 1,
                    expectedForrigePeriodeId = null,
                    expectedKildeBehandlingId = behandlingA)
        assertAndel(andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[1],
                    expectedPeriodeId = 2,
                    expectedForrigePeriodeId = 1,
                    expectedKildeBehandlingId = behandlingA)
    }

    private fun assertAndel(andelTilkjentYtelse: AndelTilkjentYtelse,
                            expectedPeriodeId: Long?,
                            expectedForrigePeriodeId: Long?,
                            expectedKildeBehandlingId: UUID?) {
        assertThat(andelTilkjentYtelse.periodeId).isEqualTo(expectedPeriodeId)
        assertThat(andelTilkjentYtelse.forrigePeriodeId).isEqualTo(expectedForrigePeriodeId)
        assertThat(andelTilkjentYtelse.kildeBehandlingId).isEqualTo(expectedKildeBehandlingId)
    }

    private fun opprettAndel(beløp: Int, stønadFom: LocalDate, stønadTom: LocalDate) =
            lagAndelTilkjentYtelse(beløp = beløp,
                                   periodetype = Periodetype.MÅNED,
                                   fraOgMed = stønadFom,
                                   tilOgMed = stønadTom,
                                   periodeId = 100, // overskreves
                                   forrigePeriodeId = 100, // overskreves
                                   kildeBehandlingId = UUID.randomUUID()) // overskreves

    private fun opprettTilkjentYtelseMedMetadata(behandlingId: UUID,
                                                 vararg andelTilkjentYtelse: AndelTilkjentYtelse) =
            TilkjentYtelseMedMetaData(tilkjentYtelse = TilkjentYtelse(id = UUID.randomUUID(),
                                                                      utbetalingsoppdrag = null,
                                                                      status = TilkjentYtelseStatus.OPPRETTET,
                                                                      andelerTilkjentYtelse = andelTilkjentYtelse.toList(),
                                                                      opphørsdato = null),
                                      personIdent = "1",
                                      behandlingId = behandlingId,
                                      eksternBehandlingId = 1,
                                      stønadstype = StønadType.OVERGANGSSTØNAD,
                                      eksternFagsakId = 1,
                                      saksbehandlerId = "VL",
                                      vedtaksdato = LocalDate.of(2021, 5, 12)
            )

}
