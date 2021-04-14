package no.nav.familie.ef.iverksett.mottak.tjeneste

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MottakService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun test(): String {
        return ""
    }
}