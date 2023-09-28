package no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag

import no.nav.familie.felles.utbetalingsgenerator.domain.Fagsystem
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsperiode.SatsType
import no.nav.familie.felles.utbetalingsgenerator.domain.Ytelsestype
import java.time.LocalDateTime

object UtbetalingsgeneratorHelper {

    enum class FagsystemEF(
        override val kode: String,
        override val gyldigeSatstyper: Set<Ytelsestype>,
    ) : Fagsystem {

        OVERGANGSSTØNAD("EFOG", setOf(YtelsestypeEF.OVERGANGSSTØNAD)),
        SKOLEPENGER("EFSP", setOf(YtelsestypeEF.SKOLEPENGER)),
        BARNETILSYN("EFBT", setOf(YtelsestypeEF.BARNETILSYN)),
    }

    enum class YtelsestypeEF(
        override val klassifisering: String,
        override val satsType: SatsType = SatsType.MND,
    ) : Ytelsestype {

        OVERGANGSSTØNAD("EFOG"),
        BARNETILSYN("EFBT"),
        SKOLEPENGER("EFSP", SatsType.ENG),
    }

    data class KonsistensavstemmingUtbetalingsoppdrag(
        val avstemmingstidspunkt: LocalDateTime,
        val fagsystem: String,
        val utbetalingsoppdrag: List<Utbetalingsoppdrag>,
    )
}
