package no.nav.familie.ef.iverksett.iverksetting

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.util.IverksettResultatMockBuilder
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettServiceTest {

    val iverksettResultatService = mockk<IverksettResultatService>()
    val taskRepository = mockk<TaskRepository>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()

    private var iverksettStatusService: IverksettingService = IverksettingService(
        taskRepository = taskRepository,
        iverksettResultatService = iverksettResultatService,
        iverksettingRepository = iverksettingRepository,
        oppdragClient = oppdragClient,
        featureToggleService = mockFeatureToggleService()
    )

    @Test
    fun `la IverksettResultat ha felt kun satt for tilkjent ytelse, forvent status SENDT_TIL_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettResultatService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.SENDT_TIL_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha tilkjent ytelse, oppdrag, og oppdragsresultat satt, forvent status FEILET_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettResultatService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
            .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.FEILET_MOT_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag med kvittert_ok, forvent status OK_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettResultatService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.OK_MOT_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for journalføring, forvent status JOURNALFØRT`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettResultatService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .journalPostResultat()
            .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.JOURNALFØRT)
    }

    @Test
    fun `la IverksettResultat ha felt for vedktasbrev ulik null, forvent status DISTRIBUERT`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettResultatService.hentIverksettResultat(behandlingsId) } returns IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .journalPostResultat()
            .vedtaksbrevResultat(behandlingsId).build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.OK)
    }
}
