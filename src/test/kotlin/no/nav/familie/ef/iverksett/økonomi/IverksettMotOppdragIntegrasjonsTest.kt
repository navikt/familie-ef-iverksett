package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingService
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.tekniskopphor.IverksettTekniskOpphørTask
import no.nav.familie.ef.iverksett.tekniskopphor.TekniskOpphørController
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.NULL_DATO
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.TekniskOpphørDto
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
    lateinit var tekniskOpphørController: TekniskOpphørController

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    @Autowired
    lateinit var tekniskOpphørTask: IverksettTekniskOpphørTask

    val behandlingid = UUID.randomUUID()
    val forsteAndel = lagAndelTilkjentYtelse(
            beløp = 1000,
            fraOgMed = LocalDate.of(2021, 1, 1),
            tilOgMed = LocalDate.of(2021, 1, 31),
            periodetype = Periodetype.MÅNED)
    val iverksett = opprettIverksett(behandlingid, andeler = listOf(forsteAndel))

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

    @Test
    internal fun `iverksett med opphør, forventer beløp lik 0 og dato lik NULL_DATO`() {
        val opphørBehandlingId = UUID.randomUUID()
        val iverksettMedOpphør = opprettIverksett(opphørBehandlingId, behandlingid, emptyList())

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettMedOpphør, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(opphørBehandlingId)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().beløp).isEqualTo(0)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().fraOgMed).isEqualTo(NULL_DATO)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().tilOgMed).isEqualTo(NULL_DATO)
    }

    @Test
    internal fun `revurdering etter teknisk opphør, forventer en peker på forrige periode `() {
        val tekniskOpphørId = UUID.randomUUID()
        taskRepository.deleteAll()
        tekniskOpphørController.iverksettTekniskOpphor(opprettTekniskOpphørDto(tekniskOpphørId))
        iverksettTekniskOpphør()

        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksett(behandlingIdRevurdering,
                                                    tekniskOpphørId,
                                                    listOf(lagAndelTilkjentYtelse(
                                                            beløp = 1000,
                                                            fraOgMed = LocalDate.now(),
                                                            tilOgMed = LocalDate.now().plusDays(15),
                                                            periodetype = Periodetype.MÅNED)))

        taskRepository.deleteAll()
        iverksettingService.startIverksetting(iverksettRevurdering, opprettBrev())
        iverksettMotOppdrag()

        val tilkjentYtelseOpphør = tilstandRepository.hentTilkjentYtelse(tekniskOpphørId)!!
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse.first().beløp).isEqualTo(0)
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse.first().fraOgMed).isEqualTo(NULL_DATO)
        assertThat(tilkjentYtelseOpphør.andelerTilkjentYtelse.first().tilOgMed).isEqualTo(NULL_DATO)

        val tilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().forrigePeriodeId).isEqualTo(1)
    }

    private fun opprettTekniskOpphørDto(tekniskOpphørId: UUID): TekniskOpphørDto {
        return TekniskOpphørDto(forrigeBehandlingId = behandlingid,
                                saksbehandlerId = iverksett.vedtak.saksbehandlerId,
                                eksternBehandlingId = iverksett.behandling.eksternId,
                                stønadstype = iverksett.fagsak.stønadstype,
                                eksternFagsakId = iverksett.fagsak.eksternId,
                                personIdent = iverksett.søker.personIdent,
                                behandlingId = tekniskOpphørId,
                                vedtaksdato = iverksett.vedtak.vedtaksdato)
    }

    private fun iverksettMotOppdrag() {
        val tasks = taskRepository.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
    }

    private fun iverksettTekniskOpphør() {
        val tasks = taskRepository.findAll()
        assertThat(tasks).hasSize(1)
        tekniskOpphørTask.doTask(tasks.first())
    }
}