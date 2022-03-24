package no.nav.familie.ef.iverksett.patch

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettIverksett
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class PatchFagsakServiceTest : ServerTest() {

    @Autowired private lateinit var patchFagsakService: PatchFagsakService

    @Autowired private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    internal fun `skal oppdatere fagsakId og fagsakEksternId`() {
        val behandlingId1 = UUID.randomUUID()
        val behandlingEksternId1 = 11L
        val fagsakId1 = UUID.randomUUID()
        val fagsakEksternId1 = 1L

        val behandlingId2 = UUID.randomUUID()
        val behandlingEksternId2 = 22L
        iverksettingRepository.lagre(behandlingId1, lagIverksett(behandlingId1, behandlingId1, behandlingEksternId1), null)
        iverksettingRepository.lagre(behandlingId2, lagIverksett(behandlingId2, behandlingId2, behandlingEksternId2), null)

        patchFagsakService.patchIverksetting(false, listOf(PatchFagsakData(fagsakEksternId1, fagsakId1, behandlingId1, 0)))

        val oppdatertIverksett1 = iverksettingRepository.hent(behandlingId1)
        assertThat(oppdatertIverksett1.fagsak.fagsakId).isEqualTo(fagsakId1)
        assertThat(oppdatertIverksett1.fagsak.eksternId).isEqualTo(fagsakEksternId1)

        // Iverksett 2 blir ikke oppdatert
        val oppdatertIverksett2 = iverksettingRepository.hent(behandlingId2)
        assertThat(oppdatertIverksett2.fagsak.fagsakId).isEqualTo(behandlingId2)
        assertThat(oppdatertIverksett2.fagsak.eksternId).isEqualTo(behandlingEksternId2)
    }

    private fun lagIverksett(behandlingId: UUID, fagsakId: UUID, fagsakEksternId: Long): Iverksett {
        val iverksett = opprettIverksett(behandlingId, null, emptyList(), null, null)
        return iverksett.copy(fagsak = iverksett.fagsak.copy(fagsakId = fagsakId, eksternId = fagsakEksternId))
    }
}