package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Brevdata
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(path = ["/api/iverksett"])
//@ProtectedWithClaims(issuer = "azuread")
class IverksettController(
    val lagreIverksettService: LagreIverksettService,
    val objectMapper: ObjectMapper
) {

    /**
     * TODO : Legg til multipart request med brev fra pdf
     */
    @PostMapping(value = ["/start"])
    fun iverksett(@RequestBody iverksettJson: IverksettJson) {
        lagreIverksettService.lagreIverksettJson(
            UUID.fromString(iverksettJson.behandlingId),
            objectMapper.writeValueAsString(iverksettJson),
            listOf(Brev("123456", Brevdata("mottaker", "saksbehandler", ByteArray(2000))))
        )
    }

}