package no.nav.familie.ef.iverksett.security

enum class Rolle {
    APPLICATION,
    SAKSBEHANDLER,
    BESLUTTER,
    ;

    fun authority(): String = "ROLE_$name"
}
