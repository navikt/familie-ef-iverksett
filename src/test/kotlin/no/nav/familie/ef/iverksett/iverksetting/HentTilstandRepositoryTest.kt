package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.util.IverksettResultatMockBuilder
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

internal class HentTilstandRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepository: TilstandRepository

    val behandlingId = UUID.randomUUID()
    val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        tilstandRepository.opprettTomtResultat(behandlingId)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = tilstandRepository.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = tilstandRepository.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }

    @Test
    fun `hent ekisterende journalpost resultat, forvent likhet og ingen unntak`() {
        val journalpostResultat = JournalpostResultat("123456789", LocalDateTime.now())
        tilstandRepository.oppdaterJournalpostResultat(behandlingId, journalpostResultat)
        val hentetJournalpostResultat = tilstandRepository.hentJournalpostResultat(behandlingId)
        assertThat(hentetJournalpostResultat).isEqualTo(journalpostResultat)
    }

    @Test
    fun `hent ikke-eksisterende journalpost resultat, forvent nullverdi i retur og ingen unntak`() {
        val hentetJournalpostResultat = tilstandRepository.hentJournalpostResultat(UUID.randomUUID())
        assertThat(hentetJournalpostResultat).isEqualTo(null)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingsID`() {

        val resultat = IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .journalPostResultat()
                .vedtaksbrevResultat(behandlingId).build(behandlingId, tilkjentYtelse)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        tilstandRepository.oppdaterOppdragResultat(behandlingId, resultat.oppdragResultat!!)
        tilstandRepository.oppdaterJournalpostResultat(behandlingId, resultat.journalpostResultat!!)
        tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId, resultat.vedtaksbrevResultat!!)
        val iverksettResultat = tilstandRepository.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat).isEqualTo(resultat)
    }

    @Test
    internal fun `hent tilkjentytelser for flere oppdragIdn`() {
        val behandlingId2 = UUID.randomUUID()
        tilstandRepository.opprettTomtResultat(behandlingId2)

        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId2, tilkjentYtelse)

        val tilkjentYtelsePåBehandlingId = tilstandRepository.hentTilkjentYtelse(setOf(behandlingId, behandlingId2))

        assertThat(tilkjentYtelsePåBehandlingId).containsKeys(behandlingId, behandlingId2)
    }

    @Test
    internal fun `hent tilkjentytelser for flere oppdragIdn skal kaste feil hvis den ikke finner tilkjent ytelse for en behandling`() {
        val behandlingId2 = UUID.randomUUID()
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)

        assertThat(catchThrowable { tilstandRepository.hentTilkjentYtelse (setOf(behandlingId, behandlingId2)) })
                .hasMessageContaining("=[$behandlingId2]")
    }

}