package no.nav.familie.ef.iverksett.startIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.stream.Collectors

@RestController
@RequestMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
                path = ["/api/iverksett"],
                produces = [MediaType.APPLICATION_JSON_VALUE])
@ProtectedWithClaims(issuer = "azuread")
class IverksettController(
        val lagreIverksettService: LagreIverksettService,
        val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun iverksett(@RequestPart("data") data: IverksettJson,
                  @RequestPart("fil") fil: List<MultipartFile>) {
        val brevListe = opprettBrevListe(data, fil)
        lagreIverksettService.lagreIverksettJson(UUID.fromString(data.behandlingId),
                                                 objectMapper.writeValueAsString(data),
                                                 brevListe)
    }

    private fun opprettBrevListe(data: IverksettJson, fil: List<MultipartFile>): List<Brev> {
        return data.brev.map { brev ->
            val pdf = fil.find { it.originalFilename == brev.journalpostId }!!.bytes
            brev.toDomain(pdf)
        }
    }
}