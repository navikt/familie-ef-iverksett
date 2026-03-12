@file:Suppress("ArrayInDataClass") // Trenger ikke implementere equals/hashCode for ren DTO-klasse

package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.iverksett.IverksettDto

data class IverksettMedBrevRequest(
    val iverksettDto: IverksettDto,
    val fil: ByteArray,
)
