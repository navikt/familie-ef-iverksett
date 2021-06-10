package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.kalkulerUtbetalingsgrad
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ØkonomiUtilsTest {

    @Test
    internal fun `skal runde riktig`() {
        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 337,
                                                 inntektsreduksjon = 1000 - 337,
                                                 samordningsfradrag = 0)))
                .withFailMessage("Skal runde opp x.7")
                .isEqualTo(34)

        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 3349,
                                                 inntektsreduksjon = 10000 - 3349,
                                                 samordningsfradrag = 0)))
                .withFailMessage("Skal runde ned under x.5")
                .isEqualTo(33)

        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 555,
                                                 inntektsreduksjon = 1000 - 555,
                                                 samordningsfradrag = 0)))
                .withFailMessage("Skal runde opp x.5")
                .isEqualTo(56)
    }

    @Test
    internal fun `skal kalkulere utbetalingsgrad`() {
        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 400,
                                                 inntektsreduksjon = 300,
                                                 samordningsfradrag = 300))).isEqualTo(40)

        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 400,
                                                 inntektsreduksjon = 0,
                                                 samordningsfradrag = 0))).isEqualTo(100)

        assertThat(kalkulerUtbetalingsgrad(lagTY(beløp = 1,
                                                 inntektsreduksjon = 50,
                                                 samordningsfradrag = 49))).isEqualTo(1)
    }

    private fun lagTY(beløp: Int, inntektsreduksjon: Int = 0, samordningsfradrag: Int = 0) =
            lagAndelTilkjentYtelse(beløp = beløp,
                                   periodetype = Periodetype.MÅNED,
                                   fraOgMed = LocalDate.now(),
                                   tilOgMed = LocalDate.now(),
                                   inntektsreduksjon = inntektsreduksjon,
                                   samordningsfradrag = samordningsfradrag)
}