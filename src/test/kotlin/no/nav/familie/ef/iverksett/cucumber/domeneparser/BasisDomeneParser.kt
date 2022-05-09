package no.nav.familie.ef.iverksett.cucumber.domeneparser

import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val norskDatoFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val norskÅrMånedFormatter = DateTimeFormatter.ofPattern("MM.yyyy")
val isoDatoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val isoÅrMånedFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun parseDato(domenebegrep: Domenenøkkel, rad: Map<String, String>): LocalDate {
    val dato = rad[domenebegrep.nøkkel]!!
    return parseDato(dato)
}

fun parseValgfriDato(domenebegrep: Domenenøkkel, rad: Map<String, String?>): LocalDate? {
    return parseValgfriDato(domenebegrep, rad)
}

fun parseÅrMåned(domenebegrep: Domenenøkkel, rad: Map<String, String?>): YearMonth {
    return parseValgfriÅrMåned(domenebegrep, rad)!!
}

fun parseValgfriÅrMåned(domenebegrep: Domenenøkkel, rad: Map<String, String?>): YearMonth? {
    return parseValgfriÅrMåned(valgfriVerdi(domenebegrep, rad))
}

fun parseString(domenebegrep: Domenenøkkel, rad: Map<String, String>): String {
    return verdi(domenebegrep, rad)
}

fun parseValgfriString(domenebegrep: Domenenøkkel, rad: Map<String, String>): String? {
    return valgfriVerdi(domenebegrep, rad)
}

fun parseBooleanMedBooleanVerdi(domenebegrep: Domenenøkkel, rad: Map<String, String>): Boolean {
    val verdi = verdi(domenebegrep, rad)

    return when (verdi) {
        "true" -> true
        else -> false
    }
}

fun parseBooleanJaIsTrue(domenebegrep: Domenenøkkel, rad: Map<String, String>): Boolean {
    return when (valgfriVerdi(domenebegrep, rad)) {
        "Ja" -> true
        else -> false
    }
}

fun parseBoolean(domenebegrep: Domenenøkkel, rad: Map<String, String>): Boolean {
    val verdi = verdi(domenebegrep, rad)

    return when (verdi) {
        "Ja" -> true
        else -> false
    }
}

fun parseValgfriBoolean(domenebegrep: Domenenøkkel, rad: Map<String, String?>): Boolean? {

    val verdi = rad[domenebegrep.nøkkel]
    if (verdi == null || verdi == "") {
        return null
    }

    return when (verdi) {
        "Ja" -> true
        "Nei" -> false
        else -> null
    }
}

private fun parseDato(dato: String): LocalDate {
    return if (dato.contains(".")) {
        LocalDate.parse(dato, norskDatoFormatter)
    } else {
        LocalDate.parse(dato, isoDatoFormatter)
    }
}

fun parseValgfriDato(domenebegrep: String, rad: Map<String, String?>): LocalDate? {
    val verdi = rad[domenebegrep]
    if (verdi == null || verdi == "") {
        return null
    }

    return if (verdi.contains(".")) {
        LocalDate.parse(verdi, norskDatoFormatter)
    } else {
        LocalDate.parse(verdi, isoDatoFormatter)
    }
}

private fun parseValgfriÅrMåned(verdi: String?): YearMonth? {
    if (verdi == null || verdi == "") {
        return null
    }

    return parseÅrMåned(verdi)
}

fun parseÅrMåned(verdi: String): YearMonth {
    return if (verdi.contains(".")) {
        YearMonth.parse(verdi, norskÅrMånedFormatter)
    } else {
        YearMonth.parse(verdi, isoÅrMånedFormatter)
    }
}

fun verdi(nøkkel: Domenenøkkel, rad: Map<String, String>): String {
    val verdi = rad[nøkkel.nøkkel]

    if (verdi == null || verdi == "") {
        throw java.lang.RuntimeException("Fant ingen verdi for $nøkkel")
    }

    return verdi
}

fun valgfriVerdi(nøkkel: Domenenøkkel, rad: Map<String, String?>): String? {
    return rad[nøkkel.nøkkel]
}

fun parseInt(domenebegrep: Domenenøkkel, rad: Map<String, String>): Int {
    val verdi = verdi(domenebegrep, rad)

    return Integer.parseInt(verdi)
}

fun parseBigDecimal(domenebegrep: Domenenøkkel, rad: Map<String, String>): BigDecimal {
    val verdi = verdi(domenebegrep, rad)
    return verdi.toBigDecimal()
}

fun parseDouble(domenebegrep: Domenenøkkel, rad: Map<String, String>): Double {
    val verdi = verdi(domenebegrep, rad)
    return verdi.toDouble()
}

fun parseValgfriDouble(domenebegrep: Domenenøkkel, rad: Map<String, String>): Double? {
    return valgfriVerdi(domenebegrep, rad)?.toDouble() ?: return null
}

fun parseValgfriInt(domenebegrep: Domenenøkkel, rad: Map<String, String>): Int? {
    valgfriVerdi(domenebegrep, rad) ?: return null

    return parseInt(domenebegrep, rad)
}

fun parseValgfriIntRange(domenebegrep: Domenenøkkel, rad: Map<String, String>): Pair<Int, Int>? {
    val verdi = valgfriVerdi(domenebegrep, rad) ?: return null

    return Pair(Integer.parseInt(verdi.split("-").first()),
                Integer.parseInt(verdi.split("-").last()))
}

fun parsePeriodetype(rad: Map<String, String>): Periodetype? {
    val verdi = valgfriVerdi(TilkjentYtelseDomenebegrep.PERIODETYPE, rad) ?: return null
    return Periodetype.valueOf(verdi)
}

inline fun <reified T : Enum<T>> parseValgfriEnum(domenebegrep: Domenenøkkel, rad: Map<String, String>): T? {
    val verdi = valgfriVerdi(domenebegrep, rad) ?: return null
    return enumValueOf<T>(verdi.uppercase())
}

inline fun <reified T : Enum<T>> parseEnum(domenebegrep: Domenenøkkel, rad: Map<String, String>): T {
    return enumValueOf(verdi(domenebegrep, rad))
}