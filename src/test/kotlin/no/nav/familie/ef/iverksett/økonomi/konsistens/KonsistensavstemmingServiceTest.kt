package no.nav.familie.ef.iverksett.økonomi.konsistens

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingDto
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingTilkjentYtelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingUtbetalingsoppdrag
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal class KonsistensavstemmingServiceTest {

    private val oppdragClient = mockk<OppdragClient>()
    private val iverksettResultatService = mockk<IverksettResultatService>()

    private val konsistensavstemmingService = KonsistensavstemmingService(oppdragClient, iverksettResultatService)

    private val saksbehandlerId = "1"
    private val eksternBehandlingId = 1L
    private val eksternFagsakId = 1L
    private val personIdent = "2"
    private val behandlingId = UUID.randomUUID()
    private val vedtaksdato = LocalDate.of(2021, 6, 1)

    private val andel1 = lagAndelTilkjentYtelse(
        beløp = 1,
        fraOgMed = YearMonth.of(2021, 1),
        tilOgMed = YearMonth.of(2021, 1),
        kildeBehandlingId = behandlingId,
    )

    private val andel2StartDato = YearMonth.of(2021, 3)
    private val andel2Sluttdato = YearMonth.of(2021, 3)
    private val andel2 = lagAndelTilkjentYtelse(
        beløp = 2,
        fraOgMed = andel2StartDato,
        tilOgMed = andel2Sluttdato,
        kildeBehandlingId = behandlingId,
    )

    private val requestSlot = slot<KonsistensavstemmingUtbetalingsoppdrag>()

    @BeforeEach
    internal fun setUp() {
        every { iverksettResultatService.hentTilkjentYtelse(setOf(behandlingId)) } returns
            mapOf(behandlingId to lagTilkjentYtelseMedUtbetalingsoppdragNy(lagTilkjentYtelseMedMetadata()))
        every { oppdragClient.konsistensavstemming(capture(requestSlot)) } returns ""
    }

    @Test
    internal fun `skal lage utbetalingsoppdrag med perioden som er med i requesten`() {
        val andelerTilkjentYtelse = listOf(
            lagAndelTilkjentYtelseDto(
                beløp = 2,
                fraOgMed = andel2StartDato.atDay(1),
                tilOgMed = andel2Sluttdato.atEndOfMonth(),
                kildeBehandlingId = behandlingId,
            ),
        )
        val tilkjentYtelseDto = KonsistensavstemmingTilkjentYtelseDto(
            behandlingId = behandlingId,
            eksternBehandlingId = eksternBehandlingId,
            eksternFagsakId = eksternFagsakId,
            personIdent = personIdent,
            andelerTilkjentYtelse = andelerTilkjentYtelse,
        )
        val konsistensavstemmingDto = KonsistensavstemmingDto(StønadType.OVERGANGSSTØNAD, listOf(tilkjentYtelseDto))
        konsistensavstemmingService.sendKonsistensavstemming(konsistensavstemmingDto)

        val request = requestSlot.captured
        assertThat(request.fagsystem).isEqualTo("EFOG")
        assertThat(request.utbetalingsoppdrag).hasSize(1)

        val utbetalingsoppdrag = request.utbetalingsoppdrag[0]
        assertThat(utbetalingsoppdrag.utbetalingsperiode).hasSize(1)

        val utbetalingsperiode = utbetalingsoppdrag.utbetalingsperiode[0]
        assertThat(utbetalingsperiode.periodeId).isEqualTo(2)
        assertThat(utbetalingsperiode.forrigePeriodeId).isEqualTo(1)
    }

    @Test
    internal fun `skal kaste feil hvis beløpet er annerledes`() {
        val andelerTilkjentYtelse = listOf(
            lagAndelTilkjentYtelseDto(
                beløp = 1,
                fraOgMed = andel2StartDato.atDay(1),
                tilOgMed = andel2Sluttdato.atEndOfMonth(),
                kildeBehandlingId = behandlingId,
            ),
        )
        val tilkjentYtelseDto = KonsistensavstemmingTilkjentYtelseDto(
            behandlingId = behandlingId,
            eksternBehandlingId = eksternBehandlingId,
            eksternFagsakId = eksternFagsakId,
            personIdent = personIdent,
            andelerTilkjentYtelse = andelerTilkjentYtelse,
        )
        val konsistensavstemmingDto = KonsistensavstemmingDto(StønadType.OVERGANGSSTØNAD, listOf(tilkjentYtelseDto))
        assertThat(catchThrowable { konsistensavstemmingService.sendKonsistensavstemming(konsistensavstemmingDto) })
    }

    private fun lagTilkjentYtelseMedMetadata(): TilkjentYtelseMedMetaData {
        return TilkjentYtelseMedMetaData(
            tilkjentYtelse = TilkjentYtelse(
                andelerTilkjentYtelse = listOf(andel1, andel2),
                startmåned = andel1.periode.fom,
            ),
            saksbehandlerId = saksbehandlerId,
            eksternBehandlingId = eksternBehandlingId,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = eksternFagsakId,
            personIdent = personIdent,
            behandlingId = behandlingId,
            vedtaksdato = vedtaksdato,
        )
    }
}
