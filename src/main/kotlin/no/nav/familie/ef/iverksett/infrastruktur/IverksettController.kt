package no.nav.familie.ef.iverksett.infrastruktur

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api"])
class IverksettController {

    @RequestMapping(path = ["/test"], method = [RequestMethod.GET])
    fun test(): ResponseEntity<String> {
        return ResponseEntity<String>("Hei", HttpStatus.OK)
    }

}

