package no.nav.familie.ef.iverksett.mottak.tjeneste

import no.nav.familie.ef.iverksett.Vedtak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MottakService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun test(vedtak: Vedtak): String {
        return vedtak.toString()
    }
}