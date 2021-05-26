package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.Barn
import no.nav.familie.ef.iverksett.iverksett.Person
import java.time.LocalDate

data class PersonDto(
    val personIdent: String? = null,
    val aktorId: String? = null
)

fun PersonDto.toDomain(): Person {
    return Person(this.personIdent, this.aktorId)
}

data class BarnDto(
        val personIdent: String? = null,
        val termindato: LocalDate? = null
)

fun BarnDto.toDomain(): Barn {
    return Barn(
            personIdent = this.personIdent,
            termindato = this.termindato
    )
}