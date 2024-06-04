package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.frittstående.AktivitetspliktInnhentingBrevUtil.brevDto
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevService
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class FrittståendeBrevServiceIntegrasjonsTest : ServerTest() {
    @Autowired
    lateinit var frittståendeBrevService: FrittståendeBrevService

    @Test
    fun `Skal ikke kunne opprette to task for samme fagsak-brev-skoleår `() {
        val eksternFagsakId = 123L

        val brevDto1 = brevDto(eksternFagsakId, 1L)
        val brevDto2 = brevDto(eksternFagsakId, 2L)

        frittståendeBrevService.opprettTask(brevDto1)
        val feil =
            assertThrows<ApiFeil> {
                frittståendeBrevService.opprettTask(brevDto2)
            }
        assertThat(feil.feil).contains("Skal ikke kunne opprette flere identiske brev til mottaker. Fagsak med eksternId=$eksternFagsakId")
    }
}
