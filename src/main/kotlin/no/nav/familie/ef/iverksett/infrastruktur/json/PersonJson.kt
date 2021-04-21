package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.domene.Person

data class PersonJson(
    val personIdent: String? = null,
    val aktorId: String? = null
)

fun PersonJson.toDomain() : Person {
    return Person(this.personIdent, this.aktorId)
}