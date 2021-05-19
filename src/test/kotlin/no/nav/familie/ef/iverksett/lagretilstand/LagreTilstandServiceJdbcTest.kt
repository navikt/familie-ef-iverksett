package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.Periodetype
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*


internal class LagreTilstandServiceJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreTilstandServiceJdbc: LagreTilstandServiceJdbc

    val behandlingsId = UUID.randomUUID()
    val journalpostId = UUID.randomUUID()

    @Test
    @Order(1)
    fun `lagre tilkjentytelse, forvent ingen unntak`() {
        val tilkjentYtelse = TilkjentYtelse(
            id = behandlingsId,
            utbetalingsoppdrag = null,
            andelerTilkjentYtelse = listOf(
                AndelTilkjentYtelse(
                    periodebeløp = Periodebeløp(
                        beløp = 100,
                        Periodetype.MÅNED,
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusMonths(1)
                    ),
                    periodeId = 1L,
                    forrigePeriodeId = 1L,
                    kildeBehandlingId = behandlingsId
                )
            )
        )
        lagreTilstandServiceJdbc.lagreTilkjentYtelseForUtbetaling(behandlingsId, tilkjentYtelse)
    }

    @Test
    @Order(2)
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        lagreTilstandServiceJdbc.oppdaterOppdragResultat(behandlingsId, oppdragResultat)
    }

    @Test
    @Order(3)
    fun `oppdater journalpost, forvent ingen unntak`() {
        lagreTilstandServiceJdbc.oppdaterJournalpostResultat(
            behandlingsId,
            JournalpostResultat(
                journalpostId = journalpostId.toString(),
                bestillingId = journalpostId.toString()
            )
        )
    }

    @Test
    @Order(4)
    fun `oppdater distribuerVedtaksbrev, forvent ingen unntak`() {
        lagreTilstandServiceJdbc.oppdaterDistribuerVedtaksbrevResultat(
            behandlingsId,
            DistribuerVedtaksbrevResultat(bestillingId = journalpostId.toString())
        )
    }
}