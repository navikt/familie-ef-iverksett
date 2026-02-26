package no.nav.familie.ef.iverksett.økonomi.grensesnitt

import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.util.VirkedagerProvider
import java.time.LocalDate
import java.time.LocalDateTime

data class GrensesnittavstemmingDto(
    val stønadstype: StønadType,
    val fraDato: LocalDate,
    val triggerTid: LocalDateTime? = null,
)

fun GrensesnittavstemmingDto.tilTask(): Task {
    val nesteVirkedag: LocalDateTime = triggerTid ?: VirkedagerProvider.nesteVirkedag(fraDato).atTime(8, 0)
    val payload =
        jsonMapper.writeValueAsString(
            GrensesnittavstemmingPayload(
                fraDato = this.fraDato,
                stønadstype = this.stønadstype,
            ),
        )

    return Task(
        type = GrensesnittavstemmingTask.TYPE,
        payload = payload,
        triggerTid = nesteVirkedag,
    )
}
