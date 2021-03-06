package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import org.opentest4j.ValueWrapper
import java.time.LocalDate
import java.util.UUID

internal class UtbetalingsoppdragGeneratorTest {

    @Test
    fun `Andeler med behandlingId, periodeId og forrigePeriodeId blir oppdaterte i lagTilkjentYtelseMedUtbetalingsoppdrag`() {
        val behandlingA = UUID.randomUUID()
        val behandlingB = UUID.randomUUID()
        val andel1 = opprettAndel(
            2,
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31)
        ) // endres ikke, beholder kildeBehandlingId
        val andel2 = opprettAndel(2, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)) // endres i behandling b
        val andel3 = opprettAndel(2, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)) // ny i behandling b
        val førsteTilkjentYtelse =
            lagTilkjentYtelseMedUtbetalingsoppdrag(
                opprettTilkjentYtelseMedMetadata(
                    behandlingA,
                    andel1.fraOgMed,
                    andel1,
                    andel2
                )
            )

        assertFørsteBehandling(førsteTilkjentYtelse, behandlingA)

        val nyePerioder = opprettTilkjentYtelseMedMetadata(
            behandlingB,
            andel1.fraOgMed,
            andel1,
            andel2.copy(tilOgMed = andel2.tilOgMed.minusMonths(2)),
            andel3
        )
        val utbetalingsoppdragB = lagTilkjentYtelseMedUtbetalingsoppdrag(nyePerioder, førsteTilkjentYtelse)

        assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(utbetalingsoppdragB, behandlingA, behandlingB)
    }

    private fun assertExpectedOgActualErLikeUtenomFeltSomFeiler(
        catchThrowable: Throwable?,
        feltSomSkalFiltreres: String
    ) {
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

    private fun assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(
        utbetalingsoppdragB: TilkjentYtelse,
        behandlingA: UUID?,
        behandlingB: UUID?
    ) {
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[0],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[1],
            expectedPeriodeId = 3,
            expectedForrigePeriodeId = 2,
            expectedKildeBehandlingId = behandlingB
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[2],
            expectedPeriodeId = 4,
            expectedForrigePeriodeId = 3,
            expectedKildeBehandlingId = behandlingB
        )
    }

    private fun assertFørsteBehandling(
        førsteTilkjentYtelse: TilkjentYtelse,
        behandlingA: UUID?
    ) {
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[0],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA
        )
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[1],
            expectedPeriodeId = 2,
            expectedForrigePeriodeId = 1,
            expectedKildeBehandlingId = behandlingA
        )
    }

    private fun assertAndel(
        andelTilkjentYtelse: AndelTilkjentYtelse,
        expectedPeriodeId: Long?,
        expectedForrigePeriodeId: Long?,
        expectedKildeBehandlingId: UUID?
    ) {
        assertThat(andelTilkjentYtelse.periodeId).isEqualTo(expectedPeriodeId)
        assertThat(andelTilkjentYtelse.forrigePeriodeId).isEqualTo(expectedForrigePeriodeId)
        assertThat(andelTilkjentYtelse.kildeBehandlingId).isEqualTo(expectedKildeBehandlingId)
    }

    private fun opprettAndel(beløp: Int, stønadFom: LocalDate, stønadTom: LocalDate) =
        lagAndelTilkjentYtelse(
            beløp = beløp,
            fraOgMed = stønadFom,
            tilOgMed = stønadTom,
            periodeId = 100, // overskreves
            forrigePeriodeId = 100, // overskreves
            kildeBehandlingId = UUID.randomUUID()
        ) // overskreves

    private fun opprettTilkjentYtelseMedMetadata(
        behandlingId: UUID,
        startdato: LocalDate,
        vararg andelTilkjentYtelse: AndelTilkjentYtelse
    ) =
        TilkjentYtelseMedMetaData(
            tilkjentYtelse = TilkjentYtelse(
                id = UUID.randomUUID(),
                utbetalingsoppdrag = null,
                status = TilkjentYtelseStatus.OPPRETTET,
                andelerTilkjentYtelse = andelTilkjentYtelse.toList(),
                startdato = startdato
            ),
            personIdent = "1",
            behandlingId = behandlingId,
            eksternBehandlingId = 1,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = 1,
            saksbehandlerId = "VL",
            vedtaksdato = LocalDate.of(2021, 5, 12)
        )
}
