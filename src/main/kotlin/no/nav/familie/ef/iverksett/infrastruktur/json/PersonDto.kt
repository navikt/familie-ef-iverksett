package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Barn
import no.nav.familie.ef.iverksett.domene.Person
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
        val terminDato: LocalDate? = null
)

fun BarnDto.toDomain(): Barn {
    return Barn(
            personIdent = this.personIdent,
            terminDato = this.terminDato
    )
}