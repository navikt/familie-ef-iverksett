package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.brev.Brevdistribusjonskonflikt
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultat
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevUtil.opprettFrittståendeBrev
import no.nav.familie.ef.iverksett.vedtakstatistikk.toJson
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.RekjørSenereException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.util.UUID

internal class DistribuerFrittståendeBrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val frittståendeBrevRepository = mockk<FrittståendeBrevRepository>()

    private val distribuerFrittståendeBrevTask =
        DistribuerFrittståendeBrevTask(frittståendeBrevRepository, journalpostClient)

    private val journalpostIdSlot = mutableListOf<String>()
    private val distribuerBrevResultatMapSlot = slot<DistribuerBrevResultatMap>()

    private fun mockDistribuerBrev(medFeil: Boolean = false) {
        every { journalpostClient.distribuerBrev(capture(journalpostIdSlot), any()) } answers {
            val journalpostId = firstArg<String>()
            if (medFeil && journalpostId == "journalpostId2") {
                error("Feilet")
            }
            "$journalpostId-bestillingId"
        }
    }

    @BeforeEach
    internal fun setUp() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev()
        justRun {
            frittståendeBrevRepository.oppdaterDistribuerBrevResultat(
                any(),
                capture(distribuerBrevResultatMapSlot)
            )
        }
    }

    @Test
    internal fun `skal ferdigstille task med bestillingsid ved Conflict exception`() {
        // TODO kommenter inn test når teting er ferdig! (mattis)
//        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev().copy(
//            journalpostResultat = JournalpostResultatMap(
//                mapOf(
//                    "222" to JournalpostResultat("journalpostId2")
//                )
//            )
//        )
//
//        mockDistribuerBrev()
//        every { journalpostClient.distribuerBrev(any(), any()) } throws ressursExceptionConflict("DetteErbestillingsId")
//
//        distribuerFrittståendeBrevTask.doTask((Task("", UUID.randomUUID().toString())))
//
//        val distribuerBrevResultatMapSlot = distribuerBrevResultatMapSlot.captured.map
//        val entries = distribuerBrevResultatMapSlot.entries.toList()
//
//        assertThat(entries[0].value.bestillingId).isEqualTo("DetteErbestillingsId")
    }

    @Test
    internal fun `skal hoppe over personer som er døde men feile tasken`() {
        val dødJournalpostId = "dødPersonJournalpostId"
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev().copy(
            journalpostResultat = JournalpostResultatMap(
                mapOf(
                    "dødPersonId" to JournalpostResultat(dødJournalpostId),
                    "222" to JournalpostResultat("journalpostId2")
                )
            )
        )

        mockDistribuerBrev()
        every { journalpostClient.distribuerBrev(dødJournalpostId, any()) } throws ressursExceptionGone()

        val throwable = Assertions.catchThrowable {
            distribuerFrittståendeBrevTask.doTask(Task("", UUID.randomUUID().toString()))
        }
        assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)
        val rekjørSenereException = throwable as RekjørSenereException
        assertThat(rekjørSenereException.triggerTid)
            .isBetween(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(8))
        assertThat(rekjørSenereException.årsak).startsWith("Dødsbo")

        verify(exactly = 2) { journalpostClient.distribuerBrev(any(), Distribusjonstype.VIKTIG) }
        verify(exactly = 1) { frittståendeBrevRepository.oppdaterDistribuerBrevResultat(any(), any()) }

        val journalpostresultat = distribuerBrevResultatMapSlot.captured.map
        val entries = journalpostresultat.entries.toList()

        assertThat(entries).hasSize(1)
        assertThat(entries[0].key).isEqualTo("journalpostId2")
        assertThat(entries[0].value.bestillingId).isEqualTo("journalpostId2-bestillingId")
    }

    @Test
    internal fun `skal distribuere frittstående brev med en mottaker`() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev().copy(
            journalpostResultat = JournalpostResultatMap(
                mapOf(
                    "111" to JournalpostResultat("journalpostId1"),
                    "222" to JournalpostResultat("journalpostId2")
                )
            )
        )
        mockDistribuerBrev()

        distribuerFrittståendeBrevTask.doTask((Task("", UUID.randomUUID().toString())))

        verify(exactly = 2) { journalpostClient.distribuerBrev(any(), Distribusjonstype.VIKTIG) }
        verify(exactly = 2) { frittståendeBrevRepository.oppdaterDistribuerBrevResultat(any(), any()) }

        val journalpostresultat = distribuerBrevResultatMapSlot.captured.map
        val entries = journalpostresultat.entries.toList()

        assertThat(entries).hasSize(2)
        assertThat(entries[0].key).isEqualTo("journalpostId1")
        assertThat(entries[0].value.bestillingId).isEqualTo("journalpostId1-bestillingId")

        assertThat(entries[1].key).isEqualTo("journalpostId2")
        assertThat(entries[1].value.bestillingId).isEqualTo("journalpostId2-bestillingId")
    }

    @Test
    internal fun `skal ikke legge inn distribuerBrevResultat som feiler og skal feile tasken`() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev().copy(
            journalpostResultat = JournalpostResultatMap(
                mapOf(
                    "111" to JournalpostResultat("journalpostId1"),
                    "222" to JournalpostResultat("journalpostId2")
                )
            )
        )

        mockDistribuerBrev(medFeil = true)

        Assertions.assertThatThrownBy {
            distribuerFrittståendeBrevTask.doTask(Task("", UUID.randomUUID().toString()))
        }.hasMessage("Feilet")

        verify(exactly = 2) { journalpostClient.distribuerBrev(any(), any()) }
        verify(exactly = 1) { frittståendeBrevRepository.oppdaterDistribuerBrevResultat(any(), any()) }

        val distribuerBrevResultat = distribuerBrevResultatMapSlot.captured.map
        val entries = distribuerBrevResultat.entries.toList()

        assertThat(entries).hasSize(1)
        assertThat(entries[0].key).isEqualTo("journalpostId1")
        assertThat(entries[0].value.bestillingId).isEqualTo("journalpostId1-bestillingId")
    }

    @Test
    internal fun `skal ikke distribuere allerede distribuerte brev`() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev().copy(
            journalpostResultat = JournalpostResultatMap(
                mapOf(
                    "111" to JournalpostResultat("tidligereDistribuertJournalpost"),
                    "222" to JournalpostResultat("journalpostId2")
                )
            ),
            distribuerBrevResultat = DistribuerBrevResultatMap(
                map = mapOf("tidligereDistribuertJournalpost" to DistribuerBrevResultat("alleredeDistribuertBestillingId"))
            )
        )

        mockDistribuerBrev()

        distribuerFrittståendeBrevTask.doTask(Task("", UUID.randomUUID().toString()))

        verify(exactly = 1) { journalpostClient.distribuerBrev(any(), any()) }
        verify(exactly = 1) { frittståendeBrevRepository.oppdaterDistribuerBrevResultat(any(), any()) }

        val distribuerBrevResultatMapSlot = distribuerBrevResultatMapSlot.captured.map
        val entries = distribuerBrevResultatMapSlot.entries.toList()

        assertThat(journalpostIdSlot).hasSize(1)
        assertThat(journalpostIdSlot[0]).isEqualTo("journalpostId2")

        assertThat(entries).hasSize(2)
        assertThat(entries[0].key).isEqualTo("tidligereDistribuertJournalpost")
        assertThat(entries[0].value.bestillingId).isEqualTo("alleredeDistribuertBestillingId")

        assertThat(entries[1].key).isEqualTo("journalpostId2")
        assertThat(entries[1].value.bestillingId).isEqualTo("journalpostId2-bestillingId")
    }

    private fun ressursExceptionGone() =
        RessursException(
            Ressurs.failure(""),
            HttpClientErrorException.create(HttpStatus.GONE, "", HttpHeaders(), byteArrayOf(), null)
        )

    private fun ressursExceptionConflict(bestillingsId: String) =
        RessursException(
            Ressurs.failure(""),
            HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "",
                HttpHeaders(),
                Brevdistribusjonskonflikt(
                    bestillingsId
                ).toJson().toByteArray(),
                null
            )
        )
}
