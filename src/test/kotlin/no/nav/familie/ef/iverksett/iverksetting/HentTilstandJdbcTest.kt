package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandJdbc
import no.nav.familie.ef.iverksett.util.IverksettResultatMockBuilder
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

internal class HentTilstandJdbcTest : ServerTest() {

    @Autowired
    private lateinit var tilstandJdbc: TilstandJdbc

    val behandlingId = UUID.randomUUID()
    val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        tilstandJdbc.opprettTomtResultat(behandlingId)
        tilstandJdbc.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = tilstandJdbc.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = tilstandJdbc.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }

    @Test
    fun `hent ekisterende journalpost resultat, forvent likhet og ingen unntak`() {
        val journalpostResultat = JournalpostResultat("123456789", LocalDateTime.now())
        tilstandJdbc.oppdaterJournalpostResultat(behandlingId, journalpostResultat)
        val hentetJournalpostResultat = tilstandJdbc.hentJournalpostResultat(behandlingId)
        assertThat(hentetJournalpostResultat).isEqualTo(journalpostResultat)
    }

    @Test
    fun `hent ikke-eksisterende journalpost resultat, forvent nullverdi i retur og ingen unntak`() {
        val hentetJournalpostResultat = tilstandJdbc.hentJournalpostResultat(UUID.randomUUID())
        assertThat(hentetJournalpostResultat).isEqualTo(null)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingsID`() {

        val resultat = IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .journalPostResultat()
                .vedtaksbrevResultat(behandlingId).build(behandlingId, tilkjentYtelse)
        tilstandJdbc.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        tilstandJdbc.oppdaterOppdragResultat(behandlingId, resultat.oppdragResultat!!)
        tilstandJdbc.oppdaterJournalpostResultat(behandlingId, resultat.journalpostResultat!!)
        tilstandJdbc.oppdaterDistribuerVedtaksbrevResultat(behandlingId, resultat.vedtaksbrevResultat!!)
        val iverksettResultat = tilstandJdbc.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat).isEqualTo(resultat)
    }
}