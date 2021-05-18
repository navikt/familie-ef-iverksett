package no.nav.familie.ef.iverksett.lagretilstand

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.Periodetype
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*


class LagreTilstandServiceJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreTilstandServiceJdbc: LagreTilstandServiceJdbc

    @Test
    fun `lagrer tilkjentytelse`() {
        val tilkjentYtelse = TilkjentYtelse(id = UUID.randomUUID(),
                                            utbetalingsoppdrag = null,
                                            andelerTilkjentYtelse = listOf(AndelTilkjentYtelse(periodebeløp = Periodebeløp(
                                                    utbetaltPerPeriode = 100,
                                                    Periodetype.MÅNED,
                                                    fraOgMed = LocalDate.now(),
                                                    tilOgMed = LocalDate.now().plusMonths(1)),
                                                                                               periodeId = 1L,
                                                                                               forrigePeriodeId = 1L,
                                                                                               kildeBehandlingId = UUID.randomUUID()))
        )
        lagreTilstandServiceJdbc.lagreTilkjentYtelseForUtbetaling(UUID.randomUUID().toString(), tilkjentYtelse)
    }
}