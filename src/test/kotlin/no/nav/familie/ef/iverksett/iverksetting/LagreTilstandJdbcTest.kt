package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandJdbc
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal class LagreTilstandJdbcTest : ServerTest() {

    @Autowired
    private lateinit var tilstandServiceJdbc: TilstandJdbc

    val behandlingsId = UUID.randomUUID()
    val journalpostId = UUID.randomUUID()

    val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)

    @BeforeEach
    fun beforeEach() {
       tilstandServiceJdbc.opprettTomtResultat(behandlingsId)
    }

    @Test
    fun `oppdater tilkjent ytelse, forvent ingen unntak`() {
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        tilstandServiceJdbc.oppdaterTilkjentYtelseForUtbetaling(behandlingsId, tilkjentYtelse)
    }

    @Test
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        tilstandServiceJdbc.oppdaterOppdragResultat(behandlingsId, oppdragResultat)
    }

    @Test
    fun `oppdater journalpost, forvent ingen unntak`() {
        tilstandServiceJdbc.oppdaterJournalpostResultat(
            behandlingsId,
            JournalpostResultat(
                journalpostId = journalpostId.toString()
            )
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev, forvent ingen unntak`() {
        tilstandServiceJdbc.oppdaterDistribuerVedtaksbrevResultat(
            behandlingsId,
            DistribuerVedtaksbrevResultat(bestillingId = "12345")
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev med feil behandlingId, forvent IllegalStateException`() {
        assertThrows<IllegalStateException> {
            tilstandServiceJdbc.oppdaterDistribuerVedtaksbrevResultat(
            UUID.randomUUID(),
            DistribuerVedtaksbrevResultat(bestillingId = journalpostId.toString())
        )}
    }
}