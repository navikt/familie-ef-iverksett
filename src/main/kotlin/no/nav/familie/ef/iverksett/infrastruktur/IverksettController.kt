package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.iverksett.tjeneste.IverksettService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(path = ["/api"])
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(val iverksettService: IverksettService) {

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE], path = ["/iverksett"])
    fun iverksettdummy(
        @RequestPart("iverksett") iverksettJson: IverksettJson,
        @RequestPart("brevdataPdf") brevdataPdf: MultipartFile
    ) {

    }

}