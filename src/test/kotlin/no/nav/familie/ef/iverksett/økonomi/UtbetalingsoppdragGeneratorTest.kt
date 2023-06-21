package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal class UtbetalingsoppdragGeneratorTest {

    @Test
    fun `Andeler med behandlingId, periodeId og forrigePeriodeId blir oppdaterte i lagTilkjentYtelseMedUtbetalingsoppdrag`() {
        val behandlingA = UUID.randomUUID()
        val behandlingB = UUID.randomUUID()
        val andel1 = opprettAndel(
            2,
            YearMonth.of(2020, 1),
            YearMonth.of(2020, 12),
        ) // endres ikke, beholder kildeBehandlingId
        val andel2 = opprettAndel(2, YearMonth.of(2021, 1), YearMonth.of(2021, 12)) // endres i behandling b
        val andel3 = opprettAndel(2, YearMonth.of(2022, 1), YearMonth.of(2022, 12)) // ny i behandling b
        val førsteTilkjentYtelse =
            lagTilkjentYtelseMedUtbetalingsoppdragNy(
                opprettTilkjentYtelseMedMetadata(
                    behandlingA,
                    andel1.periode.fom,
                    andel1,
                    andel2,
                ),
            )

        assertFørsteBehandling(førsteTilkjentYtelse, behandlingA)

        val nyePerioder = opprettTilkjentYtelseMedMetadata(
            behandlingB,
            andel1.periode.fom,
            andel1,
            andel2.copy(periode = andel2.periode.copy(tom = andel2.periode.tom.minusMonths(2))),
            andel3,
        )
        val utbetalingsoppdragB = lagTilkjentYtelseMedUtbetalingsoppdragNy(nyePerioder, førsteTilkjentYtelse)

        assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(utbetalingsoppdragB, behandlingA, behandlingB)
    }

    private fun assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(
        utbetalingsoppdragB: TilkjentYtelse,
        behandlingA: UUID?,
        behandlingB: UUID?,
    ) {
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[0],
            expectedPeriodeId = 0,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA,
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[1],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = 0,
            expectedKildeBehandlingId = behandlingA,
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[2],
            expectedPeriodeId = 2,
            expectedForrigePeriodeId = 1,
            expectedKildeBehandlingId = behandlingB,
        )
    }

    private fun assertFørsteBehandling(
        førsteTilkjentYtelse: TilkjentYtelse,
        behandlingA: UUID?,
    ) {
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[0],
            expectedPeriodeId = 0,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA,
        )
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[1],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = 0,
            expectedKildeBehandlingId = behandlingA,
        )
    }

    private fun assertAndel(
        andelTilkjentYtelse: AndelTilkjentYtelse,
        expectedPeriodeId: Long?,
        expectedForrigePeriodeId: Long?,
        expectedKildeBehandlingId: UUID?,
    ) {
        assertThat(andelTilkjentYtelse.periodeId).isEqualTo(expectedPeriodeId)
        assertThat(andelTilkjentYtelse.forrigePeriodeId).isEqualTo(expectedForrigePeriodeId)
        assertThat(andelTilkjentYtelse.kildeBehandlingId).isEqualTo(expectedKildeBehandlingId)
    }

    private fun opprettAndel(
        beløp: Int,
        stønadFom: YearMonth,
        stønadTom: YearMonth,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
    ) =
        lagAndelTilkjentYtelse(
            beløp = beløp,
            fraOgMed = stønadFom,
            tilOgMed = stønadTom,
            periodeId = periodeId,
            forrigePeriodeId = forrigePeriodeId,
            kildeBehandlingId = UUID.randomUUID(),
        )

    private fun opprettTilkjentYtelseMedMetadata(
        behandlingId: UUID,
        startmåned: YearMonth,
        vararg andelTilkjentYtelse: AndelTilkjentYtelse,
    ) =
        TilkjentYtelseMedMetaData(
            tilkjentYtelse = TilkjentYtelse(
                id = UUID.randomUUID(),
                utbetalingsoppdrag = null,
                status = TilkjentYtelseStatus.OPPRETTET,
                andelerTilkjentYtelse = andelTilkjentYtelse.toList(),
                startmåned = startmåned,
            ),
            personIdent = "1",
            behandlingId = behandlingId,
            eksternBehandlingId = 1,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = 1,
            saksbehandlerId = "VL",
            vedtaksdato = LocalDate.of(2021, 5, 12),
        )
}
