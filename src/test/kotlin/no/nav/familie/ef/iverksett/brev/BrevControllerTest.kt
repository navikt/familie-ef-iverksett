package no.nav.familie.ef.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.util.opprettFrittståendeBrevDto
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BrevControllerTest {

    @Autowired
    lateinit var taskRepository: TaskRepository

    private val journalpostClient = mockk<JournalpostClient>()
    private val brevController = BrevController(journalpostClient)

    @Test
    internal fun `opprett frittstående brev (brev uten behandling) gir 200 OK`() {

        val journalpostId = "123456789"

        every { journalpostClient.arkiverDokument(any(), any()) } returns ArkiverDokumentResponse(
            journalpostId,
            true
        )

        every { journalpostClient.distribuerBrev(journalpostId) } returns "distribuerBrevResponse"

        val frittståendeBrevDto = opprettFrittståendeBrevDto()
        val response = brevController.distribuerFrittståendeBrev(frittståendeBrevDto)

        Assertions.assertThat(response.statusCode.value()).isEqualTo(200)
    }
}
