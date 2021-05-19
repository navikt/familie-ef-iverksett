package no.nav.familie.ef.iverksett.tilstand.hent

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.tilstand.lagre.LagreTilstandJdbc
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal class HentTilstandJdbcTest : ServerTest() {

    @Autowired
    private lateinit var hentTilstandJdbc: HentTilstandJdbc

    @Autowired
    private lateinit var lagreTilstandJdbc: LagreTilstandJdbc

    val behandlingId = UUID.randomUUID()
    val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)


    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        lagreTilstandJdbc.lagreTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        val hentetTilkjentYtelse = hentTilstandJdbc.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = hentTilstandJdbc.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }


}