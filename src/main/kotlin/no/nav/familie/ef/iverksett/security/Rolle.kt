package no.nav.familie.ef.iverksett.security

enum class Rolle {
    FORVALTER,
    APPLICATION,
    SAKSBEHANDLER,
    VEILEDER,
    BESLUTTER,
    ;

    fun authority(): String = "ROLE_$name"
}
