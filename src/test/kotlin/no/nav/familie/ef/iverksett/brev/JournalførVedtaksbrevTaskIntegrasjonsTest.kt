package no.nav.familie.ef.iverksett.brev

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.config.JournalpostClientMock
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottaker
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.ef.iverksett.util.copy
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.IdentType.PERSONIDENT
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.BRUKER
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.VERGE
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
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

class JournalfÃ¸rVedtaksbrevTaskIntegrasjonsTest : ServerTest() {

    @Autowired private lateinit var tilstandRepository: TilstandRepository
    @Autowired private lateinit var iverksettingRepository: IverksettingRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var journalpostClient: JournalpostClient
    @Autowired private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired @Qualifier("mock-integrasjoner") lateinit var wireMockServer: WireMockServer
    @Autowired lateinit var journalpostClientMock: JournalpostClientMock

    var journalfÃ¸rVedtaksbrevTask: JournalfÃ¸rVedtaksbrevTask? = null

    @PostConstruct
    fun init() {
        journalfÃ¸rVedtaksbrevTask = JournalfÃ¸rVedtaksbrevTask(
            iverksettingRepository = iverksettingRepository,
            journalpostClient = journalpostClient,
            taskRepository = taskRepository,
            tilstandRepository = tilstandRepository,
        )
    }

    @Test
    fun `skal oppdatere journalpostresultat for brevmottakere`() {
        val identA = "123"
        val identB = "321"
        val iverksettMedBrevmottakere = iverksett.copy(
            vedtak =
            iverksett.vedtak.copy(
                brevmottakere = Brevmottakere(
                    mottakere = listOf(
                        Brevmottaker(
                            ident = identA,
                            navn = "Navn",
                            identType = PERSONIDENT,
                            mottakerRolle = BRUKER
                        ),
                        Brevmottaker(
                            ident = identB,
                            navn = "Navn",
                            identType = PERSONIDENT,
                            mottakerRolle = VERGE
                        )
                    )
                )
            )
        )
        val behandlingId = iverksettMedBrevmottakere.behandling.behandlingId
        tilstandRepository.opprettTomtResultat(behandlingId)
        iverksettingRepository.lagre(
            behandlingId,
            iverksettMedBrevmottakere,
            opprettBrev()
        )

        journalfÃ¸rVedtaksbrevTask!!.doTask(
            Task(
                JournalfÃ¸rVedtaksbrevTask.TYPE,
                behandlingId.toString(),
                Properties()
            )
        )

        val journalpostResultat = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)

        assertThat(journalpostResultat).hasSize(2)
        assertThat(journalpostResultat?.get(identA)).isNotNull
        assertThat(journalpostResultat?.get(identB)).isNotNull
    }

    @Test
    fun `skal oppdatere journalpostresultat for brevmottaker som gikk ok, men ikke for mottaker som feilet - retry skal gÃ¥ fint uten at vi journalfÃ¸rer dobbelt`() {
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

        kjÃ¸rTask(behandlingId)

        verifiserKallTilDokarkivMedIdent(brevmottakerA.ident, 1)
        verifiserKallTilDokarkivMedIdent(ugyldigBrevmottakerB.ident, 1)

        val journalpostResultatMap = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)
        assertThat(journalpostResultatMap).hasSize(1)
        assertThat(journalpostResultatMap?.get(brevmottakerA.ident)).isNotNull
        assertThat(journalpostResultatMap?.get(ugyldigBrevmottakerB.ident)).isNull()
        assertThat(journalpostResultatMap?.get(brevmottakerC.ident)).isNull()

        // Nullstill iverksett og brev for Ã¥ kunne rekjÃ¸re med gyldige verdier
        resettBrevOgIverksettMedGyldigeBrevmottakere(behandlingId, vedtak, gyldigeBrevmottakere)

        // Retryer, men nÃ¥ med gyldige brevmottakere og nullstiller wireMockServer sine requests for Ã¥ verifisere at identA ikke journalfÃ¸res pÃ¥ nytt
        wireMockServer.resetRequests()
        kjÃ¸rTask(behandlingId)

        val journalpostResultatMapRetry = tilstandRepository.hentJournalpostResultat(behandlingId = behandlingId)

        assertThat(journalpostResultatMapRetry).hasSize(3)
        assertThat(journalpostResultatMapRetry?.get(brevmottakerA.ident)).isNotNull
        assertThat(journalpostResultatMapRetry?.get(gyldigBrevmottakerB.ident)).isNotNull
        assertThat(journalpostResultatMapRetry?.get(brevmottakerC.ident)).isNotNull

        verifiserKallTilDokarkivMedIdent(brevmottakerA.ident, 0)
        verifiserKallTilDokarkivMedIdent(gyldigBrevmottakerB.ident, 1)
        verifiserKallTilDokarkivMedIdent(brevmottakerC.ident, 1)
    }

    private fun resettBrevOgIverksettMedGyldigeBrevmottakere(
        behandlingId: UUID,
        vedtak: Vedtaksdetaljer,
        brevmottakere: List<Brevmottaker>
    ) {
        val mapSqlParameterSource = MapSqlParameterSource(mapOf("behandlingId" to behandlingId))
        namedParameterJdbcTemplate.update("DELETE FROM brev WHERE behandling_id = :behandlingId", mapSqlParameterSource)
        namedParameterJdbcTemplate.update("DELETE FROM iverksett WHERE behandling_id = :behandlingId", mapSqlParameterSource)
        val iverksettMedGyldigeBrevmottakere =
            iverksett.copy(vedtak = vedtak.copy(brevmottakere = Brevmottakere(brevmottakere)))
        iverksettingRepository.lagre(behandlingId, iverksettMedGyldigeBrevmottakere, opprettBrev())
    }

    private fun kjÃ¸rTask(behandlingId: UUID) {
        try {
            journalfÃ¸rVedtaksbrevTask!!.doTask(Task(JournalfÃ¸rVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))
        } catch (_: Exception) {
        }
    }

    private fun verifiserKallTilDokarkivMedIdent(ident: String, antall: Int = 1) {
        wireMockServer.verify(
            antall,
            WireMock.postRequestedFor(WireMock.urlMatching(journalpostClientMock.journalfÃ¸ringPath()))
                .withRequestBody(WireMock.matchingJsonPath("$..id", WireMock.containing(ident)))
        )
    }

    companion object {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
    }
}
