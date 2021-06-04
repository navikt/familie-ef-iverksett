package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.ef.iverksett.behandlingstatistikk.BehandlingDvhUtil.Companion.byggBehandlingDVH
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.BehandlingStatistikkDto
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class BehandlingstatistikkService {

    fun sendBehandlingstatistikk(behandlingstatistikk: BehandlingStatistikkDto) {
        //if ikke finnes fra før -> mapping og lagre ned
        // else hente fra lager, oppdater og lagre ned
        //send til DVH
    }

    private fun mapTilBehandlingDVH(behandlingstatistikk: BehandlingStatistikkDto): BehandlingDVH {
        var builder = BehandlingDvhBuilder.Builder()
        return when (behandlingstatistikk.hendelse) {
            Hendelse.MOTTATT -> byggBehandlingDVH(behandlingstatistikk, builder)
            Hendelse.PÅBEGYNT -> byggBehandlingDVH(behandlingstatistikk, builder, ZonedDateTime.now())
            Hendelse.VEDTATT -> TODO()
            Hendelse.BESLUTTET -> TODO()
            Hendelse.FERDIG -> TODO()
        }
    }


}