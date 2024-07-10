package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Barn
import no.nav.familie.ef.iverksett.iverksetting.domene.Person
import no.nav.familie.kontrakter.ef.iverksett.BarnDto
import no.nav.familie.kontrakter.ef.iverksett.PersonDto

fun PersonDto.toDomain(): Person = Person(this.personIdent)

fun BarnDto.toDomain(): Barn =
    Barn(
        personIdent = this.personIdent,
        termindato = this.termindato,
    )
