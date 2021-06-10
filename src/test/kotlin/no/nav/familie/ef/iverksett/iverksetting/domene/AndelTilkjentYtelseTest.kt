package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AndelTilkjentYtelseTest {

    @Test
    internal fun `utbetalingsgrad - skal runde riktig`() {
        assertThat(lagTY(beløp = 337,
                         inntektsreduksjon = 1000 - 337,
                         samordningsfradrag = 0).utbetalingsgrad())
                .withFailMessage("Skal runde opp x.7")
                .isEqualTo(34)

        assertThat(lagTY(beløp = 3349,
                         inntektsreduksjon = 10000 - 3349,
                         samordningsfradrag = 0).utbetalingsgrad())
                .withFailMessage("Skal runde ned under x.5")
                .isEqualTo(33)

        assertThat(lagTY(beløp = 555,
                         inntektsreduksjon = 1000 - 555,
                         samordningsfradrag = 0).utbetalingsgrad())
                .withFailMessage("Skal runde opp x.5")
                .isEqualTo(56)
    }

    @Test
    internal fun `utbetalingsgrad - skal kalkulere utbetalingsgrad`() {
        assertThat(lagTY(beløp = 400,
                         inntektsreduksjon = 300,
                         samordningsfradrag = 300).utbetalingsgrad())
                .isEqualTo(40)

        assertThat(lagTY(beløp = 400,
                         inntektsreduksjon = 0,
                         samordningsfradrag = 0).utbetalingsgrad())
                .isEqualTo(100)

        assertThat(lagTY(beløp = 1,
                         inntektsreduksjon = 50,
                         samordningsfradrag = 49).utbetalingsgrad())
                .isEqualTo(1)
    }

    @Test
    internal fun `erFullOvergangsstønad er false når inntektsreduksjon eller samordningsfradrag ikke er 0`() {
        assertThat(lagTY(1, 0, 0).erFullOvergangsstønad()).isTrue
        assertThat(lagTY(1, 1, 0).erFullOvergangsstønad()).isFalse
        assertThat(lagTY(1, 0, 1).erFullOvergangsstønad()).isFalse
    }

    private fun lagTY(beløp: Int, inntektsreduksjon: Int = 0, samordningsfradrag: Int = 0) =
            lagAndelTilkjentYtelse(beløp = beløp,
                                   periodetype = Periodetype.MÅNED,
                                   fraOgMed = LocalDate.now(),
                                   tilOgMed = LocalDate.now(),
                                   inntektsreduksjon = inntektsreduksjon,
                                   samordningsfradrag = samordningsfradrag)
}