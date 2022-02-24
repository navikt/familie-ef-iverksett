package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelseMedMetadata
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.ØkonomiUtils.utbetalingsperiodeForOpphør
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class ØkonomiUtilsTest {

    private val andelStartDato = LocalDate.of(2021, 3, 1)
    private val andelSluttDato = LocalDate.of(2021, 5, 31)

    private val andelStartDato2 = LocalDate.of(2021, 11, 1)
    private val andelSluttDato2 = LocalDate.of(2021, 12, 31)

    private val opphørsdatoFørAndeler = LocalDate.of(2021, 1, 1)
    private val opphørsdatoEtterAndeler = andelSluttDato.plusDays(1) // etter andel sitt dato

    @Nested
    inner class UtbetalingsperiodeForOpphør {

        @Nested
        inner class UtenTidligereTilkjentYtelse {

            @Test
            internal fun `uten opphørsdato skal ikke få opphørsdato`() {
                assertThat(testOpphørsdatoUtenTidligereTilkjentYtelse(andeler = emptyList())).isNull()
                assertThat(testOpphørsdatoUtenTidligereTilkjentYtelse(andeler = listOf(andelMedBeløp()))).isNull()
                assertThat(testOpphørsdatoUtenTidligereTilkjentYtelse(andeler = listOf(andelUtenBeløp()))).isNull()
            }

            @Test
            internal fun `kan ikke sende med opphørsdato når det ikke finnes en tidligere behandling`() {
                listOf(emptyList(), listOf(andelMedBeløp()), listOf(andelUtenBeløp())).forEach { andeler ->
                    assertThatThrownBy {
                        testOpphørsdatoUtenTidligereTilkjentYtelse(andeler = andeler, startdato = opphørsdatoFørAndeler)
                    }.hasMessageContaining("Kan ikke opphøre noe når det ikke finnes en tidligere behandling")
                }
                listOf(emptyList(), listOf(andelMedBeløp()), listOf(andelUtenBeløp())).forEach { andeler ->
                    assertThatThrownBy {
                        testOpphørsdatoUtenTidligereTilkjentYtelse(andeler = andeler, startdato = opphørsdatoEtterAndeler)
                    }.hasMessageContaining("Kan ikke opphøre noe når det ikke finnes en tidligere behandling")
                }
            }
        }

        @Nested
        inner class MedTidligereTilkjentYtelse {

            @Test
            internal fun `opphørsdato etter tidligere opphørsdato er ikke gyldig`() {
                listOf(emptyList(), listOf(andelMedBeløp()), listOf(andelUtenBeløp())).forEach { andeler ->
                    assertThatThrownBy {
                        test(andeler = andeler,
                             tidligereAndeler = andeler,
                             startdato = opphørsdatoFørAndeler,
                             tidligereStartDato = LocalDate.MIN)
                    }.hasMessageContaining("Nytt opphørsdato=2021-01-01 kan ikke være etter")
                }
            }

            @Test
            internal fun `mangler opphørsdato når det finnes tidligere opphørsdato er ikke gyldig`() {
                listOf(emptyList(), listOf(andelMedBeløp()), listOf(andelUtenBeløp())).forEach { andeler ->
                    assertThatThrownBy {
                        test(andeler = andeler,
                             tidligereAndeler = andeler,
                             startdato = null,
                             tidligereStartDato = LocalDate.MIN)
                    }.hasMessageContaining("Må ha med opphørsdato hvis man har tidligere opphørsdato")
                }
            }

            @Test
            internal fun `andeler er like - returnerer null`() {
                listOf(emptyList(), listOf(andelMedBeløp()), listOf(andelUtenBeløp())).forEach {
                    assertThat(test(andeler = it, tidligereAndeler = it)).isNull()
                }
            }

            @Test
            internal fun `andeler er like, uten beløp, med tidligere siste andel`() {
                assertThat(test(andeler = listOf(andelUtenBeløp()),
                                tidligereAndeler = listOf(andelUtenBeløp()),
                                sisteAndelIKjede = andelMedBeløp().copy(periodeId = 2, forrigePeriodeId = 1)).opphørsdato())
                        .isNull()
            }

            @Test
            internal fun `andeler fjernes med 0-beløp - returnerer null`() {
                assertThat(test(andeler = emptyList(), tidligereAndeler = listOf(andelUtenBeløp()))).isNull()
            }

            @Test
            internal fun `andeler fjernes setter opphør til andelen sitt startdato`() {
                assertThat(test(andeler = emptyList(), tidligereAndeler = listOf(andelMedBeløp())).opphørsdato())
                        .isEqualTo(andelStartDato)
            }

            @Test
            internal fun `beløp endrer seg returnerer opphørsdato`() {
                assertThat(test(andeler = listOf(andelMedBeløp(1)),
                                tidligereAndeler = listOf(andelMedBeløp(2))).opphørsdato())
                        .isEqualTo(andelStartDato)
            }

            @Test
            internal fun `fra beløp til uten tolkes som fjerning av andeler og skal returnere opphørsdato`() {
                assertThat(test(andeler = listOf(andelUtenBeløp()), tidligereAndeler = listOf(andelMedBeløp())).opphørsdato())
                        .isEqualTo(andelStartDato)
            }

            @Test
            internal fun `ny andel uten beløp før tidligere perioder returnerer opphørsdato`() {
                assertThat(test(andeler = listOf(andelUtenBeløp(),
                                                 andelMedBeløp(fra = andelStartDato2, til = andelSluttDato2)),
                                tidligereAndeler = listOf(andelMedBeløp(fra = andelStartDato2,
                                                                        til = andelSluttDato2))).opphørsdato())
                        .isEqualTo(andelStartDato)
            }

            @Test
            internal fun `opphørsdato før tidligere andeler returnerer opphørsdato`() {
                assertThat(test(andeler = listOf(andelUtenBeløp()),
                                startdato = opphørsdatoFørAndeler,
                                tidligereAndeler = listOf(andelMedBeløp())).opphørsdato())
                        .isEqualTo(opphørsdatoFørAndeler)
            }

            @Test
            internal fun `opphørsdato før tidligere andeler, og før tidligere opphørsdato returnerer opphørsdato`() {
                assertThat(test(andeler = listOf(andelUtenBeløp()),
                                startdato = opphørsdatoFørAndeler,
                                tidligereAndeler = listOf(andelMedBeløp()),
                                tidligereStartDato = opphørsdatoFørAndeler.plusDays(1)).opphørsdato())
                        .isEqualTo(opphørsdatoFørAndeler)
            }

            @Test
            internal fun `opphørsdato før tidligere andeler, men samme som tidligere opphørsdato returnerer null`() {
                listOf(andelMedBeløp(), andelUtenBeløp()).forEach {
                    assertThat(test(andeler = listOf(it),
                                    startdato = opphørsdatoFørAndeler,
                                    tidligereAndeler = listOf(it),
                                    tidligereStartDato = opphørsdatoFørAndeler))
                            .isNull()
                }
            }

            @Test
            internal fun `fra uten beløp til beløp trenger ikke å trigge opphørsdato`() {
                assertThat(test(andeler = listOf(andelMedBeløp()), tidligereAndeler = listOf(andelUtenBeløp())))
                        .isNull()
            }

            @Test
            internal fun `endring frem i tiden returnerer null`() {
                assertThat(test(andeler = listOf(andelMedBeløp(),
                                                 andelUtenBeløp(fra = andelStartDato2, til = andelSluttDato2)),
                                tidligereAndeler = listOf(andelMedBeløp())))
                        .isNull()
                assertThat(test(andeler = listOf(andelUtenBeløp(),
                                                 andelMedBeløp(fra = andelStartDato2, til = andelSluttDato2)),
                                tidligereAndeler = listOf(andelUtenBeløp())))
                        .isNull()

                assertThat(test(andeler = listOf(andelMedBeløp(fra = andelStartDato2, til = andelSluttDato2)),
                                tidligereAndeler = listOf(andelUtenBeløp())))
                        .isNull()
            }


            // TODO test med sisteAndelIKjede
            @Test
            internal fun `endringer i periode etter tidligere opphør`() {
                assertThat(test(andeler = listOf(andelMedBeløp()),
                                startdato = opphørsdatoFørAndeler,
                                tidligereAndeler = listOf(andelMedBeløp()),
                                tidligereStartDato = opphørsdatoFørAndeler))
                        .isNull()
            }

        }

        private fun test(andeler: List<AndelTilkjentYtelse>,
                         startdato: LocalDate? = null,
                         tidligereAndeler: List<AndelTilkjentYtelse> = emptyList(),
                         tidligereStartDato: LocalDate? = null,
                         sisteAndelIKjede: AndelTilkjentYtelse? = null): Utbetalingsperiode? {
            return utbetalingsperiodeForOpphør(opprettTilkjentYtelse(andeler = leggTilPeriodeIdPåTidligereAndeler(tidligereAndeler),
                                                                     startdato = tidligereStartDato,
                                                                     sisteAndelIKjede = sisteAndelIKjede),
                                               tilkjentYtelseMedMetadata(andeler, startdato))
        }

        fun Utbetalingsperiode?.opphørsdato(): LocalDate? = this?.opphør?.opphørDatoFom

        private fun testOpphørsdatoUtenTidligereTilkjentYtelse(andeler: List<AndelTilkjentYtelse>,
                                                               startdato: LocalDate? = null) =
                utbetalingsperiodeForOpphør(null, tilkjentYtelseMedMetadata(andeler, startdato))

        private fun leggTilPeriodeIdPåTidligereAndeler(tidligereAndeler: List<AndelTilkjentYtelse>): List<AndelTilkjentYtelse> {
            val utenNullandeler = tidligereAndeler.filterNot { it.erNull() }
            if (utenNullandeler.isEmpty()) return listOf(nullAndelTilkjentYtelse(UUID.randomUUID(), PeriodeId(1)))
            return utenNullandeler
                    .sortedBy { it.fraOgMed }
                    .mapIndexed { index, andel ->
                        andel.copy(periodeId = index + 1L,
                                   forrigePeriodeId = if (index == 0) null else index.toLong())
                    }
        }

        private fun tilkjentYtelseMedMetadata(andeler: List<AndelTilkjentYtelse>,
                                              startdato: LocalDate?) = opprettTilkjentYtelseMedMetadata(
                tilkjentYtelse = opprettTilkjentYtelse(andeler = andeler,
                                                       startdato = startdato))

        fun andelMedBeløp(beløp: Int = 1,
                          fra: LocalDate = andelStartDato,
                          til: LocalDate = andelSluttDato
        ) = lagAndelTilkjentYtelse(beløp = beløp,
                                   fraOgMed = fra,
                                   tilOgMed = til)

        fun andelUtenBeløp(
                fra: LocalDate = andelStartDato,
                til: LocalDate = andelSluttDato
        ) = lagAndelTilkjentYtelse(beløp = 0,
                                   fraOgMed = fra,
                                   tilOgMed = til)

    }
}