package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.familie.kontrakter.felles.Månedsperiode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val grunnbeløpsperioder: List<Grunnbeløp> =
    listOf(
        Grunnbeløp(
            periode = Månedsperiode(YearMonth.parse("2023-05"), YearMonth.from(LocalDate.MAX)),
            grunnbeløp = 118_620.toBigDecimal(),
            perMnd = 9_885.toBigDecimal(),
            gjennomsnittPerÅr = 116_239.toBigDecimal(),
        ),
        Grunnbeløp(
            periode = Månedsperiode(YearMonth.parse("2022-05"), YearMonth.parse("2023-04")),
            grunnbeløp = 111_477.toBigDecimal(),
            perMnd = 9_290.toBigDecimal(),
            gjennomsnittPerÅr = 109_784.toBigDecimal(),
        ),
        Grunnbeløp(
            periode = Månedsperiode("2021-05" to "2022-04"),
            grunnbeløp = 106_399.toBigDecimal(),
            perMnd = 8_867.toBigDecimal(),
            gjennomsnittPerÅr = 104_716.toBigDecimal(),
        ),
    )
val nyesteGrunnbeløp = grunnbeløpsperioder.maxBy { it.periode }
val halvG = nyesteGrunnbeløp.grunnbeløp.divide(BigDecimal(2)).divide(BigDecimal(12)).setScale(0, RoundingMode.HALF_UP)
data class Grunnbeløp(
    val periode: Månedsperiode,
    val grunnbeløp: BigDecimal,
    val perMnd: BigDecimal,
    val gjennomsnittPerÅr: BigDecimal? = null,
)

fun LocalDate.norskFormat() = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

