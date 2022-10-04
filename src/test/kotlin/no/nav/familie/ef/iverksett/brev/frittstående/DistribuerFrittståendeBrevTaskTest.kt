package no.nav.familie.ef.iverksett.brev.frittstående

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.brev.JournalpostClient
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevUtil.opprettFrittståendeBrev
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

internal class DistribuerFrittståendeBrevTaskTest {

    private val journalpostClient = mockk<JournalpostClient>()
    private val frittståendeBrevRepository = mockk<FrittståendeBrevRepository>()

    private val distribuerFrittståendeBrevTask = DistribuerFrittståendeBrevTask(frittståendeBrevRepository, journalpostClient)

    private val distribuerBrevResultatSlot = slot<DistribuerBrevResultatMap>()

    @BeforeEach
    internal fun setUp() {
        every { frittståendeBrevRepository.findByIdOrNull(any()) } returns opprettFrittståendeBrev()
        justRun { frittståendeBrevRepository.oppdaterDistribuerBrevResultat(any(), capture(distribuerBrevResultatSlot)) }
    }

    @Test
    internal fun `skal hoppe over personer som er døde men feile tasken`() {
        distribuerFrittståendeBrevTask.doTask(Task("", UUID.randomUUID().toString()))
    }
}