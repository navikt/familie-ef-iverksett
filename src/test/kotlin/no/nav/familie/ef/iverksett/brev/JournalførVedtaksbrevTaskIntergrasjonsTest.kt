package no.nav.familie.ef.iverksett.brev

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottaker
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.IdentType.PERSONIDENT
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.Properties
import javax.annotation.PostConstruct

class JournalførVedtaksbrevTaskIntergrasjonsTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepository: TilstandRepository

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var journalpostClient: JournalpostClient

    private val featureToggleService = mockk<FeatureToggleService>()


    var journalførVedtaksbrevTask: JournalførVedtaksbrevTask? = null

    @PostConstruct
    fun init() {
        journalførVedtaksbrevTask = JournalførVedtaksbrevTask(iverksettingRepository = iverksettingRepository,
                                                              journalpostClient = journalpostClient,
                                                              taskRepository = taskRepository,
                                                              tilstandRepository = tilstandRepository,
                                                              featureToggleService = featureToggleService)

        every { featureToggleService.isEnabled(any()) } returns true


    }

    @Test
    fun `skal oppdatere journalpostresultat for brevmottakere`() {
        val identA = "123"
        val iverksettMedBrevmottakere = iverksett.copy(vedtak =
                                                       iverksett.vedtak.copy(brevmottakere = Brevmottakere(
                                                               mottakere = listOf(
                                                                       Brevmottaker(ident = identA,
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = MottakerRolle.BRUKER),
                                                                       Brevmottaker(ident = "321",
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = MottakerRolle.VERGE)))))
        val behandlingId = iverksettMedBrevmottakere.behandling.behandlingId
        tilstandRepository.opprettTomtResultat(behandlingId)
        iverksettingRepository.lagre(
                behandlingId,
                iverksettMedBrevmottakere,
                opprettBrev()
        )

        journalførVedtaksbrevTask!!.doTask(Task(JournalførVedtaksbrevTask.TYPE,
                                                behandlingId.toString(),
                                                Properties()))

        val journalpostResultatBrevmottakere = tilstandRepository.hentJournalpostResultatBrevmottakere(behandlingId = behandlingId)

        assertThat(journalpostResultatBrevmottakere).hasSize(2)
        assertThat(journalpostResultatBrevmottakere?.get(identA)).isNotNull
    }

    @Test
    fun `skal oppdatere journalpostresultat for brevmottaker som gikk ok, men ikke for mottaker som feilet`() {
        val identA = "123"
        val identB = "SkalKasteFeil"
        val iverksettMedBrevmottakere = iverksett.copy(vedtak =
                                                       iverksett.vedtak.copy(brevmottakere = Brevmottakere(
                                                               mottakere = listOf(
                                                                       Brevmottaker(ident = identA,
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = MottakerRolle.BRUKER),
                                                                       Brevmottaker(ident = identB,
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = MottakerRolle.VERGE)))))
        val behandlingId = iverksettMedBrevmottakere.behandling.behandlingId
        tilstandRepository.opprettTomtResultat(behandlingId)
        iverksettingRepository.lagre(
                behandlingId,
                iverksettMedBrevmottakere,
                opprettBrev()
        )

        try {
            journalførVedtaksbrevTask!!.doTask(Task(JournalførVedtaksbrevTask.TYPE,
                                                    behandlingId.toString(),
                                                    Properties()))
        } catch (_: Exception){ }


        val journalpostResultatBrevmottakere = tilstandRepository.hentJournalpostResultatBrevmottakere(behandlingId = behandlingId)

        assertThat(journalpostResultatBrevmottakere).hasSize(1)
        assertThat(journalpostResultatBrevmottakere?.get(identA)).isNotNull
        assertThat(journalpostResultatBrevmottakere?.get(identB)).isNull()
    }

    companion object {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
    }

}