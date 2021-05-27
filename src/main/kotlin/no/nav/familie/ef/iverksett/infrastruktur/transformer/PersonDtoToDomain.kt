package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.infrastruktur.json.BarnDto
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonDto
import no.nav.familie.ef.iverksett.iverksett.domene.Barn
import no.nav.familie.ef.iverksett.iverksett.domene.Person

fun PersonDto.toDomain(): Person {
    return Person(this.personIdent, this.aktorId)
}

fun BarnDto.toDomain(): Barn {
    return Barn(
            personIdent = this.personIdent,
            termindato = this.termindato
    )
}