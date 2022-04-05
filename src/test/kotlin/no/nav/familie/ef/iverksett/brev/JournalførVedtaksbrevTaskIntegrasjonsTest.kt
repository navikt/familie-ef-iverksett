package no.nav.familie.ef.iverksett.brev

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.config.JournalpostClientMock
import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottaker
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.copy
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.IdentType.PERSONIDENT
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.BRUKER
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.VERGE
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.Properties
import java.util.UUID
import javax.annotation.PostConstruct

class JournalførVedtaksbrevTaskIntegrasjonsTest : ServerTest() {

    @Autowired private lateinit var tilstandRepository: TilstandRepository
    @Autowired private lateinit var iverksettingRepository: IverksettingRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var journalpostClient: JournalpostClient
    @Autowired private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired @Qualifier("mock-integrasjoner") lateinit var wireMockServer: WireMockServer
    @Autowired lateinit var journalpostClientMock: JournalpostClientMock

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
        val identB = "321"
        val iverksettMedBrevmottakere = iverksett.copy(vedtak =
                                                       iverksett.vedtak.copy(brevmottakere = Brevmottakere(
                                                               mottakere = listOf(
                                                                       Brevmottaker(ident = identA,
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = BRUKER),
                                                                       Brevmottaker(ident = identB,
                                                                                    navn = "Navn",
                                                                                    identType = PERSONIDENT,
                                                                                    mottakerRolle = VERGE)))))
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

        val journalpostResultat = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)

        assertThat(journalpostResultat).hasSize(2)
        assertThat(journalpostResultat?.get(identA)).isNotNull
        assertThat(journalpostResultat?.get(identB)).isNotNull
    }

    @Test
    fun `skal oppdatere journalpostresultat for brevmottaker som gikk ok, men ikke for mottaker som feilet - retry skal gå fint uten at vi journalfører dobbelt`() {
        val brevmottakerA = Brevmottaker(ident = "123", navn = "N", identType = PERSONIDENT, mottakerRolle = BRUKER)
        val ugyldigBrevmottakerB =
                Brevmottaker(ident = "SkalKasteFeil", navn = "N", identType = PERSONIDENT, mottakerRolle = VERGE)
        val gyldigBrevmottakerB = Brevmottaker(ident = "345", navn = "N", identType = PERSONIDENT, mottakerRolle = VERGE)
        val brevmottakerC = Brevmottaker(ident = "234", navn = "N", identType = PERSONIDENT, mottakerRolle = VERGE)

        val ugyldigeBrevmottakere = listOf(brevmottakerA, ugyldigBrevmottakerB, brevmottakerC)
        val gyldigeBrevmottakere = listOf(brevmottakerA, gyldigBrevmottakerB, brevmottakerC)

        val vedtak = iverksett.vedtak
        val iverksettMedBrevmottakere = iverksett.copy(vedtak = vedtak.copy(brevmottakere = Brevmottakere(ugyldigeBrevmottakere)))
        val behandlingId = iverksettMedBrevmottakere.behandling.behandlingId

        // Sett iverksetting i gyldig tilstand
        tilstandRepository.opprettTomtResultat(behandlingId)
        iverksettingRepository.lagre(behandlingId, iverksettMedBrevmottakere, opprettBrev())

        kjørTask(behandlingId)

        verifiserKallTilDokarkivMedIdent(brevmottakerA.ident, 1)
        verifiserKallTilDokarkivMedIdent(ugyldigBrevmottakerB.ident, 1)

        val journalpostResultatMap = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)
        assertThat(journalpostResultatMap).hasSize(1)
        assertThat(journalpostResultatMap?.get(brevmottakerA.ident)).isNotNull
        assertThat(journalpostResultatMap?.get(ugyldigBrevmottakerB.ident)).isNull()
        assertThat(journalpostResultatMap?.get(brevmottakerC.ident)).isNull()

        // Nullstill iverksett og brev for å kunne rekjøre med gyldige verdier
        resettBrevOgIverksettMedGyldigeBrevmottakere(behandlingId, vedtak, gyldigeBrevmottakere)

        // Retryer, men nå med gyldige brevmottakere og nullstiller wireMockServer sine requests for å verifisere at identA ikke journalføres på nytt
        wireMockServer.resetRequests()
        kjørTask(behandlingId)

        val journalpostResultatMapRetry = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)

        assertThat(journalpostResultatMapRetry).hasSize(3)
        assertThat(journalpostResultatMapRetry?.get(brevmottakerA.ident)).isNotNull
        assertThat(journalpostResultatMapRetry?.get(gyldigBrevmottakerB.ident)).isNotNull
        assertThat(journalpostResultatMapRetry?.get(brevmottakerC.ident)).isNotNull

        verifiserKallTilDokarkivMedIdent(brevmottakerA.ident, 0)
        verifiserKallTilDokarkivMedIdent(gyldigBrevmottakerB.ident, 1)
        verifiserKallTilDokarkivMedIdent(brevmottakerC.ident, 1)

    }

    private fun resettBrevOgIverksettMedGyldigeBrevmottakere(behandlingId: UUID,
                                                             vedtak: Vedtaksdetaljer,
                                                             brevmottakere: List<Brevmottaker>) {
        val mapSqlParameterSource = MapSqlParameterSource(mapOf("behandlingId" to behandlingId))
        namedParameterJdbcTemplate.update("DELETE FROM brev WHERE behandling_id = :behandlingId", mapSqlParameterSource)
        namedParameterJdbcTemplate.update("DELETE FROM iverksett WHERE behandling_id = :behandlingId", mapSqlParameterSource)
        val iverksettMedGyldigeBrevmottakere =
                iverksett.copy(vedtak = vedtak.copy(brevmottakere = Brevmottakere(brevmottakere)))
        iverksettingRepository.lagre(behandlingId, iverksettMedGyldigeBrevmottakere, opprettBrev())
    }

    private fun kjørTask(behandlingId: UUID) {
        try {
            journalførVedtaksbrevTask!!.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))
        } catch (_: Exception) {
        }
    }

    private fun verifiserKallTilDokarkivMedIdent(ident: String, antall: Int = 1) {
        wireMockServer.verify(antall,
                              WireMock.postRequestedFor(WireMock.urlMatching(journalpostClientMock.journalføringPath()))
                                      .withRequestBody(WireMock.matchingJsonPath("$..id", WireMock.containing(ident))))
    }

    companion object {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
    }

}