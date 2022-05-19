package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.time.LocalDate

internal class UtbetalingsperiodeMalKtTest {

    @Test
    fun `skal mappe stønadstype til riktig satstype`() {
        assertThat(mapSatstype(StønadType.OVERGANGSSTØNAD)).isEqualTo(Utbetalingsperiode.SatsType.MND)
        assertThat(mapSatstype(StønadType.BARNETILSYN)).isEqualTo(Utbetalingsperiode.SatsType.MND)
        assertThat(mapSatstype(StønadType.SKOLEPENGER)).isEqualTo(Utbetalingsperiode.SatsType.ENG)
    }

    @Test
    internal fun `skal sette satstype til ENG for skolepenger`() {
        val utbetalingsperiode = lagUtbetalingsperiiode(StønadType.SKOLEPENGER)
        assertThat(utbetalingsperiode.satsType).isEqualTo(Utbetalingsperiode.SatsType.ENG)
    }

    @Test
    internal fun `skal sette satstype til MND for overgangsstønad`() {
        val utbetalingsperiode = lagUtbetalingsperiiode(StønadType.OVERGANGSSTØNAD)
        assertThat(utbetalingsperiode.satsType).isEqualTo(Utbetalingsperiode.SatsType.MND)
    }

    private fun lagUtbetalingsperiiode(stønadType: StønadType) =
            lagPeriodeFraAndel(andel = lagAndelTilkjentYtelse(beløp = 10,
                                                              fraOgMed = LocalDate.now(),
                                                              tilOgMed = LocalDate.now(),
                                                              periodeId = 1),
                               type = stønadType,
                               eksternBehandlingId = 1,
                               vedtaksdato = LocalDate.now(),
                               personIdent = "")
}