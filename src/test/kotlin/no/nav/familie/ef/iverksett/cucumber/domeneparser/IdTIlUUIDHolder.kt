package no.nav.familie.ef.iverksett.cucumber.domeneparser

import java.util.UUID

object IdTIlUUIDHolder {
    val behandlingIdTilUUID = (1..10).associateWith { UUID.randomUUID() }
    val tilkjentYtelseIdNummerTilUUID = (1..10).associateWith { UUID.randomUUID() }
}
