package no.nav.familie.ef.iverksett.infrastruktur.exception

class IntegrationException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, e: Throwable?) : super(message, e)
}