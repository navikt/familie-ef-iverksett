package no.nav.familie.ef.iverksett.økonomi.konsistens

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Periodebeløp
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingDto
import no.nav.familie.kontrakter.ef.iverksett.KonsistensavstemmingTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.PeriodebeløpDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingUtbetalingsoppdrag
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class KonsistensavstemmingServiceTest {


    private val oppdragClient = mockk<OppdragClient>()
    private val tilstandRepository = mockk<TilstandRepository>()

    private val konsistensavstemmingService = KonsistensavstemmingService(oppdragClient, tilstandRepository)

    private val saksbehandlerId = "1"
    private val eksternBehandlingId = 1L
    private val eksternFagsakId = 1L
    private val personIdent = "2"
    private val behandlingId = UUID.randomUUID()
    private val vedtaksdato = LocalDate.of(2021, 6, 1)

    private val andel1 =
            AndelTilkjentYtelse(Periodebeløp(1, Periodetype.MÅNED, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)))

    private val andel2StartDato = LocalDate.of(2021, 3, 1)
    private val andel2Sluttdato = LocalDate.of(2021, 3, 31)
    private val andel2 = AndelTilkjentYtelse(Periodebeløp(2, Periodetype.MÅNED, andel2StartDato, andel2Sluttdato))

    private val requestSlot = slot<KonsistensavstemmingUtbetalingsoppdrag>()

    @BeforeEach
    internal fun setUp() {
        val tilkjentYtelseMedUtbetalingsoppdrag = lagTilkjentYtelseMedUtbetalingsoppdrag(lagTilkjentYtelseMedMetadata())
        every { tilstandRepository.hentTilkjentYtelse(setOf(behandlingId)) } returns
                mapOf(behandlingId to tilkjentYtelseMedUtbetalingsoppdrag)
        every { oppdragClient.konsistensavstemming(capture(requestSlot)) } returns ""
    }

    @Test
    internal fun `skal lage utbetalingsoppdrag med perioden som er med i requesten`() {
        val andelerTilkjentYtelse = listOf(PeriodebeløpDto(2, Periodetype.MÅNED, andel2StartDato, andel2Sluttdato))
        val tilkjentYtelseDto = KonsistensavstemmingTilkjentYtelseDto(behandlingId = behandlingId,
                                                                      eksternBehandlingId = eksternBehandlingId,
                                                                      eksternFagsakId = eksternFagsakId,
                                                                      personIdent = personIdent,
                                                                      andelerTilkjentYtelse = andelerTilkjentYtelse)
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
        val andelerTilkjentYtelse = listOf(PeriodebeløpDto(1, Periodetype.MÅNED, andel2StartDato, andel2Sluttdato))
        val tilkjentYtelseDto = KonsistensavstemmingTilkjentYtelseDto(behandlingId = behandlingId,
                                                                      eksternBehandlingId = eksternBehandlingId,
                                                                      eksternFagsakId = eksternFagsakId,
                                                                      personIdent = personIdent,
                                                                      andelerTilkjentYtelse = andelerTilkjentYtelse)
        val konsistensavstemmingDto = KonsistensavstemmingDto(StønadType.OVERGANGSSTØNAD, listOf(tilkjentYtelseDto))
        assertThat(catchThrowable { konsistensavstemmingService.sendKonsistensavstemming(konsistensavstemmingDto) })
    }

    private fun lagTilkjentYtelseMedMetadata(): TilkjentYtelseMedMetaData {
        return TilkjentYtelseMedMetaData(tilkjentYtelse = TilkjentYtelse(andelerTilkjentYtelse = listOf(andel1, andel2)),
                                         saksbehandlerId = saksbehandlerId,
                                         eksternBehandlingId = eksternBehandlingId,
                                         stønadstype = StønadType.OVERGANGSSTØNAD,
                                         eksternFagsakId = eksternFagsakId,
                                         personIdent = personIdent,
                                         behandlingId = behandlingId,
                                         vedtaksdato = vedtaksdato)
    }
}