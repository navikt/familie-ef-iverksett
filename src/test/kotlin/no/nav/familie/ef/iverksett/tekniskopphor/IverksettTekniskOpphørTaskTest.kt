package no.nav.familie.ef.iverksett.tekniskopphor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelseMedMetadata
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.kontrakter.ef.iverksett.TekniskOpphørDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import javax.annotation.PostConstruct

internal class IverksettTekniskOpphørTaskTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepository: TilstandRepository

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var tekniskOpphørController: TekniskOpphørController

    var iverksettTekniskOpphørTask: IverksettTekniskOpphørTask? = null

    private val forrigeBehandlingId: UUID = UUID.randomUUID()
    private val tekniskOpphørBehandlingId: UUID = UUID.randomUUID()

    private val oppdragClient = mockk<OppdragClient>()
    private val andelTilkjentYtelse = opprettAndelTilkjentYtelse()
    private val tilkjentYtelse =
        opprettTilkjentYtelse(forrigeBehandlingId, andeler = listOf(andelTilkjentYtelse), startdato = andelTilkjentYtelse.fraOgMed)
    private val tilkjentYtelseMedUtbetalingsoppdrag =
        lagTilkjentYtelseMedUtbetalingsoppdrag(opprettTilkjentYtelseMedMetadata(forrigeBehandlingId, 1L, tilkjentYtelse))

    @PostConstruct
    fun init() {
        iverksettTekniskOpphørTask = IverksettTekniskOpphørTask(
            iverksettingRepository = iverksettingRepository,
            oppdragClient,
            taskRepository = taskRepository,
            tilstandRepository = tilstandRepository
        )
    }

    @Test
    fun skaIverksetteTekniskOpphør() {
        tilstandRepository.opprettTomtResultat(forrigeBehandlingId)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(forrigeBehandlingId, tilkjentYtelseMedUtbetalingsoppdrag)

        val utbetalingsoppdrag = slot<Utbetalingsoppdrag>()

        every {
            oppdragClient.iverksettOppdrag(capture(utbetalingsoppdrag))
        } returns "En random string"

        tekniskOpphørController.iverksettTekniskOpphor(
            TekniskOpphørDto(
                forrigeBehandlingId = forrigeBehandlingId,
                saksbehandlerId = "Sakbehandler 007",
                eksternBehandlingId = 0,
                stønadstype = StønadType.OVERGANGSSTØNAD,
                eksternFagsakId = 0,
                personIdent = "12345678",
                behandlingId = tekniskOpphørBehandlingId,
                vedtaksdato = andelTilkjentYtelse.fraOgMed
            )
        )

        val iverksettTekniskOpphørTask = taskRepository.findAll().first()
        assertThat(iverksettTekniskOpphørTask.type).isEqualTo(IverksettTekniskOpphørTask.TYPE)

        this.iverksettTekniskOpphørTask?.doTask(iverksettTekniskOpphørTask)

        assertThat(utbetalingsoppdrag.captured.kodeEndring).isEqualTo(Utbetalingsoppdrag.KodeEndring.ENDR)
        val opphør = utbetalingsoppdrag.captured.utbetalingsperiode.first().opphør
        assertThat(opphør).isNotNull
        assertThat(opphør!!.opphørDatoFom).isEqualTo(tilkjentYtelseMedUtbetalingsoppdrag.andelerTilkjentYtelse.minByOrNull { it.fraOgMed }!!.fraOgMed)
    }
}
