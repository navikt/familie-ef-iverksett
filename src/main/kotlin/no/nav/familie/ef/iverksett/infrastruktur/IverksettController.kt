package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.infrastruktur.json.toJson
import no.nav.familie.ef.iverksett.iverksett.tjeneste.IverksettService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"])
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(val iverksettService: IverksettService) {

    @PostMapping(path = ["/iverksett"])
    fun iverksettdummy(iverksettJson: IverksettJson): IverksettJson {
        return iverksettService.dummyIverksett(iverksettJson.toDomain()).toJson()
    }

}

