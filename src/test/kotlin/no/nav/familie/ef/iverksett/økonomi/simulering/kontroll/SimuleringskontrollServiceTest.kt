package no.nav.familie.ef.iverksett.økonomi.simulering.kontroll

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.detaljertSimuleringResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.ef.iverksett.simuleringsoppsummering
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class SimuleringskontrollServiceTest {

    private var logWatcher = ListAppender<ILoggingEvent>()

    private val simuleringService = mockk<SimuleringService>()

    private val simuleringskontrollRepository = mockk<SimuleringskontrollRepository>()

    private val service = SimuleringskontrollService(
        simuleringService,
        simuleringskontrollRepository,
        mockFeatureToggleService(),
    )

    private val JAN = YearMonth.of(2023, 1)
    private val FEB = YearMonth.of(2023, 2)
    private val MARS = YearMonth.of(2023, 3)

    @BeforeEach
    fun setUp() {
        logWatcher = ListAppender()
        (LoggerFactory.getLogger(SimuleringskontrollService::class.java) as Logger).addAppender(logWatcher)
        logWatcher.start()
        every { simuleringskontrollRepository.insert(any()) } answers { firstArg() }
    }

    @Test
    fun `skal ikke lagre noe når listene er empty`() {
        kontroller(emptyList(), emptyList())

        val logmeldinger = getLogmeldinger()
        assertThat(logmeldinger).hasSize(1)
        assertThat(logmeldinger[0]).contains("tidligereFørstePeriodeErNull=true nyFørstePeriodeErNull=true")

        verify(exactly = 0) { simuleringskontrollRepository.insert(any()) }
    }

    @Test
    fun `skal lagre data når det er diff på resultat før første endring i ny simulering`() {
        val tidligerePerioder = listOf(simuleringsperiode(JAN, 10), simuleringsperiode(FEB, 10))
        val nyePerioder = listOf(simuleringsperiode(FEB, 10))

        kontroller(tidligerePerioder, nyePerioder)

        val logmeldinger = getLogmeldinger()
        assertThat(logmeldinger).hasSize(1)
        assertThat(logmeldinger[0]).contains("Har diff i resultat før ny simuleringsendring")

        verify(exactly = 1) { simuleringskontrollRepository.insert(any()) }
    }

    @Test
    fun `skal lagre data når det er diff på beløp på en måned`() {
        val tidligerePerioder = listOf(simuleringsperiode(FEB, 20))
        val nyePerioder = listOf(simuleringsperiode(FEB, 10))

        kontroller(tidligerePerioder, nyePerioder)

        val logmeldinger = getLogmeldinger()
        assertThat(logmeldinger).hasSize(1)
        assertThat(logmeldinger[0]).contains("måned=2023-02 resultatMedGammelUG=20 resultatMedNyUG=10")

        verify(exactly = 1) { simuleringskontrollRepository.insert(any()) }
    }

    @Test
    fun `skal lagre data når tidligere resultatet mangler verdier`() {
        val tidligerePerioder = listOf(simuleringsperiode(FEB, 10))
        val nyePerioder = listOf(simuleringsperiode(FEB, 10), simuleringsperiode(MARS, 20))

        kontroller(tidligerePerioder, nyePerioder)

        val logmeldinger = getLogmeldinger()
        assertThat(logmeldinger).hasSize(1)
        assertThat(logmeldinger[0]).contains("diff i antall måneder")

        verify(exactly = 1) { simuleringskontrollRepository.insert(any()) }
    }

    @Test
    fun `skal lagre data når nye resultatet mangler verdier`() {
        val tidligerePerioder = listOf(simuleringsperiode(FEB, 10), simuleringsperiode(MARS, 20))
        val nyePerioder = listOf(simuleringsperiode(FEB, 10))

        kontroller(tidligerePerioder, nyePerioder)

        val logmeldinger = getLogmeldinger()
        assertThat(logmeldinger).hasSize(1)
        assertThat(logmeldinger[0]).contains("diff i antall måneder")

        verify(exactly = 1) { simuleringskontrollRepository.insert(any()) }
    }

    private fun kontroller(
        tidligerePerioder: List<Simuleringsperiode>,
        nyePerioder: List<Simuleringsperiode>,
    ) {
        every {
            simuleringService.hentBeriketSimulering(any(), any())
        } returns lagBeriketSimuleringsresultat(nyePerioder)

        val data = iverksettData()

        kontroller(data, tidligerePerioder)
    }

    private fun simuleringsperiode(måned: YearMonth, beløp: Int) =
        Simuleringsperiode(
            fom = måned.atDay(1),
            tom = måned.atEndOfMonth(),
            forfallsdato = LocalDate.now(),
            nyttBeløp = BigDecimal.ZERO,
            tidligereUtbetalt = BigDecimal.ZERO,
            resultat = beløp.toBigDecimal(),
            feilutbetaling = BigDecimal.ZERO,
        )

    private fun getLogmeldinger() =
        logWatcher.list.map { it.formattedMessage }
            .filterNot { it.contains("kontroll av ny utbetalingsgenerator utført") }

    private fun kontroller(data: IverksettOvergangsstønad, tidligereSimuleringsperioder: List<Simuleringsperiode>) {
        service.kontrollerMedNyUtbetalingsgenerator(data) { lagBeriketSimuleringsresultat(tidligereSimuleringsperioder) }
    }

    private fun lagBeriketSimuleringsresultat(perioder: List<Simuleringsperiode>): BeriketSimuleringsresultat {
        return BeriketSimuleringsresultat(
            detaljer = detaljertSimuleringResultat(),
            oppsummering = simuleringsoppsummering().copy(perioder = perioder),
        )
    }

    private fun iverksettData(): IverksettOvergangsstønad {
        return lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andeler = emptyList(),
        )
    }
}
