package no.nav.familie.ef.iverksett.util

import no.nav.familie.kontrakter.felles.objectMapper

fun Any.toJson(): String = objectMapper.writeValueAsString(this)