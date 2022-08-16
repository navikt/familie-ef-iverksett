package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring.NY
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Properties
import java.util.UUID

internal class VentePåStatusFraØkonomiTaskTest {

    private val oppdragClient = mockk<OppdragClient>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskRepository = mockk<TaskRepository>()
    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val behandlingId: UUID = UUID.randomUUID()
    private val iverksettingService = IverksettingService(
        taskRepository = taskRepository,
        oppdragClient = oppdragClient,
        iverksettingRepository = iverksettingRepository,
        iverksettResultatService = iverksettResultatService,
        featureToggleService = mockFeatureToggleService()
    )

    private val ventePåStatusFraØkonomiTask =
        VentePåStatusFraØkonomiTask(iverksettingRepository, iverksettingService, taskRepository, iverksettResultatService)

    @BeforeEach
    internal fun setUp() {
        every { oppdragClient.hentStatus(any()) } returns OppdragStatusMedMelding(OppdragStatus.KVITTERT_OK, "OK")
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettDto(behandlingId).toDomain())
        every { iverksettResultatService.oppdaterOppdragResultat(behandlingId, any()) } just runs
        every { taskRepository.save(any()) } answers { firstArg() }
    }

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTaskhvis, forvent ingen unntak`() {
        val oppdragResultatSlot = slot<OppdragResultat>()
        every { iverksettResultatService.hentTilkjentYtelse(behandlingId) } returns tilkjentYtelse(listOf(utbetalingsperiode))

        runTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 1) { iverksettResultatService.oppdaterOppdragResultat(behandlingId, capture(oppdragResultatSlot)) }
        assertThat(oppdragResultatSlot.captured.oppdragStatus).isEqualTo(OppdragStatus.KVITTERT_OK)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    internal fun `Skal ikke gjøre noe hvis ingen utbetalingoppdrag på tilkjent ytelse`() {
        every { iverksettResultatService.hentTilkjentYtelse(behandlingId) } returns tilkjentYtelse()

        runTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 0) { iverksettResultatService.oppdaterOppdragResultat(behandlingId, any()) }
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    internal fun `migrering - skal ikke opprette task for journalføring av vedtaksbrev`() {
        val opprettIverksettDto = opprettIverksettDto(behandlingId, behandlingÅrsak = BehandlingÅrsak.MIGRERING)
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettDto.toDomain())
        every { iverksettResultatService.hentTilkjentYtelse(behandlingId) } returns tilkjentYtelse(listOf(utbetalingsperiode))

        runTask(Task(IverksettMotOppdragTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 0) { taskRepository.save(any()) }
    }

    private fun runTask(task: Task) {
        ventePåStatusFraØkonomiTask.doTask(task)
        ventePåStatusFraØkonomiTask.onCompletion(task)
    }

    private val utbetalingsperiode = Utbetalingsperiode(
        erEndringPåEksisterendePeriode = false,
        opphør = null,
        periodeId = 0,
        forrigePeriodeId = null,
        datoForVedtak = LocalDate.now(),
        klassifisering = "",
        vedtakdatoFom = LocalDate.of(2021, 1, 1),
        vedtakdatoTom = LocalDate.of(2021, 6, 1),
        sats = BigDecimal.TEN,
        satsType = Utbetalingsperiode.SatsType.MND,
        utbetalesTil = "x",
        behandlingId = 0,
        utbetalingsgrad = null
    )

    private fun tilkjentYtelse(utbetalingsperioder: List<Utbetalingsperiode> = listOf()): TilkjentYtelse {
        return TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag = Utbetalingsoppdrag(
                kodeEndring = NY,
                fagSystem = "",
                saksnummer = "",
                aktoer = "",
                saksbehandlerId = "",
                avstemmingTidspunkt = LocalDateTime.now(),
                utbetalingsperiode = utbetalingsperioder
            ),
            status = TilkjentYtelseStatus.SENDT_TIL_IVERKSETTING,
            andelerTilkjentYtelse = listOf(),
            startmåned = YearMonth.now()
        )
    }
}
