package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.PeriodeId
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.nullAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal class UtbetalingsoppdragGeneratorTest {
    private val behandlingA = (UUID.randomUUID() to 1L)
    private val behandlingB = (UUID.randomUUID() to 2L)

    @Test
    fun `Andeler med behandlingId, periodeId og forrigePeriodeId blir oppdaterte i lagTilkjentYtelseMedUtbetalingsoppdrag`() {
        val andel1 =
            opprettAndel(
                2,
                YearMonth.of(2020, 1),
                YearMonth.of(2020, 12),
            ) // endres ikke, beholder kildeBehandlingId
        val andel2 = opprettAndel(2, YearMonth.of(2021, 1), YearMonth.of(2021, 12)) // endres i behandling b
        val andel3 = opprettAndel(2, YearMonth.of(2022, 1), YearMonth.of(2022, 12)) // ny i behandling b
        val utbetalingsoppdragA =
            lagTilkjentYtelseMedUtbetalingsoppdragNy(
                opprettTilkjentYtelseMedMetadata(
                    behandlingA,
                    andel1.periode.fom,
                    andel1,
                    andel2,
                ),
            )

        assertFørsteBehandling(utbetalingsoppdragA, behandlingA)

        val nyePerioder =
            opprettTilkjentYtelseMedMetadata(
                behandlingB,
                andel1.periode.fom,
                andel1,
                andel2.copy(periode = andel2.periode.copy(tom = andel2.periode.tom.minusMonths(2))),
                andel3,
            )
        val utbetalingsoppdragB = lagTilkjentYtelseMedUtbetalingsoppdragNy(nyePerioder, utbetalingsoppdragA)

        assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(utbetalingsoppdragB, behandlingA, behandlingB)
    }

    @Nested
    inner class HåndteringAvMinusUendeligheten {
        val andel1 =
            opprettAndel(
                0,
                YearMonth.of(2020, 1),
                YearMonth.of(2020, 12),
            )

        @Test
        fun `historiskt har vi lagret ned andeler med -uendelig fom-tom dato, som må håndteres`() {
            val startmåned = andel1.periode.fom
            val nullAndelTilkjentYtelse = nullAndelTilkjentYtelse(UUID.randomUUID(), PeriodeId(1L, forrige = null))
            val utbetalingsoppdrag =
                lagTilkjentYtelseMedUtbetalingsoppdragNy(
                    opprettTilkjentYtelseMedMetadata(
                        behandlingA,
                        startmåned,
                        andel1,
                    ),
                    TilkjentYtelse(
                        andelerTilkjentYtelse = listOf(nullAndelTilkjentYtelse),
                        startmåned = startmåned,
                    ),
                )
            assertThat(utbetalingsoppdrag.andelerTilkjentYtelse).hasSize(1)
            assertThat(utbetalingsoppdrag.utbetalingsoppdrag?.utbetalingsperiode).isEmpty()
            assertAndel(
                andelTilkjentYtelse = utbetalingsoppdrag.andelerTilkjentYtelse[0],
                expectedPeriodeId = null,
                expectedForrigePeriodeId = null,
                expectedKildeBehandlingId = andel1.kildeBehandlingId,
            )
        }
    }

    private fun assertThatAndreBehandlingIkkeEndrerPåKildeBehandlingIdPåAndel1(
        utbetalingsoppdragB: TilkjentYtelse,
        behandlingA: Pair<UUID, Long>,
        behandlingB: Pair<UUID, Long>,
    ) {
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[0],
            expectedPeriodeId = 0,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA.first,
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[1],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = 0,
            expectedKildeBehandlingId = behandlingB.first,
        )
        assertAndel(
            andelTilkjentYtelse = utbetalingsoppdragB.andelerTilkjentYtelse[2],
            expectedPeriodeId = 2,
            expectedForrigePeriodeId = 1,
            expectedKildeBehandlingId = behandlingB.first,
        )
        // sjekk at behandlingsId vi sender til økonomi er fra samme behandling ( expectedKildeBehandlingId )
        assertThat(
            utbetalingsoppdragB.utbetalingsoppdrag
                ?.utbetalingsperiode
                ?.first()
                ?.behandlingId,
        ).isEqualTo(behandlingB.second)
        assertThat(
            utbetalingsoppdragB.utbetalingsoppdrag
                ?.utbetalingsperiode
                ?.last()
                ?.behandlingId,
        ).isEqualTo(behandlingB.second)
    }

    private fun assertFørsteBehandling(
        førsteTilkjentYtelse: TilkjentYtelse,
        behandlingA: Pair<UUID, Long>,
    ) {
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[0],
            expectedPeriodeId = 0,
            expectedForrigePeriodeId = null,
            expectedKildeBehandlingId = behandlingA.first,
        )
        assertAndel(
            andelTilkjentYtelse = førsteTilkjentYtelse.andelerTilkjentYtelse[1],
            expectedPeriodeId = 1,
            expectedForrigePeriodeId = 0,
            expectedKildeBehandlingId = behandlingA.first,
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
    ) = lagAndelTilkjentYtelse(
        beløp = beløp,
        fraOgMed = stønadFom,
        tilOgMed = stønadTom,
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        kildeBehandlingId = UUID.randomUUID(),
    )

    private fun opprettTilkjentYtelseMedMetadata(
        behandlingId: Pair<UUID, Long>,
        startmåned: YearMonth,
        vararg andelTilkjentYtelse: AndelTilkjentYtelse,
    ) = TilkjentYtelseMedMetaData(
        tilkjentYtelse =
            TilkjentYtelse(
                id = UUID.randomUUID(),
                utbetalingsoppdrag = null,
                status = TilkjentYtelseStatus.OPPRETTET,
                andelerTilkjentYtelse = andelTilkjentYtelse.toList(),
                startmåned = startmåned,
            ),
        personIdent = "1",
        behandlingId = behandlingId.first,
        eksternBehandlingId = behandlingId.second,
        stønadstype = StønadType.OVERGANGSSTØNAD,
        eksternFagsakId = 1,
        saksbehandlerId = "VL",
        vedtaksdato = LocalDate.of(2021, 5, 12),
    )
}
