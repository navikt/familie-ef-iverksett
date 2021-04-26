package no.nav.familie.ef.iverksett.domene

import no.nav.familie.ef.sak.sikkerhet.SikkerhetContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Sporbar(val opprettetAv: String = SikkerhetContext.hentSaksbehandler(),
                   val opprettetTid: LocalDateTime = SporbarUtils.now(),
                   val endret: Endret = Endret())

data class Endret(val endretAv: String = SikkerhetContext.hentSaksbehandler(),
                  val endretTid: LocalDateTime = SporbarUtils.now())

object SporbarUtils {

    fun now(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
}
