package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.YearMonth
import java.util.UUID

class IverksettMotOppdragIntegrasjonsTest : ServerTest() {
    @Autowired
    lateinit var iverksettResultatService: IverksettResultatService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    private val behandlingid: UUID = UUID.randomUUID()
    private val førsteAndel =
        lagAndelTilkjentYtelse(
            beløp = 1000,
            fraOgMed = YearMonth.of(2021, 1),
            tilOgMed = YearMonth.of(2021, 1),
        )
    private val iverksett =
        opprettIverksettOvergangsstønad(behandlingid, andeler = listOf(førsteAndel), startmåned = førsteAndel.periode.fom)

    @BeforeEach
    internal fun setUp() {
        iverksettingService.startIverksetting(iverksett, opprettBrev())
        iverksettMotOppdrag()
    }

    @Test
    internal fun `start iverksetting, forvent at andelerTilkjentYtelse er lik 1 og har periodeId 1`() {
        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingid)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering med en ny periode, forvent at den nye perioden har peker på den forrige`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering =
            opprettIverksettOvergangsstønad(
                behandlingIdRevurdering,
                behandlingid,
                listOf(
                    førsteAndel,
                    lagAndelTilkjentYtelse(
                        beløp = 1000,
                        fraOgMed = YearMonth.now(),
                        tilOgMed = YearMonth.now().plusMonths(1),
                    ),
                ),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering der beløpet på den første endres, og en ny legges til, forvent at den første perioden erstattes`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering =
            opprettIverksettOvergangsstønad(
                behandlingIdRevurdering,
                behandlingid,
                listOf(
                    førsteAndel.copy(beløp = 299),
                    lagAndelTilkjentYtelse(
                        beløp = 1000,
                        fraOgMed = YearMonth.now(),
                        tilOgMed = YearMonth.now().plusMonths(1),
                    ),
                ),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(1)
    }

    @Test
    internal fun `iverksett med opphør - sisteAndelIKjedene ivaretar `() {
        val opphørBehandlingId = UUID.randomUUID()
        val startmåned = førsteAndel.periode.fom
        val iverksettMedOpphør =
            opprettIverksettOvergangsstønad(opphørBehandlingId, behandlingid, emptyList(), startmåned = startmåned)

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettMedOpphør, opprettBrev())
        iverksettMotOppdrag()

        val behandlingIdRevurderingUtbetaling = UUID.randomUUID()
        val iverksettUtbetaling =
            iverksett.copy(
                behandling =
                    iverksett.behandling.copy(
                        behandlingId = behandlingIdRevurderingUtbetaling,
                        forrigeBehandlingId = opphørBehandlingId,
                    ),
            )
        iverksettingService.startIverksetting(
            iverksettUtbetaling,
            opprettBrev(),
        )
        iverksettMotOppdrag()

        val tilkjentYtelseOpphør = iverksettResultatService.hentTilkjentYtelse(opphørBehandlingId)!!
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse).hasSize(0)
        assertThat(tilkjentYtelseOpphør.sisteAndelIKjede?.periodeId).isEqualTo(0)

        val tilkjentYtelseUtbetaling = iverksettResultatService.hentTilkjentYtelse(behandlingIdRevurderingUtbetaling)!!
        assertThat(tilkjentYtelseUtbetaling.sisteAndelIKjede?.periodeId).isEqualTo(1)
        assertThat(tilkjentYtelseUtbetaling.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelseUtbetaling.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelseUtbetaling.andelerTilkjentYtelse.first().forrigePeriodeId).isEqualTo(0)
        assertThat(tilkjentYtelseUtbetaling.andelerTilkjentYtelse.first().beløp).isGreaterThan(0)
    }

    private fun iverksettMotOppdrag() {
        val tasks = taskService.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
        taskService.deleteAll(taskService.findAll())
    }
}
