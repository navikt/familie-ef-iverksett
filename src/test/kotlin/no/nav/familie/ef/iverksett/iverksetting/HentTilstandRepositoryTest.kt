package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandRepository
import no.nav.familie.ef.iverksett.tilbakekreving.tilOpprettTilbakekrevingRequest
import no.nav.familie.ef.iverksett.util.IverksettResultatMockBuilder
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
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

    private val behandlingId: UUID = UUID.randomUUID()
    private val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

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
        val journalpostResultat = IverksettResultatMockBuilder.Builder().journalPostResultat().build(behandlingId, tilkjentYtelse).journalpostResultat

        val (mottakerIdent, resultat) = journalpostResultat!!.entries.first()
        tilstandRepository.oppdaterJournalpostResultat(behandlingId, mottakerIdent, resultat)

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
        val (mottakerIdent, journalpostresultat) = resultat.journalpostResultat?.entries!!.first()
        tilstandRepository.oppdaterJournalpostResultat(behandlingId, mottakerIdent, journalpostresultat)
        val (journalpostId, vedtaksbrevResultat) = resultat.vedtaksbrevResultat?.entries!!.first()

        tilstandRepository.oppdaterDistribuerVedtaksbrevResultat(behandlingId, journalpostId, vedtaksbrevResultat)
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
    internal fun `hentTilkjentYtelse for flere oppdragIdn kaster feil hvis den ikke finner tilkjent ytelse for en behandling`() {
        val behandlingId2 = UUID.randomUUID()
        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)

        assertThat(catchThrowable { tilstandRepository.hentTilkjentYtelse(setOf(behandlingId, behandlingId2)) })
                .hasMessageContaining("=[$behandlingId2]")
    }

    @Test
    fun `lagre tilbakekrevingsresultat, hent IverksettResultat med tilbakekrevingsresultat`() {

        val iverksett = opprettIverksett(behandlingId)
        val opprettTilbakekrevingRequest = iverksett
                .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        val tilbakekrevingResultat = TilbakekrevingResultat(opprettTilbakekrevingRequest)

        tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId, iverksett.vedtak.tilkjentYtelse!!)
        tilstandRepository.oppdaterTilbakekrevingResultat(behandlingId, tilbakekrevingResultat)

        val hentetTilbakekrevingResultat = tilstandRepository.hentTilbakekrevingResultat(behandlingId)
        assertThat(hentetTilbakekrevingResultat!!).isEqualTo(tilbakekrevingResultat)

        val iverksettResultat = tilstandRepository.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat!!.tilkjentYtelseForUtbetaling).isNotNull
        assertThat(iverksettResultat.tilbakekrevingResultat).isEqualTo(tilbakekrevingResultat)
    }

    @Test
    fun `overskriv tomt (null) tilbakekrevingsresultat`() {

        val id = UUID.randomUUID()
        val iverksett = opprettIverksett(id)
        val opprettTilbakekrevingRequest = iverksett
                .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        val tilbakekrevingResultat = TilbakekrevingResultat(opprettTilbakekrevingRequest)

        assertThat(tilstandRepository.hentIverksettResultat(id)).isNull()
        tilstandRepository.opprettTomtResultat(id)
        assertThat(tilstandRepository.hentIverksettResultat(id)!!.tilbakekrevingResultat).isNull()

        tilstandRepository.oppdaterTilbakekrevingResultat(id, tilbakekrevingResultat)
        assertThat(tilstandRepository.hentTilbakekrevingResultat(id)!!)
                .isEqualTo(tilbakekrevingResultat)
    }

}