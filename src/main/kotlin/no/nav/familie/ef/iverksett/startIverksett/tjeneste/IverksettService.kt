package no.nav.familie.ef.iverksett.startIverksett.tjeneste

import no.nav.familie.ef.iverksett.domene.Iverksett
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IverksettService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun dummyIverksett(iverksett: Iverksett): Iverksett {
        return iverksett
    }
}