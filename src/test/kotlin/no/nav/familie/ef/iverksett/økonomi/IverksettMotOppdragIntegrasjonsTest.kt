package no.nav.familie.ef.iverksett.økonomi

import io.mockk.every
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class IverksettMotOppdragIntegrasjonsTest : ServerTest() {

    @Autowired
    lateinit var oppdragClient: OppdragClient

    @Autowired
    lateinit var tilstandRepository: TilstandRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    val behandlingid = UUID.randomUUID()
    val forsteAndel = lagAndelTilkjentYtelse(
            beløp = 1000,
            fraOgMed = LocalDate.of(2021, 1, 1),
            tilOgMed = LocalDate.of(2021, 1, 31),
            periodetype = Periodetype.MÅNED)
    val iverksett = opprettIverksett(behandlingid, andeler = listOf(forsteAndel))

    @BeforeEach
    internal fun setUp() {
        every { oppdragClient.iverksettOppdrag(any()) } returns ""
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
        val iverksettRevurdering = opprettIverksett(behandlingIdRevurdering,
                                                    behandlingid,
                                                    listOf(forsteAndel,
                                                           lagAndelTilkjentYtelse(
                                                                   beløp = 1000,
                                                                   fraOgMed = LocalDate.now(),
                                                                   tilOgMed = LocalDate.now().plusDays(15),
                                                                   periodetype = Periodetype.MÅNED)))

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
    internal fun `revurdering med der beløpet på den første endres, samt en ny legges til, forvent at den første perioden erstattes`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksett(behandlingIdRevurdering,
                                                    behandlingid,
                                                    listOf(forsteAndel.copy(beløp = 299),
                                                           lagAndelTilkjentYtelse(
                                                                   beløp = 1000,
                                                                   fraOgMed = LocalDate.now(),
                                                                   tilOgMed = LocalDate.now().plusDays(15),
                                                                   periodetype = Periodetype.MÅNED)))

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(3)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(2)

    }

    private fun iverksettMotOppdrag() {
        val tasks = taskRepository.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
    }
}