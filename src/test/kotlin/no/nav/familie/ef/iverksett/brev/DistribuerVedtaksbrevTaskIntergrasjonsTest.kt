package no.nav.familie.ef.iverksett.brev

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.config.JournalpostClientMock
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.Properties
import java.util.UUID
import jakarta.annotation.PostConstruct

class DistribuerVedtaksbrevTaskIntergrasjonsTest : ServerTest() {

    @Autowired
    private lateinit var iverksettResultatService: IverksettResultatService

    @Autowired
    private lateinit var journalpostClient: JournalpostClient

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    @Qualifier("mock-integrasjoner")
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var journalpostClientMock: JournalpostClientMock

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    var distribuerVedtaksbrevTask: DistribuerVedtaksbrevTask? = null

    @PostConstruct
    fun init() {
        distribuerVedtaksbrevTask = DistribuerVedtaksbrevTask(
            journalpostClient = journalpostClient,
            iverksettResultatService = iverksettResultatService,
            taskService = taskService,
        )
    }

    @Test
    fun `skal distribuere brev til alle brevmottakere`() {
        val behandlingId = UUID.randomUUID()

        val mottakerA = "mottakerA"
        val journalpostA = "journalpostA"
        val mottakerB = "mottakerB"
        val journalpostB = "journalpostB"

        settOppTilstandsrepository(behandlingId, listOf(Pair(mottakerA, journalpostA), Pair(mottakerB, journalpostB)))

        distribuerVedtaksbrevTask!!.doTask(
            Task(
                JournalførVedtaksbrevTask.TYPE,
                behandlingId.toString(),
                Properties(),
            ),
        )

        val distribuerVedtaksbrevResultat = iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId)
        assertThat(distribuerVedtaksbrevResultat).hasSize(2)
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostA)).isNotNull
        assertThat(distribuerVedtaksbrevResultat?.get(journalpostB)).isNotNull
    }

    @Test
    fun `skal oppdatere distribueringsresultat for brevmottaker som gikk ok, men ikke for mottaker som feilet, retry skal gå bra uten at vi distrubuerer dobbelt`() {
        val behandlingId = UUID.randomUUID()

        val mottakerJournalpostA = Pair("mottakerA", "journalpostA")
        val ugyldigMottakerJournalpostB = Pair("mottakerB", "SkalFeile")
        val gyldigMottakerJournalpostB = Pair("mottakerB", "journalpostB")
        val mottakerJournalpostC = Pair("mottakerC", "journalpostC")

        settOppTilstandsrepository(behandlingId, listOf(mottakerJournalpostA, ugyldigMottakerJournalpostB, mottakerJournalpostC))

        kjørTask(behandlingId)
        verifiserKallTilDokarkivMedIdent(mottakerJournalpostA.second, 1)
        verifiserKallTilDokarkivMedIdent(ugyldigMottakerJournalpostB.second, 1)
        verifiserKallTilDokarkivMedIdent(mottakerJournalpostC.second, 0)

        val distribuerVedtaksbrevResultat = iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId)
        assertThat(distribuerVedtaksbrevResultat).hasSize(1)
        assertThat(distribuerVedtaksbrevResultat?.get(mottakerJournalpostA.second)).isNotNull
        assertThat(distribuerVedtaksbrevResultat?.get(ugyldigMottakerJournalpostB.second)).isNull()
        assertThat(distribuerVedtaksbrevResultat?.get(mottakerJournalpostC.second)).isNull()

        nullstillIverksettResultat(behandlingId)
        settOppTilstandsrepository(behandlingId, listOf(mottakerJournalpostA, gyldigMottakerJournalpostB, mottakerJournalpostC))

        wireMockServer.resetRequests()
        kjørTask(behandlingId)

        verifiserKallTilDokarkivMedIdent(mottakerJournalpostA.second, 0)
        verifiserKallTilDokarkivMedIdent(gyldigMottakerJournalpostB.second, 1)
        verifiserKallTilDokarkivMedIdent(mottakerJournalpostC.second, 1)

        val retryResultat = iverksettResultatService.hentdistribuerVedtaksbrevResultat(behandlingId)
        assertThat(retryResultat).hasSize(3)
        assertThat(retryResultat?.get(mottakerJournalpostA.second)).isNotNull
        assertThat(retryResultat?.get(gyldigMottakerJournalpostB.second)).isNotNull
        assertThat(retryResultat?.get(mottakerJournalpostC.second)).isNotNull
    }

    private fun kjørTask(behandlingId: UUID) {
        try {
            distribuerVedtaksbrevTask!!.doTask(
                Task(
                    JournalførVedtaksbrevTask.TYPE,
                    behandlingId.toString(),
                    Properties(),
                ),
            )
        } catch (_: Exception) {
        }
    }

    private fun verifiserKallTilDokarkivMedIdent(journalpostId: String, antall: Int = 1) {
        wireMockServer.verify(
            antall,
            WireMock.postRequestedFor(WireMock.urlMatching(journalpostClientMock.distribuerPath()))
                .withRequestBody(WireMock.matchingJsonPath("$..journalpostId", WireMock.containing(journalpostId))),
        )
    }

    private fun settOppTilstandsrepository(behandlingId: UUID, mottakereMedJournalpost: List<Pair<String, String>>) {
        val resultat = iverksettResultatService.hentIverksettResultat(behandlingId)
        if (resultat == null) iverksettResultatService.opprettTomtResultat(behandlingId)

        mottakereMedJournalpost.forEach {
            iverksettResultatService.oppdaterJournalpostResultat(
                behandlingId = behandlingId,
                mottakerIdent = it.first,
                journalPostResultat = JournalpostResultat(it.second),
            )
        }
    }

    private fun nullstillIverksettResultat(behandlingId: UUID) {
        val mapSqlParameterSource = MapSqlParameterSource(mapOf("behandlingId" to behandlingId))
        namedParameterJdbcTemplate.update(
            "UPDATE iverksett_resultat SET journalpostresultat = null WHERE behandling_id = :behandlingId",
            mapSqlParameterSource,
        )
    }
}
