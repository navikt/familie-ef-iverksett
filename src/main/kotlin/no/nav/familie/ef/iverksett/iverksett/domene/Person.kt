package no.nav.familie.ef.iverksett.iverksett.domene

import java.time.LocalDate

data class Person(
    val personIdent: String? = null,
    val aktorId: String? = null
)

data class Barn(
        val personIdent: String? = null,
        val termindato: LocalDate? = null
)