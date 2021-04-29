package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], path = ["/api/iverksett"])
//@ProtectedWithClaims(issuer = "azuread")
class IverksettController(
        val lagreIverksettService: LagreIverksettService,
        val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * TODO : Legg til multipart request med brev fra pdf
     */
    @PostMapping
    fun iverksett(@RequestPart("iverksettJson") iverksettJson: IverksettJson,
                  @RequestPart("fil") fil: List<MultipartFile>): HttpStatus {
        lagreIverksettService.lagreIverksettJson(objectMapper.writeValueAsString(iverksettJson), emptyList()).mapLeft {
            logger.error("Kunne ikke iverksette request : $iverksettJson")
            return HttpStatus.INTERNAL_SERVER_ERROR
        }
        return HttpStatus.OK
    }
}