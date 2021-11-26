package no.nav.familie.ef.iverksett.oppgave

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OppgaveServiceTest {

    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val oppgaveService = OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository)

    @Test
    fun `skalOppretteVurderHendelsOppgave for innvilget førstegangsbehandling, forvent true`() {
        val iverksett = opprettIverksett(UUID.randomUUID())
        assertThat(oppgaveService.skalOppretteVurderHendelsOppgave(iverksett)).isTrue()
    }

    @Test
    fun `skalOppretteVurderHendelsOppgave for teknisk opphør, forvent true`() {
        var iverksettMock = mockk<Iverksett>()
        every {  iverksettMock.behandling.behandlingType } returns BehandlingType.TEKNISK_OPPHØR
        assertThat(oppgaveService.skalOppretteVurderHendelsOppgave(iverksettMock)).isTrue()
    }

    private fun iverksett(behandlingType : BehandlingType) : Iverksett {

    }
}