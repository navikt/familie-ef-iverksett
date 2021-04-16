package no.nav.familie.ef.iverksett.infrastruktur

import no.nav.familie.ef.iverksett.infrastruktur.json.VedtakJSON
import no.nav.familie.ef.iverksett.infrastruktur.transformer.TransformerVedtakJSON
import no.nav.familie.ef.iverksett.mottak.tjeneste.MottakService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api"])
class IverksettController(val mottakService: MottakService) {

    @PostMapping(path = ["/test"])
    fun test(@RequestBody vedtakJSON: VedtakJSON): ResponseEntity<String> {
        return ResponseEntity<String>(mottakService.test(TransformerVedtakJSON.transformer(vedtakJSON)), HttpStatus.OK)
    }

}

