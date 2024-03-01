package no.nav.familie.ef.iverksett.felles.util

import no.nav.familie.ef.iverksett.felles.util.DatoUtil.dagensDatoMedTid
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DatoFormat {
    val DATE_FORMAT_NORSK = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val GOSYS_DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm")
}

object DatoUtil {
    fun dagensDatoMedTid(): LocalDateTime = LocalDateTime.now()

    fun dagensDato(): LocalDate = LocalDate.now()
}

fun dagensDatoNorskFormat(): String = LocalDate.now().format(DatoFormat.DATE_FORMAT_NORSK)

fun dagensDatoMedTidNorskFormat(): String = dagensDatoMedTid().format(DatoFormat.GOSYS_DATE_TIME)
