package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.tilbakekreving.tilOpprettTilbakekrevingRequest
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class LagreTilstandRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepositoryRepository: TilstandRepository

    private val behandlingsId: UUID = UUID.randomUUID()
    private val journalpostId: UUID = UUID.randomUUID()

    private val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)

    @BeforeEach
    fun beforeEach() {
        tilstandRepositoryRepository.opprettTomtResultat(behandlingsId)
    }

    @Test
    fun `oppdater tilkjent ytelse, forvent ingen unntak`() {
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        tilstandRepositoryRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingsId, tilkjentYtelse)
    }

    @Test
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        tilstandRepositoryRepository.oppdaterOppdragResultat(behandlingsId, oppdragResultat)
    }

    @Test
    fun `oppdater journalpost, forvent ingen unntak`() {
        tilstandRepositoryRepository.oppdaterJournalpostResultat(
            behandlingsId,
            "123",
            JournalpostResultat(
                journalpostId = journalpostId.toString()
            )
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev, forvent ingen unntak`() {
        tilstandRepositoryRepository.oppdaterDistribuerVedtaksbrevResultat(
            behandlingsId,
            "123",
            DistribuerVedtaksbrevResultat(bestillingId = "12345")
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev med feil behandlingId, forvent IllegalStateException`() {
        assertThrows<IllegalStateException> {
            tilstandRepositoryRepository.oppdaterDistribuerVedtaksbrevResultat(
                UUID.randomUUID(),
                "123",
                DistribuerVedtaksbrevResultat(bestillingId = journalpostId.toString())
            )
        }
    }

    @Test
    fun `oppdater tilbakekrevingsresultat, forvent ingen unntak`() {

        val opprettTilbakekrevingRequest = opprettIverksettOvergangsstønad(behandlingsId)
            .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        tilstandRepositoryRepository.oppdaterTilbakekrevingResultat(
            behandlingsId,
            TilbakekrevingResultat(opprettTilbakekrevingRequest)
        )
    }
}
