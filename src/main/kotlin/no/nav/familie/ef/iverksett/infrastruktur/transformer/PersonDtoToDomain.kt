package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Person
import no.nav.familie.kontrakter.ef.iverksett.BarnDto
import no.nav.familie.kontrakter.ef.iverksett.PersonDto

fun PersonDto.toDomain(): Person {
    return Person(this.personIdent)
}

fun BarnDto.toDomain(): Barn {
    return Barn(
            personIdent = this.personIdent,
            termindato = this.termindato
    )
}