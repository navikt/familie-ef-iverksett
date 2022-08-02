package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class IverksettMotOppdragIntegrasjonsTest : ServerTest() {

    @Autowired
    lateinit var tilstandRepository: TilstandRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    private val behandlingid: UUID = UUID.randomUUID()
    private val førsteAndel = lagAndelTilkjentYtelse(
        beløp = 1000,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 1, 31)
    )
    private val iverksett =
        opprettIverksettOvergangsstønad(behandlingid, andeler = listOf(førsteAndel), startdato = førsteAndel.fraOgMed)

    @BeforeEach
    internal fun setUp() {
        iverksettingService.startIverksetting(iverksett, opprettBrev())
        iverksettMotOppdrag()
    }

    @Test
    internal fun `start iverksetting, forvent at andelerTilkjentYtelse er lik 1 og har periodeId 1`() {
        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingid)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
    }

    @Test
    internal fun `revurdering med en ny periode, forvent at den nye perioden har peker på den forrige`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksettOvergangsstønad(
            behandlingIdRevurdering,
            behandlingid,
            listOf(
                førsteAndel,
                lagAndelTilkjentYtelse(
                    beløp = 1000,
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().plusDays(15)
                )
            )
        )

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(1)
    }

    @Test
    internal fun `revurdering der beløpet på den første endres, og en ny legges til, forvent at den første perioden erstattes`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksettOvergangsstønad(
            behandlingIdRevurdering,
            behandlingid,
            listOf(
                førsteAndel.copy(beløp = 299),
                lagAndelTilkjentYtelse(
                    beløp = 1000,
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().plusDays(15)
                )
            )
        )

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(3)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(2)
    }

    @Test
    internal fun `iverksett med opphør, forventer beløp lik 0 og dato lik LocalDate MIN`() {
        val opphørBehandlingId = UUID.randomUUID()
        val startdato = førsteAndel.fraOgMed
        val iverksettMedOpphør =
            opprettIverksettOvergangsstønad(opphørBehandlingId, behandlingid, emptyList(), startdato = startdato)

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettMedOpphør, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(opphørBehandlingId)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().beløp).isEqualTo(0)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().fraOgMed).isEqualTo(LocalDate.MIN)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().tilOgMed).isEqualTo(LocalDate.MIN)
    }

    private fun iverksettMotOppdrag() {
        val tasks = taskRepository.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
    }
}
