package no.nav.familie.ef.iverksett.infotrygd

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.infotrygd.OpprettPeriodeHendelseDto
import no.nav.familie.kontrakter.ef.infotrygd.Periode
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

internal class SendPerioderTilInfotrygdTaskTest {

    private val infotrygdFeedClient = mockk<InfotrygdFeedClient>(relaxed = true)
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>(relaxed = true)
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskService = mockk<TaskService>()

    private val behandlingId = UUID.randomUUID()

    private val identer = listOf(
        PersonIdentMedHistorikk("1", false),
        PersonIdentMedHistorikk("2", true)
    )

    private val requestSlot = slot<OpprettPeriodeHendelseDto>()

    val task = SendPerioderTilInfotrygdTask(
        infotrygdFeedClient,
        familieIntegrasjonerClient,
        iverksettingRepository,
        taskService
    )

    @BeforeEach
    internal fun setUp() {
        justRun { infotrygdFeedClient.opprettPeriodeHendelse(capture(requestSlot)) }
        every { familieIntegrasjonerClient.hentIdenter(any(), any()) } returns identer
    }

    @Test
    internal fun `skal sende perioder fra andeler til infotrygd`() {
        val iverksett = opprettData(lagAndelTilkjentYtelse(2, YearMonth.of(1901, 1), YearMonth.of(1901, 1)))
        every { iverksettingRepository.findByIdOrThrow(behandlingId) } returns iverksett

        task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString()))

        assertThat(requestSlot.captured.personIdenter).containsExactlyInAnyOrder("1", "2")
        assertThat(requestSlot.captured.perioder).containsExactly(
            Periode(LocalDate.of(1901, 1, 1), LocalDate.of(1901, 1, 31), true)
        )
        verify(exactly = 1) { infotrygdFeedClient.opprettPeriodeHendelse(any()) }
    }

    @Test
    internal fun `fullOvergangsstønad er false hvis samordningsfradrag eller inntektsreduksjon ikke er 0`() {
        val andelTilkjentYtelse = lagAndelTilkjentYtelse(
            beløp = 1,
            fraOgMed = YearMonth.of(1901, 1),
            tilOgMed = YearMonth.of(1901, 1),
            samordningsfradrag = 1,
            inntektsreduksjon = 0
        )
        val andelTilkjentYtelse2 = lagAndelTilkjentYtelse(
            beløp = 3,
            fraOgMed = YearMonth.of(1902, 1),
            tilOgMed = YearMonth.of(1902, 1),
            samordningsfradrag = 0,
            inntektsreduksjon = 1
        )
        val iverksett = opprettData(andelTilkjentYtelse, andelTilkjentYtelse2)
        every { iverksettingRepository.findByIdOrThrow(behandlingId) } returns iverksett

        task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString()))

        assertThat(requestSlot.captured.personIdenter).containsExactlyInAnyOrder("1", "2")
        assertThat(requestSlot.captured.perioder).containsExactly(
            Periode(LocalDate.of(1901, 1, 1), LocalDate.of(1901, 1, 31), false),
            Periode(LocalDate.of(1902, 1, 1), LocalDate.of(1902, 1, 31), false)
        )
        verify(exactly = 1) { infotrygdFeedClient.opprettPeriodeHendelse(any()) }
    }

    @Test
    internal fun `skal sende tom liste med perioder til infotrygd hvis det ikke finnes noen andeler`() {
        every { iverksettingRepository.findByIdOrThrow(behandlingId) } returns opprettData()

        task.doTask(Task(SendPerioderTilInfotrygdTask.TYPE, behandlingId.toString()))

        assertThat(requestSlot.captured.personIdenter).containsExactlyInAnyOrder("1", "2")
        assertThat(requestSlot.captured.perioder).isEmpty()
        verify(exactly = 1) { infotrygdFeedClient.opprettPeriodeHendelse(any()) }
    }

    private fun opprettData(vararg andelTilkjentYtelse: AndelTilkjentYtelse): Iverksett {
        val iverksett = opprettIverksettOvergangsstønad(behandlingId)
        val vedtak = iverksett.vedtak
        val tilkjentYtelse = vedtak.tilkjentYtelse
        val iverksettData = iverksett.copy(
            vedtak = vedtak.copy(
                tilkjentYtelse =
                tilkjentYtelse!!.copy(andelerTilkjentYtelse = andelTilkjentYtelse.toList())
            )
        )
        return Iverksett(behandlingId, iverksettData, iverksettData.behandling.eksternId)
    }
}
