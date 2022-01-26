package no.nav.familie.ef.iverksett.behandling

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BehandlingService(val iverksettingRepository: IverksettingRepository) {


    fun hentBeregnetInntektForBehandlingOgDato(eksternId: Long, dato: LocalDate): Int? {
        val iverksetting = iverksettingRepository.hentAvEksternId(eksternId)
        return iverksetting.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.firstOrNull {
            dato.isEqualOrAfter(it.fraOgMed) && dato.isEqualOrBefore(it.tilOgMed)
        }?.inntekt
    }
}

fun LocalDate.isEqualOrAfter(dato: LocalDate) = this.equals(dato) || this.isAfter(dato)
fun LocalDate.isEqualOrBefore(dato: LocalDate) = this.equals(dato) || this.isBefore(dato)