package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"])
//@ProtectedWithClaims(issuer = "azuread")
class IverksettController(
    val lagreIverksettService: LagreIverksettService,
    val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping(value = ["/iverksett"])
    fun iverksett(@RequestBody iverksettJson: IverksettJson) : HttpStatus {
        lagreIverksettService.lagreIverksettJson(objectMapper.writeValueAsString(iverksettJson)).mapLeft {
            logger.error("Kunne ikke iverksette request : ${iverksettJson}")
            return HttpStatus.INTERNAL_SERVER_ERROR
        }
        return HttpStatus.OK
    }

}