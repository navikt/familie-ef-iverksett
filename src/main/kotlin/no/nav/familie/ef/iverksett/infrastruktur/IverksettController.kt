package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.familie.ef.iverksett.infrastruktur.json.VedtakJSON
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api"])
class IverksettController {

    @PostMapping(path = ["/test"])
    fun test(@RequestBody vedtakJSON: VedtakJSON): ResponseEntity<String> {
        return ResponseEntity<String>(vedtakJSON.toString(), HttpStatus.OK)
    }

}

