package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.Properties
import java.util.UUID
import javax.annotation.PostConstruct

class DistribuerVedtaksbrevTaskIntergrasjonsTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepository: TilstandRepository

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var journalpostClient: JournalpostClient

    private val featureToggleService = mockk<FeatureToggleService>()


    var distribuerVedtaksbrevTask: DistribuerVedtaksbrevTask? = null

    @PostConstruct
    fun init() {
        distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(journalpostClient = journalpostClient,
                                                              tilstandRepository = tilstandRepository,
                                                              featureToggleService = featureToggleService)

        every { featureToggleService.isEnabled(any()) } returns true


    }

    @Test
    fun `skal distribuere brev til alle brevmottakere`() {
        val behandlingId = UUID.randomUUID()

        val mottakerA = "mottakerA"
        val journalpostA = "journalpostA"
        val mottakerB = "mottakerB"
        val journalpostB = "journalpostB"

        settOppTilstandsrepository(behandlingId, mottakerA, mottakerB, journalpostA, journalpostB)


        distribuerVedtaksbrevTask!!.doTask(Task(JournalførVedtaksbrevTask.TYPE,
                                                behandlingId.toString(),
                                                Properties()))


        val distribuerVedtaksbrevResultat = tilstandRepository.hentdistribuerVedtaksbrevResultatBrevmottakere(behandlingId)
        assertThat(distribuerVedtaksbrevResultat).hasSize(2)
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostA)).isNotNull
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostB)).isNotNull
    }

    private fun settOppTilstandsrepository(behandlingId: UUID,
                                           mottakerA: String,
                                           mottakerB: String,
                                           journalpostA: String,
                                           journalpostB: String
    ) {
        tilstandRepository.opprettTomtResultat(behandlingId)

        tilstandRepository.oppdaterJournalpostResultatBrevmottakere(behandlingId = behandlingId,
                                                                    mottakerIdent = mottakerA,
                                                                    journalPostResultat = JournalpostResultat(journalpostA))

        tilstandRepository.oppdaterJournalpostResultatBrevmottakere(behandlingId = behandlingId,
                                                                    mottakerIdent = mottakerB,
                                                                    journalPostResultat = JournalpostResultat(journalpostB))
    }

    @Test
    fun `skal oppdatere distribueringsresultat for brevmottaker som gikk ok, men ikke for mottaker som feilet`() {
        val behandlingId = UUID.randomUUID()

        val mottakerA = "mottakerA"
        val journalpostA = "journalpostA"
        val mottakerB = "mottakerB"
        val journalpostB = "SkalFeile"

        settOppTilstandsrepository(behandlingId, mottakerA, mottakerB, journalpostA, journalpostB)


        try {
            distribuerVedtaksbrevTask!!.doTask(Task(JournalførVedtaksbrevTask.TYPE,
                                                    behandlingId.toString(),
                                                    Properties()))
        } catch (_: Exception) {
        }


        val distribuerVedtaksbrevResultat = tilstandRepository.hentdistribuerVedtaksbrevResultatBrevmottakere(behandlingId)
        assertThat(distribuerVedtaksbrevResultat).hasSize(1)
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostA)).isNotNull
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostB)).isNull()
    }
}