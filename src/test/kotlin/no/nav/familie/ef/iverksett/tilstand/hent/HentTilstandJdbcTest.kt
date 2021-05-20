package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandJdbc
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

internal class HentTilstandJdbcTest : ServerTest() {

    @Autowired
    private lateinit var hentTilstandJdbc: HentTilstandJdbc

    @Autowired
    private lateinit var lagreTilstandJdbc: LagreTilstandJdbc

    val behandlingId = UUID.randomUUID()
    val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        lagreTilstandJdbc.lagreTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = hentTilstandJdbc.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = hentTilstandJdbc.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }

    @Test
    fun `hent ekisterende journalpost resultat, forvent likhet og ingen unntak`() {
        val journalpostResultat = JournalpostResultat("123456789", LocalDateTime.now())
        lagreTilstandJdbc.oppdaterJournalpostResultat(behandlingId, journalpostResultat)
        val hentetJournalpostResultat = hentTilstandJdbc.hentJournalpostResultat(behandlingId)
        assertThat(hentetJournalpostResultat).isEqualTo(journalpostResultat)
    }

    @Test
    fun `hent ikke-eksisterende journalpost resultat, forvent nullverdi i retur og ingen unntak`() {
        val hentetJournalpostResultat = hentTilstandJdbc.hentJournalpostResultat(UUID.randomUUID())
        assertThat(hentetJournalpostResultat).isEqualTo(null)
    }
}